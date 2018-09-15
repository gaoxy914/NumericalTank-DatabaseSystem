package test;

import java.util.Random;

import client.Client;

public class test {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Random random = new Random();
		String[] collection = { "caller", "files", "histories", "material", "project", "solvers", "target", "tasks", "templates", "users"};
		while (true) {
			client.find("gaoxy", "caller", collection[random.nextInt(9)]);
//			System.out.println("查询结果大小：" + list.size());
		}
	}
}
