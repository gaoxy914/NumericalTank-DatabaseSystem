package api;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import client.Client;
import conf.Config;
import conf.Constants;
import object.Task;

public class File {
	private static Logger logger = LogManager.getLogger(File.class.getName()); // 日志
	
	public static boolean exportFile(String user_id, String dbName, String collectionName
			,Document filter,String proj,Document order,Integer limit,Integer skip
			,String type,String filename)
	{
		long taskID = Client.getTaskID();
		Document params = new Document("filter",filter);
		params.append("proj", proj);
		params.append("order", order);
		params.append("limit", limit);
		params.append("skip", skip);
		params.append("type", type);
		params.append("filename",filename);
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "exportFile", dbName, collectionName, params, Constants.UNDO);
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

	public static boolean importFile(String user_id,String dbName,String collectionName,
			String fields,boolean ignoreblanks,boolean headerline,boolean drop,String mode,String upsertfields
			,String filepath,String type) {
		long taskID = Client.getTaskID();
		Document params = new Document("fields",fields);
		params.append("ignoreblanks", ignoreblanks);
		params.append("headerline", headerline);
		params.append("drop", drop);
		params.append("mode", mode);
		params.append("upsertfields", upsertfields);
		params.append("filepath", filepath);
		params.append("type", type);	
		Task task = new Task(Config.localIP, Config.serverPort, Config.fileServerPort, taskID, new Date().toString(), user_id, "importFile", dbName, collectionName, params, Constants.UNDO);
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
}
