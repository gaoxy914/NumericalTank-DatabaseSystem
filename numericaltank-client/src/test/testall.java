package test;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;

import client.Client;

public class testall {
	public static Client client;
	
	public static void main(String args[]) {
		try {
			client = new Client(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(client.insertOne("xxx", "project","test",new Document("attribute1","value1")
				.append("attribute2","value2")
				.append("attribute3","value3")));
		List<Document> obj = new ArrayList<Document>();
		obj.add(new Document("attribute1","value11")
				.append("attribute2","value12")
				.append("attribute3","value13"));
		obj.add(new Document("attribute1","value21")
				.append("attribute2","value22")
				.append("attribute3","value23"));
		obj.add(new Document("attribute1","value31")
				.append("attribute2","value32")
				.append("attribute3","value33"));
		System.out.println(client.insertMany("xxx", "project","test", obj));
		System.out.println("end insert");
		Document groupfield = new Document("_id","$attribute1");
		groupfield.append("count", new Document("$sum",1));
		Document group = new Document("$group",groupfield);
		List<Document> cond = new ArrayList<Document>();
		cond.add(group);
		for(Document doc:
			client.aggregate("xxx","project","test", cond))
			System.out.println(doc);
		System.out.println("end aggregate");
		System.out.println(client.count("xxx","project","test",new Document()));
		System.out.println("end count");
		Document ur = client.updateOne("xxx","project","test",new Document("attribute1","value11"),
				new Document("$set",new Document("attribute1","value1")));
		System.out.println(ur.get("ModifiedCount")); 
		System.out.println(client.updateMany("xxx","project","test",new Document("attribute1","value21"),
				new Document("$set",new Document("attribute1","value1"))));
		System.out.println(client.findOneAndUpdate("xxx","project","test",new Document("attribute1","value31"),
				new Document("$set",new Document("attribute1","value11")),new FindOneAndUpdateOptions()));
		System.out.println(client.findOneAndUpdate("xxx","project","test",new Document("attribute1","value31"),
				new Document("$set",new Document("attribute2","value12"))));
		System.out.println(client.findOneAndUpdate("xxx","project","test",new Document("attribute1","value31"),
				new Document("$set",new Document("attribute3","value13")),null));
		System.out.println("end update");
		Document dr1= client.deleteOne("xxx","project","test",new Document("attribute2","value32"));
		System.out.println(dr1.get("DeletedCount"));
		Document dr2= client.deleteMany("xxx","project","test",new Document("attribute2","value22"));
		System.out.println(dr2.get("DeletedCount"));
		System.out.println(client.findOneAndDelete("xxx","project","test",new Document("attribute2","value12"),new FindOneAndDeleteOptions()));
		System.out.println(client.findOneAndDelete("xxx","project","test",new Document("attribute3","value13"),null));
		System.out.println("end delete");
		String s = client.createIndex("xxx","project","test",new Document("attribute1",1), new IndexOptions());
		String s1 = client.createIndex("xxx","project","test",new Document("attribute1",1).append("attribute2", 1), null);
		System.out.println(s);
		System.out.println(s1);
		List<IndexModel> lim = new ArrayList<IndexModel>();
		lim.add(new IndexModel(new Document("attribute2",1),new IndexOptions()));
		lim.add(new IndexModel(new Document("attribute3",1),new IndexOptions()));
		System.out.println(client.createIndexes("xxx","project","test",lim));
		System.out.println(client.listIndexes("xxx","project","test"));
		System.out.println(client.dropIndex("xxx","project","test",new Document("attribute3",1)));
		System.out.println(client.dropIndex("xxx","project","test",s));
		System.out.println(client.dropIndexes("xxx","project","test"));
		System.out.println("end index");
	}
}
