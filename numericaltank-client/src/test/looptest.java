package test;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import client.Client;

public class looptest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0], "root", "root");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
		String string = scanner.nextLine();
		if (string.equals("start")) {
			Random random = new Random();
			String[] user = { "gaoxy", "xxx", "lx", "tl", "qq", "cqm" };
			while (true) {
//				client.find(user[random.nextInt(5)], "project", "ship", new Document("id", random.nextInt(100000)), null, null);
				List<Document> list = client.find(user[random.nextInt(5)], "project", "Ship", new Document("Id", random.nextInt(3914736)), null, null);
				System.out.println("查询结果大小：" + list.size());
			}
		}
		scanner.close();
	}
}
