package test;

import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import client.Client;

public class insertpressuretest {
public static Client client;
	
	public static volatile int count = 0;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
		int numThread = scanner.nextInt();
		for (int i = 0; i < numThread; i++) {
			new Thread(new QueryThread()).start();
		}
		int begin = count;
		long start = System.currentTimeMillis();
		while (true) {
			System.out.println(count - begin);
			if (count - begin == 10000) {
				System.out.println(System.currentTimeMillis() - start);
				break;
			}
		}
		scanner.close();
	}
	
	public synchronized static void add() {
		count ++;
	}
	
	static class QueryThread implements Runnable{
		public void run() {
			Random random = new Random();
			String[] cartype = {"sport", "suv", "racing", "MPV", "jeep", "taxi", "bus", "police"};
			while (true) {	
				Document doc = new Document("Id", random.nextInt(100000));
				doc.append("cartype", cartype[random.nextInt(7)]);
				doc.append("weight", random.nextInt(10)+"t");
				doc.append("people", random.nextInt(9));
				doc.append("price", random.nextInt(10000000));
				doc.append("color", random.nextInt(10));
				client.insertOne("gaoxy", "project", "car", doc);
				add();
			}
		}
	}
}
