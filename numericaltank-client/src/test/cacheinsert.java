package test;

import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import client.Client;

public class cacheinsert {
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
			int Id = random.nextInt(100000);
			System.out.println("insert: {id:" + Id + "name:gaoxy}");
			boolean result = client.insertOne("gaoxy", "project", "ex1", new Document("id", Id).append("name", "gaoxy"));
			System.out.println(result);
		}
		scanner.close();
	}
}
