package api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;

import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class Index {
	private static Logger logger = LogManager.getLogger(Index.class.getName()); // 日志
	
	/**
	 * 创建索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @param obj 创建对象
	 * @return 索引名
	 */
	public static String createIndex(String user_id,String dbName,String collectionName
			,Document obj) {
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document("obj",obj);
		params.append("options", null);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"createIndex",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		String result = document.getString("res");
		return result;
	}
	/**
	 * 创建索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @param obj 创建对象
	 * @param options 特定参数
	 * @return 索引名
	 */
	public static String createIndex(String user_id,String dbName,String collectionName
			,Document obj,IndexOptions options) {
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document("obj",obj);
		params.append("options",options==null ? null:exchange(options));
		Task task=new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"createIndex",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		String result = document.getString("res");
		return result;
	}
	/**
	 * 创建多个索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @param obj 索引对象序列
	 * @return 索引名序列
	 */
	@SuppressWarnings("unchecked")
	public static List<String> createIndexes(String user_id,String dbName,String collectionName
			,List<IndexModel> obj)
	{
		long taskID = Client.taskID;
		Client.taskID++;
		List<Document> ld = new ArrayList<Document>();
		for(IndexModel im:obj)
		{
			ld.add(new Document("Keys",im.getKeys())
					.append("Options", exchange(im.getOptions())));
		}
		Document params = new Document("obj",ld);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"createIndexes",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		List<String> result = (List<String>) document.get("res");
		return result;
	}
	/**
	 * 列出所有索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @return 索引信息序列
	 */
	@SuppressWarnings("unchecked")
	public static List<Document> listIndexes(String user_id,String dbName,String collectionName)
	{
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document();
		Task task=new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"listIndexes",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		List<Document> result = (List<Document>) document.get("res");
		return result;
	}
	/**
	 * 删除表中所有索引
	 * @param user_id
	 * @param dbName
	 * @param collectionName
	 * @return 是否成功
	 */
	public static boolean dropIndexes(String user_id,String dbName,String collectionName)
	{
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document();
		Task task=new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"dropIndexes",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		boolean result = document.getBoolean("res");
		return result;
	}
	/**
	 * 删除索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @param obj 删除索引的属性
	 * @return 是否成功
	 */
	public static boolean dropIndex(String user_id,String dbName,String collectionName,Document obj)
	{
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document("obj",obj);
		Task task=new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"dropIndexB",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		boolean result = document.getBoolean("res");
		return result;
	}
	/**
	 * 删除索引
	 * @param user_id 用户id
	 * @param dbName 数据库名
	 * @param collectionName 表名
	 * @param obj 删除索引名
	 * @return 是否成功
	 */
	public static boolean dropIndex(String user_id,String dbName,String collectionName,String obj)
	{
		long taskID = Client.taskID;
		Client.taskID++;
		Document params = new Document("obj",obj);
		Task task=new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(),user_id,"dropIndexS",dbName,collectionName,params,Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		boolean result = document.getBoolean("res");
		return result;
	}
	public static Document exchange(IndexOptions options)
	{
		Document doc = new Document("DefaultLanguage",options.getDefaultLanguage());
		doc.append("Bits",options.getBits());
		doc.append("BucketSize",options.getBucketSize());
		doc.append("LanguageOverride",options.getLanguageOverride());
		doc.append("Max",options.getMax());
		doc.append("Min",options.getMin());
		doc.append("Name",options.getName());
		doc.append("PartialFilterExpression",options.getPartialFilterExpression());
		doc.append("SphereVersion",options.getSphereVersion());
		doc.append("StorageEngine",options.getStorageEngine());
		doc.append("TextVersion",options.getTextVersion());
		doc.append("Version",options.getVersion());
		doc.append("Weights",options.getWeights());
		doc.append("Background",options.isBackground());
		doc.append("Sparse",options.isSparse());
		doc.append("Unique",options.isUnique());
		return doc;
	}

}
