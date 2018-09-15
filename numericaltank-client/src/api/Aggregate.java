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

public class Aggregate {
	private static Logger logger = LogManager.getLogger(Aggregate.class.getName()); // 日志
	
	public static List<Document> aggregate(String user_id, String dbName, String collectionName, List<Document> cond) {
		long taskID = Client.getTaskID();
		Document params = new Document("cond", cond);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "aggregate", dbName, collectionName, params, Constants.UNDO);
		boolean send = util.send(task);
		if (send) {
			logger.info("发送任务: " + task.toString());
		}
		Document document = util.getResult(taskID);
		logger.info("收到结果: " + task.toString());
		task.setResult(Constants.DONE);
		util.send(task);
		@SuppressWarnings("unchecked")
		List<Document> list = (List<Document>) document.get("res");
		return list;
	}
}
