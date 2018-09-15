package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import client.Client;
import conf.Config;
import object.Result;
import object.Task;

/**
 * ServerService
 * @author 高翔宇
 *
 */
public class ServerService {
	private volatile boolean running = false; // 服务运行状态
	private Thread connWatchThread; // server线程
	private ServerSocket serverSocket; // 套接字
	public static volatile Hashtable<Long, Document> tempRestore; // 暂存结果
	private Hashtable<String, Socket> connections; // 连接信息
	private Logger logger = LogManager.getLogger(ServerService.class.getName()); // 日志
	
	/**
	 * 启动ServerService
	 */
	public void start() {
		logger.info("启动结果接收线程");
		if (running) {
			return;
		}
		try {
			serverSocket = new ServerSocket(0);
			Config.serverPort = serverSocket.getLocalPort();
			logger.info("ServerPort:" + Config.serverPort);
			running = true;
			tempRestore = new Hashtable<Long, Document>();
			connWatchThread = new Thread(new ConnWatchThread());
			connWatchThread.start();
			connections = new Hashtable<String, Socket>();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CRREATING SOCKET.");
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭ServerService
	 */
	public void close() {
		logger.info("关闭结果接收线程");
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
				try {
					Socket socket = serverSocket.accept();
					connections.put(socket.getInetAddress().getHostAddress(), socket);
					new Thread(new ReceiveThread(socket)).start(); // 创建接收线程
				} catch (IOException e) {
					System.out.println("ERROR WHILE ACCEPTING CONNECTIONS.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 接收线程
	 * @author 高翔宇
	 *
	 */
	class ReceiveThread implements Runnable {
		boolean run = true; // 运行标志位
		Socket socket; // socket套接字
		
		/**
		 * ReceiveThread声明
		 * @param socket socket套接字
		 */
		public ReceiveThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			logger.info("client connected. ip :" + socket.getInetAddress().getHostAddress());
			String ip = socket.getInetAddress().getHostAddress();
//			System.out.println(ip);
//			System.out.println(Config.middleWareIP);
			if (Config.middleWareIP.contains(ip)) {
//				System.out.println("contain ip");
				ClientService clientService = Client.client2MiddleWarePool.get(ip);
				FileSender fileSender = Client.fileSenders.get(ip);
				if (clientService == null  || !clientService.connected()) {
//					System.out.println("restart conenction");
					clientService = new ClientService();
					clientService.start(ip, Config.mwServerPort);
					clientService.send(String.valueOf(Config.serverPort));
					Client.client2MiddleWarePool.replace(ip, clientService);
//					System.out.println(Client.needReSend);
					if (Client.needReSend) { // 所有中间件宕机 needReSend才置为true 
						for (Task task : Client.taskList) {
							clientService.send(task.serialize());
						}
						Client.needReSend = false;
					}
				}
				fileSender = new FileSender();
				fileSender.start(ip, Config.mwFileServerPort);
				Client.fileSenders.replace(ip, fileSender);
			} else {
//				System.out.println("do not contain ip");
				Config.middleWareIP.add(ip);
				ClientService clientService = new ClientService();
				clientService.start(ip, Config.mwServerPort);
				clientService.send(String.valueOf(Config.serverPort));
				Client.client2MiddleWarePool.put(ip, clientService);
				FileSender fileSender = new FileSender();
				fileSender.start(ip, Config.mwFileServerPort);
				Client.fileSenders.put(ip, fileSender);
			}
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (running && run) {
					String line = bufferedReader.readLine();
//					System.out.println(line);
					if (line != null) {
						try {
							Result result = new Result(line);
//							System.out.println(result);
							// Result拼接
							if (result.isEnd() && !tempRestore.containsKey(new Long(result.getTaskID()))) {
								Client.result.put(new Long(result.getTaskID()), result.getResult());
							} else if (!result.isEnd() && !tempRestore.containsKey(new Long(result.getTaskID()))) {
								tempRestore.put(new Long(result.getTaskID()), result.getResult());
							} else if (!result.isEnd() && tempRestore.containsKey(new Long(result.getTaskID()))) {
								@SuppressWarnings("unchecked")
								List<Document> newResult = (List<Document>) result.getResult().get("res");
								@SuppressWarnings("unchecked")
								List<Document> oldResult = (List<Document>) tempRestore
										.get(new Long(result.getTaskID())).get("res");
								oldResult.addAll(newResult);
								tempRestore.replace(new Long(result.getTaskID()), new Document("res", oldResult));
							} else {
								@SuppressWarnings("unchecked")
								List<Document> newResult = (List<Document>) result.getResult().get("res");
								@SuppressWarnings("unchecked")
								List<Document> oldResult = (List<Document>) tempRestore
										.remove(new Long(result.getTaskID())).get("res");
								oldResult.addAll(newResult);
								Client.result.put(new Long(result.getTaskID()), new Document("res", oldResult));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						overThis();
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
		
		/**
		 * 释放当前接收线程
		 */
		public void overThis() {
			logger.info("关闭连接：" + socket.getRemoteSocketAddress());
			if (run) {
				run = false;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String ip = socket.getInetAddress().getHostAddress();
			if (Config.middleWareIP.contains(ip)) {
				ClientService clientService = Client.client2MiddleWarePool.get(ip);
				clientService.close();
				Client.client2MiddleWarePool.remove(ip);
			}
		}
	}
}
