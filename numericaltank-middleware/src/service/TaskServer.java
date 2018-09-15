package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;
import conf.Constants;
import middleware.MiddleWare;
import object.Task;

/**
 * TaskServer
 * @author 高翔宇
 *
 */
public class TaskServer {
	private volatile boolean running = false; // service running status
	private Thread connWatchThread; // accept connection
	private ServerSocket serverSocket; // server socket
	private Hashtable<String, Socket> connections; // keep clients IP-socket
	private Logger mwLogger = LogManager.getLogger("middleware"); // middleware log
	
	/**
	 * 启动TaskServer
	 */
	public void start() {
		if (running) {
		mwLogger.info("启动任务接收线程");
			return;
		}
		try {
			serverSocket = new ServerSocket(Config.serverPort);
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
	 * 关闭TaskServer
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
//					System.out.println(Config.clientIP);
					if (Config.client_validate_way == 0) { // 0 ip验证
						if (Config.clientIP.contains(ip)) {
							mwLogger.info("与Client建立连接 IP：" + ip);
							connections.put(ip, socket);
							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							String line = bufferedReader.readLine();
//							System.out.println(line);
							int port = Integer.valueOf(line);
//							System.out.println(port);
							if (MiddleWare.clientResultSender.contain(ip, port)) {
//								System.out.println("contain sender");
								new Thread(new ReceiveThread(socket, ip, port)).start();
							} else {	
//								System.out.println("do not contain sender");
								int localPort = Config.getClientPort();
								if (localPort > 0) {
									mwLogger.info("请求建立tcp连接 IP：" + ip + ":" + port + "本地端口：" + localPort);
									Socket sender = new Socket(ip, port, null, localPort);
									MiddleWare.clientResultSender.insert(ip, port, sender);
									writeFile(ip, port);
									new Thread(new ReceiveThread(socket, ip, port)).start();
								} else {
									mwLogger.info("Client连接数目达到上限，没有足够的开放端口。");
									socket.close();
								}
							}
						}
					} else if (Config.client_validate_way == 1) { // 1 用户密码验证
						mwLogger.info("与Client建立连接 IP：" + ip);
						connections.put(ip, socket);
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String line = bufferedReader.readLine();
						String[] args = line.split(Constants.SPACE);
						double rank = ExecuteService.validate(args[0], args[1]);
						mwLogger.info("用户：" + args[0] + " 尝试登陆\n" + "登录验证权限：" + rank);
						int port = Integer.valueOf(args[2]);
						if (rank > 0) {
							mwLogger.info("用户：" + args[0] + " 允许登录");
							if (MiddleWare.clientResultSender.contain(ip, port)) {
								new Thread(new ReceiveThread(socket, ip, port)).start();
							} else {
								int localPort = Config.getClientPort();
								if (localPort > 0) {
									mwLogger.info("请求建立tcp连接 IP：" + ip + ":" + port + "本地端口：" + localPort);
									Socket sender = new Socket(ip, port, null, localPort);
									MiddleWare.clientResultSender.insert(ip, port, sender);
									writeFile(ip, port);
									new Thread(new ReceiveThread(socket, ip, port)).start();
								} else {
									mwLogger.info("Client连接数目达到上限，没有足够的开放端口。");
									socket.close();
								}
							}
						} else {
							mwLogger.info("用户：" + args[0] + " 拒绝登录");
							socket.close();
						}
					} else if (Config.client_validate_way == 2) { // 2 允许所有连接
						mwLogger.info("与Client建立连接 IP：" + ip);
						connections.put(ip, socket);
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String line = bufferedReader.readLine();
//						System.out.println(line);
						int port = Integer.valueOf(line);
//						System.out.println(port);
						if (MiddleWare.clientResultSender.contain(ip, port)) {
//							System.out.println("contain sender");
							new Thread(new ReceiveThread(socket, ip, port)).start();
						} else {	
//							System.out.println("do not contain sender");
							int localPort = Config.getClientPort();
							if (localPort > 0) {
								mwLogger.info("请求建立tcp连接 IP：" + ip + ":" + port + "本地端口：" + localPort);
								Socket sender = new Socket(ip, port, null, localPort);
								MiddleWare.clientResultSender.insert(ip, port, sender);
								writeFile(ip, port);
								new Thread(new ReceiveThread(socket, ip, port)).start();
							} else {
								mwLogger.info("Client连接数目达到上限，没有足够的开放端口。");
								socket.close();
							}
						}
					} else { // 拒绝所有连接
						socket.close();
						mwLogger.info("CLIENT验证方式选择越界。");
					}
				} catch (IOException e) {
					System.out.println("ERROR WHILE ACCEPTING CONNECTIONS.");
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 将活跃的Client写入文件
		 * @param ip Client IP
		 * @param port Client 通信端口
		 */
		private void writeFile(String ip, int port) {
			try {
				Boolean written = false;
				List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
				for (int i = 0; i < lines.size(); i++) {
					String[] word = lines.get(i).split(":");
					if (word[0].indexOf(Config.ACTIVE_CLIENT) != -1) {
						if (word.length > 1) {
							String newline = lines.remove(i) + ";" + ip + "&" + port;
							lines.add(i, newline);
							written = true;
							break;
						} else {
							String newline = lines.remove(i) + ip + "&" + port;
							lines.add(i, newline);
							written = true;
							break;
						}
					}
				}
				if (!written) {
					String newline = Config.ACTIVE_CLIENT + ":" + ip + "&" + port;
					lines.add(newline);
				}
				Files.write(Paths.get("Config.txt"), lines);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		String ip; // Client IP地址
		int port; //　Client 通信端口
		
		/**
		 * ReceiveThread声明
		 * @param socket socket套接字
		 * @param ip Client IP地址
		 * @param port Client 通信端口
		 */
		public ReceiveThread(Socket socket, String ip, int port) {
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
						try {
							Task task = new Task(line);
							if (task.getResult() == Constants.UNDO) {
								mwLogger.info("收到任务：" + task.toString());
								MiddleWare.middleWareStatus.addTask(task);
								mwLogger.info("中间件状态：" + MiddleWare.middleWareStatus.toString());
							} else if (task.getResult() == Constants.DONE && !MiddleWare.middleWareStatus.isPrimary()) {
								MiddleWare.middleWareStatus.delTask(task);
								mwLogger.info("完成任务：" + task.toString());
							}
						} catch (Exception e) {
							e.printStackTrace();
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
		 * @param ip Client IP
		 * @param port Client 通信端口
		 */
		public void overThis(String ip, int port) {
			mwLogger.info("关闭连接：" + socket.getRemoteSocketAddress());
			if (run) {
				run = false;
			}
			MiddleWare.clientResultSender.remove(ip, port);
			try {
				List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
				for (int i = 0; i < lines.size(); i++) {
					String[] word = lines.get(i).split(":");
					if (word[0].indexOf(Config.ACTIVE_CLIENT) != -1) {
						String newline = Config.ACTIVE_CLIENT + ":";
						String[] client = word[1].split(";");
						boolean first = true;
						for (int j = 0; j < client.length; j++) {
							if (!client[j].equals(ip + "&" + port)) {
								if (first) {
									newline += client[j];
								} else {
									newline += client[j] + ";";
								}
							}
						}
						lines.remove(i);
						lines.add(i, newline);
						break;
					}
				}
				Files.write(Paths.get("Config.txt"), lines);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
