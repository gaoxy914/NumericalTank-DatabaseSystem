package databaseimpl;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;

public class Update {
	public static Document update(MongoCollection<Document> collection, Document filter, Document obj) {
		UpdateResult result = collection.updateOne(filter, obj);
		Document res = new Document();
		res.append("MatchedCount",result.getMatchedCount());
		res.append("ModifiedCount",result.getModifiedCount());
		res.append("Acknowledged",result.wasAcknowledged());
		return new Document("res",res);
	}

	public static Document updateMany(MongoCollection<Document> collection, Document filter, Document obj) {
		UpdateResult result = collection.updateMany(filter, obj);
		Document res = new Document();
		res.append("MatchedCount",result.getMatchedCount());
		res.append("ModifiedCount",result.getModifiedCount());
		res.append("Acknowledged",result.wasAcknowledged());
		return new Document("res",res);
	}
	
	public static Document findOneAndUpdate(MongoCollection<Document> collection, Document filter, Document obj,
			Document options) {
		Document result = (Document) collection.findOneAndUpdate(filter, obj,
				options == null ? new FindOneAndUpdateOptions() : new FindOneAndUpdateOptions()
						.projection((Document)options.get("Projection"))
						.sort((Document)options.get("Sort"))
						.bypassDocumentValidation(options.getBoolean("BypassDocumentValidation"))
						.upsert(options.getBoolean("Upsert"))
						.returnDocument(options.getBoolean("ReturnDocument")? ReturnDocument.AFTER:ReturnDocument.BEFORE));
		return new Document("res", result);
	}
}
