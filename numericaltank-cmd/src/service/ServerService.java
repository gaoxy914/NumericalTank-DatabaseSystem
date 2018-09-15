package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.bson.Document;

import cmd.CMD;

public class ServerService {
	private volatile boolean running = false;
	private Thread connWatchThread;
	private ServerSocket serverSocket;
	public static int consolePort;
	private Hashtable<String, Socket> connections;
	
	public void start() {
		if (running) {
			return;
		}
		try {
			serverSocket = new ServerSocket(0);
			consolePort = serverSocket.getLocalPort();
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
		long lastReceiveTime = System.currentTimeMillis();

		public ReceiveThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (running && run) {
					String line = bufferedReader.readLine();
					if (line != null) {
						CMD.result = Document.parse(line);
						CMD.flag = true;
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
//			overThis();
		}

		public void overThis() {
			if (run) {
				run = false;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			if(CMD.clientService.connected()) {
				CMD.clientService.close();
			}
		}
	}
}
