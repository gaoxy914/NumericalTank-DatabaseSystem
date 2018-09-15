package api;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.model.FindOneAndDeleteOptions;

import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class Delete {
	private static Logger logger = LogManager.getLogger(Delete.class.getName()); // 日志
	
	public static Document deleteMany(String user_id, String dbName, String collectionName, Document filter) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "deleteMany", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		Document result = (Document) document.get("res");
		return result;
	}

	public static Document deleteOne(String user_id, String dbName, String collectionName, Document filter) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "delete", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		Document result = (Document) document.get("res");
		return result;
	}

	public static Document findOneAndDelete(String user_id, String dbName, String collectionName, Document filter,
			FindOneAndDeleteOptions options) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		Document exoptions = options == null ? null:new Document("Projection",options.getProjection())
				.append("Sort", options.getSort());
		params.append("options", exoptions);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "findOneAndDelete", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		Document result = (Document) document.get("res");
		return result;
	}
}
