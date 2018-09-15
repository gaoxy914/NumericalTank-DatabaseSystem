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

public class Find {
	private static Logger logger = LogManager.getLogger(Find.class.getName()); // 日志

	public static List<Document> find(String user_id, String dbName, String collectionName) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", null);
		params.append("limit", 0);
		params.append("skip", 0);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "find", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		@SuppressWarnings("unchecked")
		List<Document> result = (List<Document>) document.get("res");
		return result;
	}

	public static List<Document> find(String user_id, String dbName, String collectionName, Document filter,
			Document order, Document proj) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter == null ? new Document() : filter);
		params.append("order", order).append("proj", proj);
		params.append("limit", 0);
		params.append("skip", 0);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "find", dbName, collectionName, params, Constants.UNDO);;
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		@SuppressWarnings("unchecked")
		List<Document> result = (List<Document>) document.get("res");
		return result;
	}

	public static List<Document> find(String user_id, String dbName, String collectionName, Document filter,
			Document order, Document proj, Integer limit, Integer skip) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter == null ? new Document() : filter);
		params.append("order", order).append("proj", proj);
		params.append("limit", limit == null ? 0 : limit);
		params.append("skip", skip == null ? 0 : skip);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "find", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		@SuppressWarnings("unchecked")
		List<Document> result = (List<Document>) document.get("res");
		return result;
	}

}
