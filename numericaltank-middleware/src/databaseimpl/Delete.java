package databaseimpl;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.result.DeleteResult;

public class Delete {
	public static Document delete(MongoCollection<Document> collection, Document filter) {
		DeleteResult result = collection.deleteOne(filter);
		Document res = new Document("DeletedCount",result.getDeletedCount());
		res.append("Acknowledged",result.wasAcknowledged());
		return new Document("res",res);
	}

	public static Document deleteMany(MongoCollection<Document> collection, Document filter) {
		DeleteResult result = collection.deleteMany(filter);
		Document res = new Document("DeletedCount",result.getDeletedCount());
		res.append("Acknowledged",result.wasAcknowledged());
		return new Document("res",res);
	}

	public static Document findOneAndDelete(MongoCollection<Document> collection, Document filter,
			Document options) {
		Document result = (Document) collection.findOneAndDelete(filter,
				options == null ? new FindOneAndDeleteOptions() : 
					new FindOneAndDeleteOptions()
					.projection((Document)options.get("Projection"))
					.sort((Document)options.get("Sort")));
		return new Document("res", result);
	}
}
