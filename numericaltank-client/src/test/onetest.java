package test;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import client.Client;

public class onetest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
		String string = scanner.nextLine();
		if (string.equals("start")) {
			Random random = new Random();
			String[] user = { "gaoxy", "xxx", "lx", "tl", "qq", "cqm" };
			long start = System.currentTimeMillis();
			List<Document> list = client.find(user[random.nextInt(5)], "project", "Ship", new Document("Id", 1), null, null);
			System.out.println(list);
			long end = System.currentTimeMillis();
			System.out.println("time : " + (end-start) + "ms");
		}
		client.find("gaoxy", "project", "Ship", new Document("Id", 1), null, null);
		client.find("gaoxy", "project", "Ship", new Document("Id", 1), null, null);
		client.find("gaoxy", "project", "Ship", new Document("Id", 1), null, null);
		client.find("gaoxy", "project", "Ship", new Document("Id", 1), null, null);
		client.find("gaoxy", "project", "Ship", new Document("Id", 1), null, null);
		scanner.close();
	}
}
