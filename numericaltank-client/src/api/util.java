package api;

import org.bson.Document;

import client.Client;
import conf.Config;
import object.Task;
import service.ClientService;

public class util {
	public static boolean send(Task task) {
		boolean send = false;
		ClientService clientService = null;
		if (Client.client2MiddleWarePool.size() > 0) {
			for (String ip : Config.middleWareIP) {
				clientService = Client.client2MiddleWarePool.get(ip);
				if (clientService.connected()) {
					clientService.send(task.serialize());
					send = true;
				}
			}
		}
		return send;
	}
	
	public static Document getResult(long taskID) {
		while (true) {
			if (Client.result.containsKey(taskID)) {
				Document result = Client.result.remove(taskID);
				return result;
			}
		}
	}
}
