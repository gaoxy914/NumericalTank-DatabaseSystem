package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;
import conf.Constants;
import middleware.MiddleWare;

/**
 * CheckAliveService
 * @author 高翔宇
 *
 */
public class CheckAliveService {
	private Socket socket; // client socket
	private volatile boolean running = false; // service running status
	private volatile boolean getReply = true; // response status
	private long lastSendTime; // time of last send
	private Thread checkThread; // send heart beat
	private Thread receiveThread; // receive response
	private Logger logger = LogManager.getLogger("middleware"); // log
	
	/**
	 * 启动CheckAliveService
	 */
	public void start() {
		logger.info("启动心跳检测线程");
		try {
			logger.info("向主中间件发起心跳检测，中间件IP：" + Config.mainMiddleWareIP + "本地端口：" + Config.checkPort);
			socket = new Socket(Config.mainMiddleWareIP, Config.listenPort, null, Config.checkPort);
			lastSendTime = System.currentTimeMillis();
			running = true;
			checkThread = new Thread(new CheckThread());
			receiveThread = new Thread(new ReceiveThread());
			checkThread.start();
			receiveThread.start();
		} catch (UnknownHostException e) {
			System.out.println("ERROR WHILE CREATING SOKCET.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOKCET.");
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭CheckAliveService
	 */
	public void close() {
		logger.info("关闭心跳检测线程");
		if (running) {
			running = false;
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("ERROR WHILE CLOSING SOKCET.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 中间件系统间通信
	 * @param message 信息
	 */
	public void send(String message) {
		try {
			if (socket != null && !socket.isClosed()) {
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				bufferedWriter.write(message);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}	
		} catch (IOException e) {
			System.out.println("ERROR WHILE SENDING MESSAGE:" + message);
			e.printStackTrace();
		}
	}
	
	/**
	 * CheckThread线程 检测中间件系统存活线程
	 * @author 高翔宇
	 *
	 */
	class CheckThread implements Runnable {
		long checkDelay = 10; // check间隔
		long keepAliveDelay = 2000; // 存活时间

		public void run() {
			while (running) {
				if (!MiddleWare.middleWareStatus.isPrimary()) {
					if (System.currentTimeMillis() - lastSendTime > keepAliveDelay && getReply) {
						CheckAliveService.this.send(Constants.getCheckMessage());
						getReply = false;
						lastSendTime = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - lastSendTime > keepAliveDelay && !getReply) {
						if (Config.backupMiddleWareIP.get(0).equals(Config.localIP)) {
							MiddleWare.middleWareStatus.setPrimary(true);
						}
						Config.backupMiddleWareIP.add(Config.mainMiddleWareIP);
						Config.mainMiddleWareIP = Config.backupMiddleWareIP.remove(0);
						logger.info("主中间件变化");
						logger.info("主中间件IP地址：" + Config.mainMiddleWareIP);
						getReply = true;
						if (!Config.localIP.equals(Config.mainMiddleWareIP)) {
							MiddleWare.checkAliveService.start();
						} else {
							MiddleWare.checkAliveService.close();
						}
					} else {
						try {
							Thread.sleep(checkDelay);
						} catch (Exception e) {
							System.out.println("ERROR IN CHECKTHREAD.");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/**
	 * ReceiveThread线程 接收中间件系统响应
	 * @author 高翔宇
	 *
	 */
	class ReceiveThread implements Runnable {
		public void run() {
			while (running) {
				if (!MiddleWare.middleWareStatus.isPrimary()) {
					BufferedReader bufferedReader = null;
					try {
						bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String line = bufferedReader.readLine();
						if (line != null) {
							String[] args = line.split(Constants.SPACE);
							if (args[0].equals(Constants.STILL)) {
								getReply = true;
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
