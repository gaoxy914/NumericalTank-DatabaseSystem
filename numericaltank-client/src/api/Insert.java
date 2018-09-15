package api;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class Insert {
	private static Logger logger = LogManager.getLogger(Insert.class.getName()); // 日志
	
	public static boolean insertOne(String user_id, String dbName, String collectionName, Document obj) {
		long taskID = Client.getTaskID();
		Document Params = new Document("obj", obj);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "insert", dbName, collectionName, Params, Constants.UNDO);
		Client.taskList.add(task);
		boolean send = util.send(task);
		if (!send) {
			Client.needReSend = true;
		} else {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		Client.taskList.remove(task);
		task.setResult(Constants.DONE);
		util.send(task);
		boolean result = document.getBoolean("res");
		return result;
	}

	public static boolean insertMany(String user_id, String dbName, String collectionName, List<Document> obj) {
		long taskID = Client.getTaskID();
		Document Params = new Document("obj", obj);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "insertMany", dbName, collectionName, Params, Constants.UNDO);
		Client.taskList.add(task);
		boolean send = util.send(task);
		if (!send) {
			Client.needReSend = true;
		} else {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		Client.taskList.remove(task);
		task.setResult(Constants.DONE);
		util.send(task);
		boolean result = document.getBoolean("res");
		return result;
	}
}
