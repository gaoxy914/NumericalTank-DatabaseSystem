package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author 高翔宇
 *
 */
public class ClientService {
	private Socket socket; // 套接字
	private String ip; // IP地址
	private int port; // 通信端口
	private Logger logger = LogManager.getLogger(ClientService.class.getName()); // 日志
	
	/**
	 * 启动ClientService
	 * @param ip 连接IP
	 * @param port 通信端口
	 */
	public void start(String ip, int port) {
		this.ip = ip;
		this.port = port;
		logger.info("请求建立tcp连接 IP：" + this.ip + ":" + this.port);
		try {
			socket = new Socket(ip, port);
		} catch (UnknownHostException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭ClientService
	 */
	public void close() {
		logger.info("关闭tcp连接 IP：" + socket.getRemoteSocketAddress());
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("ERROR WHILE CLOSING SOCKET.");
				e.printStackTrace();
			}
		}
		socket = null;
	}
	
	/**
	 * 判断ClientService与中间件连接装填
	 * @return true 连接；false 断开
	 */
	public boolean connected() {
		if (socket == null) {
			return false;
		} else {
			if (socket.isClosed()) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * 发送信息
	 * @param message 信息
	 */
	public synchronized void send(String message) {
		if (!socket.isClosed() && socket != null) {
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				bufferedWriter.write(message);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			} catch (IOException e) {
				System.out.println("SOKCET:" + socket.getRemoteSocketAddress() + "ERROR WHILE SENDING MESSAGE:" + message);
				e.printStackTrace();
			}
		}
	}
}
