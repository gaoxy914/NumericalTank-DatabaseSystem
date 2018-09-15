package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;

/**
 * ResultSender
 * @author 高翔宇
 *
 */
public class ResultSender {
	private HashMap<String, Socket> senders; // 发送者
	private Logger mwLogger = LogManager.getLogger("middleware"); // log
	
	/**
	 * ResultSender声明
	 */
	public ResultSender() {
		senders = new HashMap<String, Socket>();
	}
	
	/**
	 * 插入新的sender
	 * @param ip IP地址
	 * @param port 通信端口
	 * @param socket 套接字
	 */
	public void insert(String ip, int port, Socket socket) {
		mwLogger.info("插入新sender IP: " + ip + " Port: " + port);
		String key = ip + "." + port;
		senders.put(key, socket);
	}
	
	/**
	 * 移除sender
	 * @param ip IP地址
	 * @param port 通信端口
	 */
	public void remove(String ip, int port) {
		String key = ip + "." + port;
		Socket socket = senders.get(key);
		int localPort = socket.getLocalPort();
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		senders.remove(key);
		Config.freeClientPort(localPort);
		mwLogger.info("删除sender IP: " + ip + " Port: " + port + " LocalPort: " + localPort);
	}
	
	/**
	 * sender是否存在
	 * @param ip IP地址
	 * @param port 通信端口
	 * @return true 存在；false 不存在
	 */
	public boolean contain(String ip, int port) {
		String key = ip + "." + port;
		return senders.containsKey(key);
	}
	
	/**
	 * 发送信息
	 * @param ip 接收端IP地址
	 * @param port 接收端通信端口
	 * @param message 信息
	 */
	public void sendData(String ip, int port, String message) {
		String key = ip + "." + port;
		Socket socket;
		if (senders.containsKey(key)) {
			socket = senders.get(key);
			sendData(socket, message);
		} else {
			try {
				socket = new Socket(ip, port);
				senders.put(key, socket);
				mwLogger.info("插入新Client IP: " + ip + " Port: " + port);
				sendData(socket, message);
			} catch (UnknownHostException e) {
				System.out.println("ERROR WHILE CREATING SOCKET.");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("ERROR WHILE CREATING SOCKET.");
				e.printStackTrace();
			}
		} 
	}
	
	private void sendData(Socket socket, String message) {
		try {
			BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bufferWriter.write(message);
		    bufferWriter.newLine();
		    bufferWriter.flush();
		} catch (IOException e) {
			System.out.println("ERROR WITH SOCKET IO.");
			e.printStackTrace();
		} 
	}
	
	/**
	 * 关闭sender
	 */
	public void close() {
		mwLogger.info("关闭所有sender");
		for(Socket socket: senders.values()) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
