package service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FileSender {
	private Socket socket; // 套接字
	private String ip;
	private int port;
	private Logger logger = LogManager.getLogger(FileSender.class.getName()); // 日志
	
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

	public void sendFile(String filePath) {
		logger.info("向IP：" + ip + "发送文件：" + filePath);
		logger.info("send file: "+filePath);
		if (!socket.isClosed() && socket != null) {
			try {
				File file = new File(filePath);
				while(!file.exists());
				while(!file.canWrite());
	            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
	            DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); 
	            dos.writeUTF(file.getName());
	            dos.flush();
	            dos.writeLong((long) file.length());
	            dos.flush();
	            int bufferSize = 8192;
	            byte[] buf = new byte[bufferSize];
	            while (true) {
	                int read = 0;
	                if (dis != null)
	                    read = dis.read(buf);
	                if (read == -1)
	                    break;
	                dos.write(buf, 0, read);
	            }
	            dos.flush();            
	            dis.close();
	            logger.info("end send file: "+filePath);
			} catch (IOException e) {
				System.out.println("ERROR WHILE SENDING FILE:" + filePath);
				e.printStackTrace();
			}
		}
	}
}
