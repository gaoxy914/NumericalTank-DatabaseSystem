package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientService {
	private Socket socket;
	private int serverPort = 53000;

	public void start(String ip) {
		try {
			socket = new Socket(ip, serverPort);
		} catch (UnknownHostException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR WHILE CREATING SOCKET.");
			e.printStackTrace();
		}
	}

	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("ERROR WHILE CLOSING SOCKET.");
				e.printStackTrace();
			}
		}
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

	public synchronized void send(String message) {
		try {

			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
