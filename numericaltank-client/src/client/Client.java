package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;

import api.Aggregate;
import api.Count;
import api.Delete;
import api.File;
import api.Find;
import api.Index;
import api.Insert;
import api.Update;
import conf.Config;
import conf.Constants;
import object.Task;
import service.ClientService;
import service.FileSender;
import service.FileServer;
import service.ServerService;

/**
 * Client
 * @author 高翔宇
 *
 */
public class Client {
	private ServerService serverService = new ServerService(); // TCP通信接收端
	private FileServer fileServer = new FileServer(); //　文件接收端
	public static Hashtable<String, FileSender> fileSenders = new Hashtable<String, FileSender>(); // 文件发送端
	public static volatile Hashtable<String, ClientService> client2MiddleWarePool = new Hashtable<String, ClientService>(); // TCP通信发送端
	public static volatile Hashtable<Long, Document> result = new Hashtable<Long, Document>(); // 结果
	public static volatile List<Task> taskList = new ArrayList<Task>(); // 任务队列
	public static volatile boolean needReSend = false; // 重发标志位
	public static long taskID = 0; //　任务ID
	
	/**
	 * Client声明，对应中间件系统IP验证与无需验证方式
	 * @param configPath 配置文件路径
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Client(String configPath) throws IOException, InterruptedException {
		new Config(configPath);
		serverService.start();
		fileServer.start();
		ClientService clientService = null;
		FileSender fileSender = null;
		for (String ip : Config.middleWareIP) {
			clientService = new ClientService();
			clientService.start(ip, Config.mwServerPort);
			clientService.send(String.valueOf(Config.serverPort));
			client2MiddleWarePool.put(ip, clientService);
			fileSender = new FileSender();
			fileSender.start(ip, Config.mwFileServerPort);
			fileSenders.put(ip, fileSender);
		}
	}
	
	/**
	 * Client声明，对应中间件系统用户密码验证方式
	 * @param configPath 配置文件路径
	 * @param user 用户名
	 * @param password 密码
	 * @throws IOException
	 */
	public Client(String configPath, String user, String password) throws IOException {
		new Config(configPath);
		serverService.start();
		fileServer.start();
		ClientService clientService = null;
		FileSender fileSender = null;
		for (String ip : Config.middleWareIP) {
			clientService = new ClientService();
			clientService.start(ip, Config.mwServerPort);
			clientService.send(user + " " + password + " " + String.valueOf(Config.serverPort));
			client2MiddleWarePool.put(ip, clientService);
			fileSender = new FileSender();
			fileSender.start(ip, Config.mwFileServerPort);
			fileSenders.put(ip, fileSender);
		}
	}
	
	/**
	 * 关闭Client
	 */
	public void close() {
		serverService.close();
		fileServer.close();
		for (ClientService clientService : client2MiddleWarePool.values()) {
			clientService.close();
		}
		for (FileSender fileSender : fileSenders.values()) {
			fileSender.close();
		}
	}
	
	/**
	 * 获取任务ID
	 * @return 任务ID
	 */
	public static synchronized long getTaskID() {
		long taskID = Client.taskID;
		Client.taskID++;
		if (Client.taskID > Constants.MAXIMUM) {
			Client.taskID = 0;
		}
		return taskID;
	}
	
	public List<Document> aggregate(String user_id, String dbName, String collectionName, List<Document> cond) {
		return Aggregate.aggregate(user_id, dbName, collectionName, cond);
	}
	
	public long count(String user_id, String dbName, String collectionName, Document filter) {
		return Count.count(user_id, dbName, collectionName, filter);
	}
	
	public Document findOneAndUpdate(String user_id, String dbName, String collectionName, Document filter,
			Document obj) {
		return Update.findOneAndUpdate(user_id, dbName, collectionName, filter, obj);
	}

	public Document findOneAndUpdate(String user_id, String dbName, String collectionName, Document filter,
			Document obj, FindOneAndUpdateOptions options) {
		return Update.findOneAndUpdate(user_id, dbName, collectionName, filter, obj, options);
	}

	public Document updateMany(String user_id, String dbName, String collectionName, Document filter, Document obj) {
		return Update.updateMany(user_id, dbName, collectionName, filter, obj);
	}

	public Document updateOne(String user_id, String dbName, String collectionName, Document filter,
			Document obj) {
		return Update.updateOne(user_id, dbName, collectionName, filter, obj);
	}
	
	public boolean insertOne(String user_id, String dbName, String collectionName, Document obj) {
		return Insert.insertOne(user_id, dbName, collectionName, obj);
	}

	public boolean insertMany(String user_id, String dbName, String collectionName, List<Document> obj) {
		return Insert.insertMany(user_id, dbName, collectionName, obj);
	}
	
	public String createIndex(String user_id,String dbName,String collectionName
			,Document obj) {
		return Index.createIndex(user_id, dbName, collectionName, obj);
	}
	
	public String createIndex(String user_id,String dbName,String collectionName
			,Document obj,IndexOptions options) {
		return Index.createIndex(user_id, dbName, collectionName, obj, options);
	}
	
	public List<String> createIndexes(String user_id,String dbName,String collectionName
			,List<IndexModel> obj)
	{
		return Index.createIndexes(user_id, dbName, collectionName, obj);
	}
	
	public List<Document> listIndexes(String user_id,String dbName,String collectionName)
	{
		return Index.listIndexes(user_id, dbName, collectionName);
	}
	
	public boolean dropIndexes(String user_id,String dbName,String collectionName)
	{
		return Index.dropIndexes(user_id, dbName, collectionName);
	}
	
	public boolean dropIndex(String user_id,String dbName,String collectionName,Document obj)
	{
		return Index.dropIndex(user_id, dbName, collectionName, obj);
	}
	
	public boolean dropIndex(String user_id,String dbName,String collectionName,String obj)
	{
		return Index.dropIndex(user_id, dbName, collectionName, obj);
	}
	
	public boolean exportFile(String user_id, String dbName, String collectionName
			,Document filter,String proj,Document order,Integer limit,Integer skip
			,String type,String filename)
	{
		return File.exportFile(user_id, dbName, collectionName, filter, proj, order, limit, skip, type, filename);
	}

	public boolean importFile(String user_id,String dbName,String collectionName,
			String fields,boolean ignoreblanks,boolean headerline,boolean drop,String mode,String upsertfields
			,String filepath,String type) {
		return File.importFile(user_id, dbName, collectionName, fields, ignoreblanks, headerline, drop, mode, upsertfields, filepath, type);
	}
	
	public Document deleteMany(String user_id, String dbName, String collectionName, Document filter) {
		return Delete.deleteMany(user_id, dbName, collectionName, filter);
	}

	public Document deleteOne(String user_id, String dbName, String collectionName, Document filter) {
		return Delete.deleteOne(user_id, dbName, collectionName, filter);
	}

	public Document findOneAndDelete(String user_id, String dbName, String collectionName, Document filter,
			FindOneAndDeleteOptions options) {
		return Delete.findOneAndDelete(user_id, dbName, collectionName, filter, options);
	}
	
	public List<Document> find(String user_id, String dbName, String collectionName) {
		return Find.find(user_id, dbName, collectionName);
	}

	public List<Document> find(String user_id, String dbName, String collectionName, Document filter,
			Document order, Document proj) {
		return Find.find(user_id, dbName, collectionName, filter, order, proj);
	}

	public List<Document> find(String user_id, String dbName, String collectionName, Document filter,
			Document order, Document proj, Integer limit, Integer skip) {
		return Find.find(user_id, dbName, collectionName, filter, order, proj, limit, skip);
	}
}
