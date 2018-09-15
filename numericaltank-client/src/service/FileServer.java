package service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import client.Client;
import conf.Config;
import conf.Constants;

public class FileServer {
	private volatile boolean running = false;
	private Thread connWatchThread;
	private ServerSocket serverSocket;
	private Hashtable<String, Socket> connections;
	private Logger logger = LogManager.getLogger(FileServer.class.getName());
	
	public void start() {
		logger.info("启动文件接收线程");
		if (running) {
			return;
		}
		try {
			serverSocket = new ServerSocket(0);
			Config.fileServerPort = serverSocket.getLocalPort();
			logger.info("FileServerPort:" + Config.fileServerPort);
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
					String utf = inputStream.readUTF();
	//				System.out.println(utf);
					if (utf.equals(Constants.IMPORT_FILE)) {
						String filename = inputStream.readUTF();
						logger.info("向中间件 IP:" + socket.getRemoteSocketAddress() + "发送文件" + filename);
						File file = new File(filename);
						System.out.println("send file start");
						while (!file.exists());
						System.out.println("send file exist");
						while (!file.canWrite());					
						System.out.println("send file");
						Client.fileSenders.get(socket.getInetAddress().getHostAddress()).sendFile(filename);//change						
						logger.info("发送完成");
					} else {
						String savePath = Config.savePath;
						int bufferSize = 8192;
						byte[] buf = new byte[bufferSize];
						int passedlen = 0;
						long len = 0;
						savePath += utf;
						DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
						len = inputStream.readLong();
						logger.info("文件长度:" + len + "\n");       
			            while (true) {
			                int read = 0;
			                if (inputStream != null)
			                    read = inputStream.read(buf);
			                if (read == -1)
			                    break;
			                passedlen += read;
			                //logger.info("已收到文件：" +  (passedlen * 100L/ len) + "%\n");
			                fileOut.write(buf, 0, read);
			                if(passedlen * 1L== len)
			                {
			                	break;
			                }
			            }
			            logger.info("完成接受，文件保存在： " + savePath + "\n");
			            System.out.println("完成接受，文件保存在： " + savePath + "\n");
			            fileOut.close();
					}
				}
				//inputStream.close();
			} catch (Exception e) {
				System.out.println("ERROR WHILE RECEIVING FILE.");
				e.printStackTrace();
			}
			overThis();
		}
		
		public void overThis() {
			logger.info("关闭连接：" + socket.getRemoteSocketAddress());
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
