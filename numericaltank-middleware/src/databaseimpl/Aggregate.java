package databaseimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

public class Aggregate {
	public static Document aggregate(MongoCollection<Document> collection, List<Document> cond) {
		AggregateIterable<Document> findIterable = collection.aggregate(cond);
		List<Document> results = new ArrayList<Document>();
		for (Document doc : findIterable) {
			results.add(doc);
		}
		return new Document("res", results);
	}
}
