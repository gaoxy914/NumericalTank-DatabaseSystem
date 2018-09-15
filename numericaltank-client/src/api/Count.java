package api;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class Count {
	private static Logger logger = LogManager.getLogger(Count.class.getName()); // 日志
	
	public static long count(String user_id, String dbName, String collectionName, Document filter) {
		long taskID = Client.getTaskID();
		Document params = new Document("filter", filter);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "count", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		return document.getLong("res");
	}
}
