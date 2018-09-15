package test;

import java.util.Random;

import org.bson.Document;

import client.Client;

public class insertspeedtest {
	public static Client client;
	
	public static void main(String[] args) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long sum = 0;
		long min = 10000;
		long max = 0;
//		String[] cartype = {"sport", "suv", "racing", "MPV", "jeep", "taxi", "bus", "police"};
		for (int i = 0; i < 10000; i++) {
			Random rand = new Random();
			Document doc = new Document("user_id","xxx_"+i);
			doc.append("Id", i);
			doc.append("template_id","temp_"+i);
			doc.append("featureLength",rand.nextInt(10000));
			doc.append("floatingWidth",rand.nextInt(10000));
			doc.append("platformQuality",rand.nextInt(10000));
			doc.append("platformDraught",rand.nextDouble());
			doc.append("volumeDrainage",rand.nextDouble());
			doc.append("surfaceArea",rand.nextDouble());
			doc.append("Xg",rand.nextDouble());
			doc.append("Zg",rand.nextDouble());
			doc.append("Yg",rand.nextDouble());
			doc.append("rolling",rand.nextDouble());
			doc.append("pitch",rand.nextDouble());
			doc.append("yawing",rand.nextDouble());
			doc.append("R44",rand.nextDouble());
			doc.append("fluidChamber","fluidChamber_"+rand.nextInt(1000)+"_"+rand.nextInt(1000));
			doc.append("version","version_"+rand.nextInt(1000)+"_"+rand.nextInt(1000));
			doc.append("workingDirectory","workingDirectory_"+rand.nextInt(1000)+"_"+rand.nextInt(1000));
			doc.append("RSH",rand.nextInt(1)==1 ? true:false);
			doc.append("SSH",rand.nextInt(1)==1 ? true:false);
			long start = System.currentTimeMillis();
			client.insertOne("gaoxy", "project", "car2", doc);
			long end = System.currentTimeMillis();
			sum += (end - start);
			min = Math.min(min, (end - start));
			max = Math.max(max, (end - start));
		}
		System.out.println("totaltime : " + sum + "ms");
		System.out.println("maxtime : " + max + "ms");
		System.out.println("mintime : " + min + "ms");
		System.out.println("avgtime : " + (sum*1.0 / 10000) + "ms");
	}
}
