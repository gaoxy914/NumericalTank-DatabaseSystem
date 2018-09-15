package api;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class Update {
	private static Logger logger = LogManager.getLogger(Update.class.getName()); // 日志
	
	public static Document findOneAndUpdate(String user_id, String dbName, String collectionName, Document filter,
			Document obj) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		params.append("obj", obj);
		params.append("options", null);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "findOneAndUpdate",dbName, collectionName, params, Constants.UNDO);
		Client.taskList.add(task);;
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
		Document result = (Document) document.get("res");
		return result;
	}

	public static Document findOneAndUpdate(String user_id, String dbName, String collectionName, Document filter,
			Document obj, FindOneAndUpdateOptions options) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		params.append("obj", obj);
		Document exoptions = options == null ? null:new Document("Projection",options.getProjection())
				.append("Sort", options.getSort())
				.append("ReturnDocument", options.getReturnDocument().equals(ReturnDocument.AFTER) ? true:false)
				.append("Upsert",options.isUpsert())
				.append("BypassDocumentValidation", options.getBypassDocumentValidation());
		params.append("options", exoptions);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "findOneAndUpdate", dbName, collectionName, params, Constants.UNDO);
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
		Document result = (Document) document.get("res");
		return result;
	}

	public static Document updateMany(String user_id, String dbName, String collectionName, Document filter, Document obj) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		params.append("obj", obj);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "updateMany", dbName, collectionName, params, Constants.UNDO);
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
		Document result = (Document) document.get("res");
		return result;
	}

	public static Document updateOne(String user_id, String dbName, String collectionName, Document filter,
			Document obj) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		params.append("obj", obj);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "update", dbName, collectionName, params, Constants.UNDO);
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
		Document result = (Document) document.get("res");
		return result;
	}
}
