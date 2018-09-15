package test;

import java.util.List;
import java.util.Random;

import org.bson.Document;

import client.Client;

public class bigdatatest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Random random = new Random();
		String[] user = { "gaoxy", "xxx", "lx", "tl", "qq", "cqm" };
		long start = System.currentTimeMillis();
		List<Document> list = client.find(user[random.nextInt(5)], "project", "Ship");
		System.out.println(list.size());
		long end = System.currentTimeMillis();
		System.out.println("time : " + (end-start) + "ms");
	}
}
