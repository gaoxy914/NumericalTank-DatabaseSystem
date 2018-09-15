package service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;

public class FileSender {
	private Socket socket; // client socket
	private Logger logger = LogManager.getLogger("middleware"); // log
	private String ip;
	
	public void start(String ip, int port, int localPort) {
		logger.info("请求建立文件传输连接：" + ip + ":" + port + "本地端口：" + localPort);
		this.ip = ip;
		try {
			socket = new Socket(ip, port, null, localPort);
		} catch (UnknownHostException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		}
	}
	
	public void close() {
		logger.info("关闭文件传输连接");
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
	
	public void sendFile(String filename) {
		logger.info("向IP：" + ip + "发送文件：" + filename);
		logger.info("send file: "+filename);
		if (!socket.isClosed() && socket != null) {
			try {
				File file = new File(Config.savePath+filename);
				while(!file.exists());
				while(!file.canWrite());
	            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Config.savePath+filename)));
	            DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); 
	            dos.writeUTF(filename);
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
	            logger.info("end send file: "+filename);
			} catch (IOException e) {
				System.out.println("ERROR WHILE SENDING FILE:" + filename);
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(String filePath) {
		logger.info("向IP：" + ip + "请求发送文件：" + filePath);
		if (!socket.isClosed() && socket != null) {
			System.out.println("sendMessage start");
	        try {
	        	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());            
	            dos.writeUTF("importFile");
	            dos.flush();
	            dos.writeUTF(filePath);
	            dos.flush();
	            dos.flush();
	            System.out.println("sendMessage end");
	        } catch (Exception e) {
	        	System.out.println("ERROR WHILE SENDING FILE:" + filePath);
				e.printStackTrace();
	        }
		}
	}
}
