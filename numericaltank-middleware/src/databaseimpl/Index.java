package databaseimpl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;

public class Index {
	public static Document createIndex(MongoCollection<Document> collection, Document obj,
			Document options) {
		String result = collection.createIndex(obj, options == null ? new IndexOptions() : exchange(options));
		return new Document("res", result);
	}
	public static Document createIndexes(MongoCollection<Document> collection,
			List<Document> obj) {
		List<IndexModel> lim = new ArrayList<IndexModel>();
		for(Document doc:obj)
		{
			lim.add(new IndexModel((Document)doc.get("Keys"),exchange((Document)doc.get("Options"))));
		}
		return new Document("res", collection.createIndexes(lim));
	}

	public static Document listIndexes(MongoCollection<Document> collection) {
		ListIndexesIterable<Document> listIndexesIterable = collection.listIndexes();
		List<Document> results = new ArrayList<Document>();
		for (Document doc : listIndexesIterable) {
			results.add(doc);
		}
		return new Document("res", results);
	}

	public static Document dropIndexB(MongoCollection<Document> collection, Document obj) {
		collection.dropIndex(obj);
		return new Document("res", true);
	}

	public static Document dropIndexS(MongoCollection<Document> collection, String obj) {
		collection.dropIndex(obj);
		return new Document("res", true);
	}

	public static Document dropIndexes(MongoCollection<Document> collection) {
		collection.dropIndexes();
		return new Document("res", true);
	}
	public static IndexOptions exchange(Document options)
	{
		IndexOptions io = new IndexOptions();
		io.background(options.getBoolean("Background"));
		io.sparse(options.getBoolean("Sparse"));
		io.unique(options.getBoolean("Unique"));
		io.bits(options.getInteger("Bits"));
		io.bucketSize(options.getDouble("BucketSize"));
		io.defaultLanguage(options.getString("DefaultLanguage"));
		io.languageOverride(options.getString("LanguageOverride"));
		io.max(options.getDouble("Max"));
		io.min(options.getDouble("Min"));
		io.name(options.getString("Name"));
		io.partialFilterExpression((Document)options.get("PartialFilterExpression"));
		io.sphereVersion(options.getInteger("SphereVersion"));
		io.storageEngine((Document)options.get("StorageEngine"));
		io.textVersion(options.getInteger("TextVersion"));
		io.version(options.getInteger("Version"));
		io.weights((Document)options.get("Weights"));
		return io;
	}
}
