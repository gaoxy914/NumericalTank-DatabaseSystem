package databaseimpl;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public class Count {
	public static Document count(MongoCollection<Document> collection, Document filter) {
		return new Document("res", collection.count(filter));
	}
}
