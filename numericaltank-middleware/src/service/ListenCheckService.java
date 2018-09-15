package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;
import conf.Constants;
import middleware.MiddleWare;

/**
 * ListenCheckService
 * @author 高翔宇
 *
 */
public class ListenCheckService {
	private volatile boolean running = false; // service running status
	private Thread connWatchThread; // accept connection
	private ServerSocket serverSocket; // server socket
	private Hashtable<String, Socket> connections; // keep clients IP-socket
	private Logger logger = LogManager.getLogger("middleware"); // log
	
	/**
	 * 启动ListenCheckService
	 */
	public void start() {
		logger.info("启动心跳监听线程");
		if (running) {
			return;
		}
		try {
			serverSocket = new ServerSocket(Config.listenPort);
			running = true;
			connWatchThread = new Thread(new ConnWatchThread());
			connWatchThread.start();
			connections = new Hashtable<String, Socket>();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭ListenCheckService
	 */
	public void close() {
		logger.info("关闭心跳监听线程");
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
	
	/**
	 * 监听线程
	 * @author 高翔宇
	 *
	 */
	class ConnWatchThread implements Runnable {
		public void run() {
			while (running) {
				if (MiddleWare.middleWareStatus.isPrimary()) {
					try {
						Socket socket = serverSocket.accept();
						connections.put(socket.getInetAddress().getHostAddress(), socket);
						new Thread(new ListenThread(socket)).start();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	/**
	 * 监听check并回复线程
	 * @author 高翔宇
	 *
	 */
	class ListenThread implements Runnable {
		boolean run = true; // 运行标志位
		Socket socket; // socket套接字
		long lastReceiveTime = System.currentTimeMillis(); // 上次接收到check的时间

		public ListenThread(Socket socket) {
			this.socket = socket;
		}
		
		public void send(String message) {
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				bufferedWriter.write(message);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			} catch (IOException e) {
				System.out.println("ERROR WHILE SENDING MESSAGE:" + message);
				e.printStackTrace();
			}
		}
		
		public void run() {
			logger.info("从中间件：" + socket.getRemoteSocketAddress() + "开始心跳检测");
			while (running && run) {	
				if (MiddleWare.middleWareStatus.isPrimary()) {
					BufferedReader bufferedReader = null;
					try {
						bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String line = bufferedReader.readLine();
						if (line != null) {
							String[] args = line.split(Constants.SPACE);
							if (args[0].equals(Constants.CHECK)) {
								send(Constants.getReplyMessage());
							}
						}
					} catch (Exception e) {
						try {
							bufferedReader.close();
							bufferedReader = null;
						} catch (IOException e1) {
							System.out.println("Error while closing buffers.");
						}
					}
				}
			}
		}
	}
}
