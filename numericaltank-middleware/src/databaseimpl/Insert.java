package databaseimpl;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public class Insert {
	public static Document insert(MongoCollection<Document> collection, Document obj) {
		collection.insertOne(obj);
		return new Document("res", true);
	}

	public static Document insertMany(MongoCollection<Document> collection, List<Document> obj) {
		collection.insertMany(obj);
		return new Document("res", true);
	}
}
