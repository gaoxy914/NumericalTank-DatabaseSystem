package databaseimpl;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class Find {
	public static FindIterable<Document> find(MongoCollection<Document> collection, Document filter, Document order, Document proj,
			Integer limit, Integer skip) {
		FindIterable<Document> findIterable;
		findIterable = collection.find(filter == null ? new Document() : filter).projection(proj).sort(order)
				.limit(limit == null ? 0 : limit).skip(skip == null ? 0 : skip);
		return findIterable;
	}
}
