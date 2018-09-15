package service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;

public class FileServer {
	private volatile boolean running = false; // service running status
	private Thread connWatchThread; // accept connection
	private ServerSocket serverSocket; // server socket
	private Hashtable<String, Socket> connections; // keep clients IP-socket
	private Logger logger = LogManager.getLogger("middleware"); // log
	
	public void start() {
		logger.info("启动文件接收线程");
		if (running) {
			return;
		}
		try {
			serverSocket = new ServerSocket(Config.fileServerPort);
			running = true;
			connWatchThread = new Thread(new ConnWatchThread());
			connWatchThread.start();
			connections = new Hashtable<String, Socket>();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		}
	}
	
	public void close() {
		logger.info("关闭文件接收线程");
		if (running) {
			running = false;
		}
		for (Socket socket : connections.values()) {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("ERROR WHILE CLOSING SOCKET.");
				e.printStackTrace();
			}
		}
	}

	class ConnWatchThread implements Runnable {
		public void run() {
			while (running) {
				try {
					Socket socket = serverSocket.accept();
					logger.info("与Client建立文件连接 IP：" + socket.getInetAddress().getHostAddress());
					connections.put(socket.getInetAddress().getHostAddress(), socket);
					new Thread(new ReceiveThread(socket)).start();
				} catch (IOException e) {
					System.out.println("ERROR WHILE ACCEPTING CONNECTIONS.");
					e.printStackTrace();
				}
			}
		}
	}
	
	class ReceiveThread implements Runnable {
		boolean run = true;
		Socket socket;
		
		public ReceiveThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			DataInputStream inputStream = null;
			try {
				inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				while(true) {
					String savePath = Config.savePath;
					int bufferSize = 8192;
					byte[] buf = new byte[bufferSize];
					int passedlen = 0;
					long len = 0;
					savePath += inputStream.readUTF();
					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
					len = inputStream.readLong();
					logger.info("文件长度:" + len);
		            while (true) {
		                int read = 0;
		                if (inputStream != null)
		                    read = inputStream.read(buf);
		                passedlen += read;
		                if (read == -1)
		                    break;
		                //logger.info("收到文件 " +  (passedlen * 100L/ len) + "%");
		                fileOut.write(buf, 0, read);
		                if(passedlen * 1L== len)
		                {
		                	break;
		                }
		            }
		           logger.info("收到文件，文件存储在：" + savePath + "\n");
		            fileOut.close();
				}
	            //inputStream.close();
			} catch (Exception e) {
				overThis();
			}
			overThis();
		}
		
		public void overThis() {
			logger.info("关闭文件连接：" + socket.getRemoteSocketAddress());
			if (run) {
				run = false;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("ERROR WHILE CLOSING SOCKET.");
					e.printStackTrace();
				}
			}
		}
	}
}
