package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import conf.Config;
import conf.Constants;
import middleware.MiddleWare;

/**
 * CMDServer
 * @author 高翔宇
 *
 */
public class CMDServer {
	private volatile boolean running = false; // 运行标志位
	private Thread connWatchThread; // 监听线程
	private ServerSocket serverSocket; // 套接字
	private Hashtable<String, Socket> connections; // 连接信息
	private Logger mwLogger = LogManager.getLogger("middleware"); // middleware log
	private Logger cmdLogger = LogManager.getLogger("cmd"); // cmd log
	
	/**
	 * 启动CMDServer
	 */
	public void start() {
		if (running) {
			mwLogger.info("启动CMD任务接收线程");
				return;
			}
			try {
				serverSocket = new ServerSocket(Config.cmdServerPort);
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
	 * 关闭CMDServer
	 */
	public void close() {
		mwLogger.info("关闭任务接收线程");
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
					String ip = socket.getInetAddress().getHostAddress();
					mwLogger.info("IP：" + ip + "试图建立连接");
//					System.out.println(Config.clientIP);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String line = bufferedReader.readLine();
					String[] args = line.split(Constants.SPACE);
					int port = Integer.valueOf(args[2]);
					int localPort = Config.getCMDPort();
					if (localPort > 0) {
						mwLogger.info("请求建立tcp连接 IP：" + ip + ":" + port + "本地端口：" + localPort);
						Socket cmdsender = new Socket(ip, port, null, localPort);
						MiddleWare.cmdResultSender.insert(ip, port, cmdsender);
						double rank = ExecuteService.validate(args[0], args[1]);
						cmdLogger.info("用户：" + args[0] + " 尝试登陆\n" + "登录验证权限：" + rank);
						if (rank > 0) {
							cmdLogger.info("用户：" + args[0] + " 允许登录");
							mwLogger.info("与CONSOLE建立连接 IP：" + ip);
							connections.put(ip, socket);
							new Thread(new ReceiveThread(socket, args[0], rank, ip, port)).start();
							MiddleWare.cmdResultSender.sendData(ip, port, new Document("res", "welcome").toJson());
						} else {
							cmdLogger.info("用户：" + args[0] + " 拒绝登录");
							MiddleWare.cmdResultSender.sendData(ip, port, new Document("res", "Incorrect username or password.").toJson());
							MiddleWare.cmdResultSender.remove(ip, port);
							mwLogger.info("拒绝连接请求 IP：" + ip);
							socket.close();
						}
					} else {
						mwLogger.info("CMD连接数目达到上限，没有足够的开放端口。");
						socket.close();
					}
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
		double rank; // 权限 1 normal 2 root
		String user; // 用户名
		Socket socket; // socket套接字
		String ip; // IP地址
		int port; // 通信端口
		
		/**
		 * ReceiveThread声明
		 * @param socket 套接字
		 * @param user 用户名
		 * @param rank 权限
		 * @param ip IP地址
		 * @param port 通信端口
		 */
		public ReceiveThread(Socket socket, String user, double rank, String ip, int port) {
			this.rank = rank;
			this.user = user;
			this.socket = socket;
			this.ip = ip;
			this.port = port;
		}
		
		public void run() {
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (running && run) {
					String line = bufferedReader.readLine();
					if (line != null) {
						cmdLogger.info("用户：" + user + " 执行指令：" + line);
						Document document = ExecuteService.runCommand(line, rank);
						MiddleWare.cmdResultSender.sendData(ip, port, document.toJson());
						if (MiddleWare.endFlag) {
							MiddleWare.middleWare.clsoe();
						}
					} else {
						overThis(ip, port);
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
		 * 释放接收线程
		 * @param ip IP地址
		 * @param port 通信端口
		 */
		public void overThis(String ip, int port) {
			mwLogger.info("关闭连接：" + socket.getRemoteSocketAddress());
			if (run) {
				run = false;
			}
			MiddleWare.cmdResultSender.remove(ip, port);
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("ERROR WHILE CLOSING SOKCET");
					e.printStackTrace();
				}
			}
		}
	}
}
