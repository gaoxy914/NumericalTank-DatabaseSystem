package test;

import java.util.Random;

import org.bson.Document;

import client.Client;

public class speedtest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 20; i++) {
			test(i);
		}
	}
	
	public static void test(int x) {
		long sum = 0;
		long min = 10000;
		long max = 0;
		Random random = new Random();
		String[] user = { "gaoxy", "xxx", "lx", "tl", "qq", "cqm" };
		for (int i = 0; i < 100; i++) {
			long start = System.currentTimeMillis();
			client.find(user[random.nextInt(5)], "project", "Ship", new Document("Id", random.nextInt(5000)), null, null);
//			client.find(user[random.nextInt(5)], "project", "ship", new Document("id", random.nextInt(5000)), null, null);
			long end = System.currentTimeMillis();
			sum += (end - start);
			min = Math.min(min, (end - start));
			max = Math.max(max, (end - start));
		}
		System.out.println("test" + x);
		System.out.println("totaltime : " + sum + "ms");
		System.out.println("maxtime : " + max + "ms");
		System.out.println("mintime : " + min + "ms");
		System.out.println("avgtime : " + (sum*1.0 / 100) + "ms");
	}
}
