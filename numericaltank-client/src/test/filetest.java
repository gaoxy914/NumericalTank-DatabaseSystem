package test;

import java.util.Random;
import java.util.Scanner;

import org.bson.Document;

import client.Client;

public class filetest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
		String command = scanner.nextLine();
		if (command.equals("export")) {
			Random random = new Random();
			int Id =  random.nextInt(3914736);
			System.out.println("exportfile:" + Id + ".csv");
			boolean result = client.exportFile("gaoxy", "project", "Ship", new Document("Id", Id), "Id,Name,Type,Weight,Length,Width,Draft", null, null, null, "csv", Id+".csv");
			System.out.println(result);
		} else {
			System.out.println("importfile:1.csv");
			boolean result = client.importFile("xxx", "logdb", "ship", null,true,true,true,"insert",null,"1.csv","csv");
			System.out.println(result);
		}
		scanner.close();
	}
}
