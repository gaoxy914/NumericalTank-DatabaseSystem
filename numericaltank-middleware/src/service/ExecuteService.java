package service;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import conf.Config;
import conf.Constants;
import databaseimpl.Aggregate;
import databaseimpl.Count;
import databaseimpl.Delete;
import databaseimpl.File;
import databaseimpl.Find;
import databaseimpl.Index;
import databaseimpl.Insert;
import databaseimpl.Update;
import middleware.MiddleWare;
import mongolog.DBStatus;
import mongolog.LocalLog;
import mongolog.SysLog;
import object.Result;
import object.Task;

/**
 * ExecuteService
 * @author 高翔宇
 *
 */
public class ExecuteService {
	private volatile boolean running = false; // service running status
	private Thread checkTaskListThread; // check task list and thread pool
	private ExecutorService fixedThreadPool; // thread pool
	private static MongoClient mongoClient; // MongDB connection pool
	private String mongouser; // mongodb用户名
	private String password; // mongodb验证密码
	private Logger mwLogger = LogManager.getLogger("middleware"); // middleware
																	// log
	/**
	 * 启动ExecuteService
	 * @param user mongodb用户名
	 * @param password mongodb验证密码
	 */
	public void start(String user, String password) {
		this.mongouser = user;
		this.password = password;
		mwLogger.info("启动任务监测线程");
		if (running) {
			return;
		}
		running = true;
		fixedThreadPool = Executors.newFixedThreadPool(Config.threadPoolSize);
		List<ServerAddress> addresslist = new ArrayList<ServerAddress>();
		ServerAddress serverAddress = null;
		for (String ip : Config.mongosIP) {
			serverAddress = new ServerAddress(ip, Config.mongosPort);
			addresslist.add(serverAddress);
		}
		MongoCredential credential = MongoCredential.createCredential(this.mongouser, "admin",
				this.password.toCharArray());
		mongoClient = new MongoClient(addresslist, Arrays.asList(credential));
		checkTaskListThread = new Thread(new CheckTaskListThread());
		checkTaskListThread.start();
	}
	
	/**
	 * 关闭ExecuteService
	 */
	public void clsoe() {
		mwLogger.info("关闭任务监测线程");
		if (running) {
			running = false;
		}
		if (mongoClient != null) {
			mongoClient.close();
		}
		if (fixedThreadPool != null) {
			fixedThreadPool.shutdown();
		}
	}
	
	/**
	 * 登录验证（Client与CMD）
	 * @param name 用户名
	 * @param password 密码
	 * @return
	 */
	public static double validate(String name, String password) {
		MongoCollection<Document> user = mongoClient.getDatabase("user").getCollection("user");
		FindIterable<Document> findIterable = user.find(new Document("_id", name).append("password", password));
		for (Document doc : findIterable) {
			// System.out.println(doc.getInteger("rank"));
			return doc.getDouble("rank");
		}
		return 0;
		// return 1;

	}

	/**
	 * CMD命令执行
	 * @param command 命令
	 * @param rank 权限
	 * @return 执行结果
	 */
	public static Document runCommand(String command, double rank) {
		MongoDatabase admin = mongoClient.getDatabase("admin");
		MongoCollection<Document> locallog = mongoClient.getDatabase("logdb").getCollection("locallog");
		MongoCollection<Document> syslog = mongoClient.getDatabase("project").getCollection("system.profile");
		String[] args = command.split(" ");
		if (args.length == 1) {
			switch (args[0]) {
			case "dbStatus":
				List<Document> dbslist = new ArrayList<Document>();
				if (rank == 1) {
					dbslist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", dbslist);
				}
				return new Document("res", DBStatus.showStatus(admin));
			case "localLog.countByDB":
				return new Document("res", LocalLog.countByDB(locallog));
			case "localLog.trendByYear":
				return new Document("res", LocalLog.trendByYear(locallog));
			case "localLog.trendByWeek":
				return new Document("res", LocalLog.trendByWeek(locallog));
			case "localLog.trendByMonth":
				return new Document("res", LocalLog.trendByMonth(locallog));
			case "middleWareStatus":
				List<Document> mwList = new ArrayList<Document>();
				if (rank == 1) {
					mwList.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", mwList);
				}
				mwList.add(new Document("middlewarestatus", MiddleWare.middleWareStatus.toString()));
				return new Document("res", mwList);
			case "stopMiddleWare":
				List<Document> endList = new ArrayList<Document>();
				if (rank == 1) {
					endList.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", endList);
				}
				MiddleWare.endFlag = true;
				endList.add(new Document(args[0], "OK"));
				return new Document("res", endList);
			case "memoryStatus":
				List<Document> list = new ArrayList<Document>();
				if (rank == 1) {
					list.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", list);
				}
				list.add(new Document("totalMem:", Runtime.getRuntime().totalMemory() / 1024 / 1024 + "M")
						.append("usedMem:", Runtime.getRuntime().freeMemory() / 1024 / 1024 + "M").append("freeMem:",
								(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024
										+ "M"));
				return new Document("res", list);
			case "currentOp":
				List<Document> currentlist = new ArrayList<Document>();
				if (rank == 1) {
					currentlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", currentlist);
				}
				return new Document("res", DBStatus.currentOp(admin, null));
			case "listDatabases":
				List<Document> lblist = new ArrayList<Document>();
				if (rank == 1) {
					lblist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", lblist);
				}
				return new Document("res", DBStatus.listDatabases(admin));
			case "connectionStatus":
				List<Document> connlist = new ArrayList<Document>();
				if (rank == 1) {
					connlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", connlist);
				}
				return new Document("res", DBStatus.connectionStatus(admin));
			case "listShards":
				List<Document> lslist = new ArrayList<Document>();
				if (rank == 1) {
					lslist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", lslist);
				}
				return new Document("res", DBStatus.listShards(admin));
			case "nodeStatus":
				List<Document> nslist = new ArrayList<Document>();
				if (rank == 1) {
					nslist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", nslist);
				}
				return new Document("res", DBStatus.nodeStatus(admin));
			case "allUsers":
				List<Document> userlist = new ArrayList<Document>();
				if (rank == 1) {
					userlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", userlist);
				}
				MongoCollection<Document> user = mongoClient.getDatabase("user").getCollection("user");
				FindIterable<Document> findIterable = user.find();
				for (Document document : findIterable) {
					userlist.add(document);
				}
				return new Document("res", userlist);
			case "listClients":
				List<Document> clientList = new ArrayList<Document>();
				for (String ip : Config.clientIP) {
					clientList.add(new Document("clientIP", ip));
				}
				return new Document("res", clientList);
			case "listBackUpMiddleWares":
				List<Document> ipList = new ArrayList<Document>();
				for (String ip : Config.backupMiddleWareIP) {
					ipList.add(new Document("backupMiddleWareIP", ip));
				}
				return new Document("res", ipList);
			case "connectClient":
				List<Document> result = new ArrayList<Document>();
				if (rank == 1) {
					result.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", result);
				}
//				System.out.println(Config.activeClient.toString());
				for (String client : Config.activeClient) {
//					System.out.println(client);
					int localPort = Config.getClientPort();
//					System.out.println(localPort);
					if (localPort > 0) {
						Socket sender;
						try {
							String[] tmp = client.split("&");
//							System.out.println(tmp[0] + " " + tmp[1]);
							sender = new Socket(tmp[0], Integer.valueOf(tmp[1]), null, localPort);
							MiddleWare.clientResultSender.insert(tmp[0], Integer.valueOf(tmp[1]), sender);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						result.add(new Document(args[0], "Client连接数目达到上限，没有足够的开放端口。"));
					}
				}
				return new Document("res", result);
			default:
				List<Document> errorlist = new ArrayList<Document>();
				errorlist.add(new Document(args[0], "Error command. Please check and rewrite."));
				return new Document("res", errorlist);
			}
		} else if (args.length == 2) {
			switch (args[0]) {
			case "currentOp":
				List<Document> currentlist = new ArrayList<Document>();
				if (rank == 1) {
					currentlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", currentlist);
				}
				return new Document("res", DBStatus.currentOp(admin, args[1]));
			case "localLog.show":
				return new Document("res", LocalLog.show(locallog, Integer.parseInt(args[1])));
			case "localLog.errorOp":
				return new Document("res", LocalLog.errorOp(locallog, Integer.parseInt(args[1])));
			case "sysLog.showByTs":
				return new Document("res", SysLog.showByTs(syslog, Integer.parseInt(args[1])));
			case "sysLog.showByMillis":
				return new Document("res", SysLog.showByMillis(syslog, Integer.parseInt(args[1])));
			case "localLog.userOp":
				return new Document("res", LocalLog.userOp(locallog, args[1]));
			case "localLog.userOpCount":
				return new Document("res", LocalLog.userOpCount(locallog, args[1]));
			case "addBackUpMiddleWare":
				List<Document> ipList = new ArrayList<Document>();
				if (rank == 1) {
					ipList.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", ipList);
				}
				Config.backupMiddleWareIP.add(args[1]);
				for (String ip : Config.backupMiddleWareIP) {
					ipList.add(new Document("backupMiddleWareIP", ip));
				}
				try {
					List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
					for (int i = 0; i < lines.size(); i++) {
						String[] word = lines.get(i).split(":");
						if (word[0].indexOf(Config.BACKUP_MIDDLEWARE_IP) != -1) {
							String newline = lines.remove(i) + ";" + args[1];
							lines.add(i, newline);
							break;
						}
					}
					Files.write(Paths.get("Config.txt"), lines);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new Document("res", ipList);
			case "addClient":
				List<Document> clientList = new ArrayList<Document>();
				if (rank == 1) {
					clientList.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", clientList);
				}
				Config.clientIP.add(args[1]);
				for (String ip : Config.clientIP) {
					clientList.add(new Document("clientIP", ip));
				}
				try {
					List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
					for (int i = 0; i < lines.size(); i++) {
						String[] word = lines.get(i).split(":");
						if (word[0].indexOf(Config.CLIENT_IP) != -1) {
							String newline = lines.remove(i) + ";" + args[1];
							lines.add(i, newline);
							break;
						}
					}
					Files.write(Paths.get("Config.txt"), lines);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new Document("res", clientList);
			case "delUser":
				List<Document> userlist = new ArrayList<Document>();
				if (rank == 1) {
					userlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", userlist);
				}
				MongoCollection<Document> user = mongoClient.getDatabase("user").getCollection("user");
				user.deleteOne(new Document("_id", args[1]));
				userlist.add(new Document(args[0], "OK"));
				return new Document("res", userlist);
			case "addReceiverEmailAccount":
				List<Document> emaillist = new ArrayList<Document>();
				if (rank == 1) {
					emaillist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", emaillist);
				}
				Config.receiverEmailAccount.add(args[1]);
				for (String email : Config.receiverEmailAccount) {
					emaillist.add(new Document("receiverEmailAccount", email));
				}
				try {
					List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
					for (int i = 0; i < lines.size(); i++) {
						String[] word = lines.get(i).split(":");
						if (word[0].indexOf(Config.RECEIVER_EMAIL_ACCOUNT) != -1) {
							String newline = lines.remove(i) + ";" + args[1];
							lines.add(i, newline);
							break;
						}
					}
					Files.write(Paths.get("Config.txt"), lines);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new Document("res", emaillist);
			default:
				List<Document> errorlist = new ArrayList<Document>();
				errorlist.add(new Document(args[0], "Error command. Please check and rewrite."));
				return new Document("res", errorlist);
			}
		} else if (args.length == 3) {
			switch (args[0]) {
				case "connectClient":
					List<Document> result = new ArrayList<Document>();
					if (rank == 1) {
						result.add(new Document(args[0], "You can't excute the command without permission."));
						return new Document("res", result);
					}
					int localPort = Config.getClientPort();
					if (localPort > 0) {
						Socket sender;
						try {
//							System.out.println("connect client");
							sender = new Socket(args[1], Integer.valueOf(args[2]), null, localPort);
							MiddleWare.clientResultSender.insert(args[1], Integer.valueOf(args[2]), sender);
							result.add(new Document(args[0], "Insert sender (" + args[1] + ":" + args[2] + ")"));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						result.add(new Document(args[0], "Client连接数目达到上限，没有足够的开放端口。"));
					}
					return new Document("res", result);
				default:
					List<Document> errorlist = new ArrayList<Document>();
					errorlist.add(new Document(args[0], "Error command. Please check and rewrite."));
					return new Document("res", errorlist);
			}
		} else if (args.length == 4) {
			switch (args[0]) {
			case "addUser":
				List<Document> userlist = new ArrayList<Document>();
				if (rank == 1) {
					userlist.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", userlist);
				}
				MongoCollection<Document> user = mongoClient.getDatabase("user").getCollection("user");
				if (args[3].equals("root")) {
					user.insertOne(
							new Document("_id", args[1]).append("password", args[2]).append("rank", new Double(2)));
				} else if (args[3].equals("normal")) {
					user.insertOne(
							new Document("_id", args[1]).append("password", args[2]).append("rank", new Double(1)));
				} else {
					userlist.add(new Document(args[0], "failed").append("reason", "Incorrect authority."));
					return new Document("res", userlist);
				}
				userlist.add(new Document(args[0], "OK").append("username", args[1]).append("authority", args[3]));
				return new Document("res", userlist);
			case "changeSender":
				List<Document> sender = new ArrayList<Document>();
				if (rank == 1) {
					sender.add(new Document(args[0], "You can't excute the command without permission."));
					return new Document("res", sender);
				}
				Config.senderEmailAccount = args[1];
				Config.senderEmailPassword = args[2];
				Config.senderEmailSMTPHost = args[3];
				try {
					List<String> lines = Files.readAllLines(Paths.get("Config.txt"));
					for (int i = 0; i < lines.size(); i++) {
						String[] word = lines.get(i).split(":");
						if (word[0].indexOf(Config.SENDER_EMAIL_ACCOUNT) != -1) {
							lines.remove(i);
							String newline = Config.SENDER_EMAIL_ACCOUNT + ":" + Config.senderEmailAccount;
							lines.add(i, newline);
						} else if (word[0].indexOf(Config.SENDER_EMAIL_PASSWORD) != -1) {
							lines.remove(i);
							String newline = Config.SENDER_EMAIL_PASSWORD + ":" + Config.senderEmailPassword;
							lines.add(i, newline);
						} else if (word[0].indexOf(Config.SENDER_EMAIL_SMTPHOST) != -1) {
							lines.remove(i);
							String newline = Config.SENDER_EMAIL_SMTPHOST + ":" + Config.senderEmailSMTPHost;
							lines.add(i, newline);
						}
					}
					Files.write(Paths.get("Config.txt"), lines);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			default:
				List<Document> errorlist = new ArrayList<Document>();
				errorlist.add(new Document(args[0], "Error command. Please check and rewrite."));
				return new Document("res", errorlist);
			}
		}
		else {
			List<Document> errorlist = new ArrayList<Document>();
			;
			errorlist.add(new Document(args[0], "Error command. Please check and rewrite."));
			return new Document("res", errorlist);
		}
	}
	
	/**
	 * 检测任务队列线程
	 * @author 高翔宇
	 *
	 */
	class CheckTaskListThread implements Runnable {

		public void run() {
			while (running) {
				if (MiddleWare.middleWareStatus.isPrimary()) {
					if (((ThreadPoolExecutor) fixedThreadPool).getActiveCount() < Config.threadPoolSize
							&& MiddleWare.middleWareStatus.getTaskQueueSize() > 0) { // 线程池执行
						try {
							Task task = MiddleWare.middleWareStatus.getTask();
							MongoDatabase mongoDatabase = mongoClient.getDatabase(task.getDb());
							MongoCollection<Document> mongoCollection = mongoDatabase
									.getCollection(task.getCollection());
							fixedThreadPool.execute(new ExecuteThread(mongoCollection, task));
							mwLogger.info("执行任务：" + task.toString());
							MiddleWare.middleWareStatus.add(task.getUser(), task.getFunc());
						} catch (InterruptedException e) {
							System.out.println("ERROR IN CHECKTASKLISTTHREAD.");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * 执行线程
	 * @author 高翔宇
	 *
	 */
	class ExecuteThread implements Runnable { // 线程池执行
		MongoCollection<Document> mongoCollection;
		Task task;

		public ExecuteThread(MongoCollection<Document> mongoCollection, Task task) {
			this.mongoCollection = mongoCollection;
			this.task = task;
		}

		public void run() {
			boolean flag = true;
			Result result = null;
			switch (task.getFunc()) {
			case "importFile":
				String filepath = task.getParams().getString("filepath");
				System.out.println("importFile Start");
				if(MiddleWare.fileSenders.containsKey(task.getIP()+":"+task.getFilePort())){
					MiddleWare.fileSenders.get(task.getIP()+":"+task.getFilePort()).sendMessage(filepath);
				}else {
					FileSender fileSender = new FileSender();
					System.out.println(task.getFilePort());
					int localPort = Config.getFilePort();
					if (localPort > 0) {
						fileSender.start(task.getIP(), task.getFilePort(), localPort);
						fileSender.sendMessage(filepath);
						MiddleWare.fileSenders.put(task.getIP()+":"+task.getFilePort(), fileSender);
					}
				}
				String filename = filepath.split("/")[filepath.split("/").length-1];
				java.io.File file = new java.io.File(Config.savePath+filename);
				while(!file.exists());
				Document result_im =File.importFile(task.getDb(), task.getCollection(),Config.mongosIP.get(0),Config.mongosPort,
						task.getParams().getString("fields"), task.getParams().getBoolean("ignoreblanks"),
						task.getParams().getBoolean("headerline"), task.getParams().getBoolean("drop"),
						task.getParams().getString("mode"), task.getParams().getString("upsertfields"),
						filename, task.getParams().getString("type"), mongouser, password);
				result = new Result(task.getTaskID(), result_im, Constants.END);
				break;
			case "exportFile":
				Document result_ex = File.exportFile(task.getDb(), task.getCollection(),Config.mongosIP.get(0),Config.mongosPort,
						(Document)task.getParams().get("filter"),task.getParams().getString("proj"), (Document)task.getParams().get("order")
						,task.getParams().getInteger("limit"), task.getParams().getInteger("skip")
						,task.getParams().getString("filename"), task.getParams().getString("type"), mongouser, password);
//				System.out.println(result_ex);
//				System.out.println(task.getParams().getString("filename"));
				if(MiddleWare.fileSenders.containsKey(task.getIP()+":"+task.getFilePort())){
					MiddleWare.fileSenders.get(task.getIP()+":"+task.getFilePort()).sendFile(task.getParams().getString("filename"));
				}else {
					FileSender fileSender = new FileSender();
					int localPort = Config.getFilePort();
					if (localPort > 0) {
						fileSender.start(task.getIP(), task.getFilePort(), localPort);
						fileSender.sendFile(task.getParams().getString("filename"));
						MiddleWare.fileSenders.put(task.getIP()+":"+task.getFilePort(), fileSender);
					}
				}
				result = new Result(task.getTaskID(), result_ex, Constants.END);
				break;
			case "aggregate":
				@SuppressWarnings("unchecked") 
				List<Document> cond_ag = (List<Document>) task.getParams().get("cond");
				Document result_ag = Aggregate.aggregate(mongoCollection, cond_ag);
				result = new Result(task.getTaskID(), result_ag, Constants.END);
				break;
			case "find":
				Document filter_fd = (Document) task.getParams().get("filter");
				Document order_fd = (Document) task.getParams().get("order");
				Document proj_fd = (Document) task.getParams().get("proj");
				Integer limit_fd = task.getParams().getInteger("limit");
				Integer skip_fd = task.getParams().getInteger("skip");
				FindIterable<Document> findIterable = Find.find(mongoCollection, filter_fd, order_fd, proj_fd, limit_fd, skip_fd);
				List<Document> result_fd = new ArrayList<Document>();
				flag = false;
				for (Document doc : findIterable) {
					flag = true;
					result_fd.add(doc);
					if (result_fd.size() > Constants.MAX_RETURN_SIZE-1) {
						result = new Result(task.getTaskID(), new Document("res", result_fd), Constants.NOT_END);
						MiddleWare.clientResultSender.sendData(task.getIP(), task.getPort(), result.serialize());
						result_fd.clear();
					}
				}
				result = new Result(task.getTaskID(), new Document("res", result_fd), Constants.END);
				break;
			case "update":
				Document filter_ud = (Document) task.getParams().get("filter");
				Document obj_ud = (Document) task.getParams().get("obj");
				Document result_ud = Update.update(mongoCollection, filter_ud, obj_ud);
				if(((Document)result_ud.get("res")).getLong("ModifiedCount")==0)
				{
					flag = false;
				}
				result = new Result(task.getTaskID(), result_ud, Constants.END);
				break;
			case "updateMany":
				Document filter_udM = (Document) task.getParams().get("filter");
				Document obj_udM = (Document) task.getParams().get("obj");
				Document result_udM = Update.update(mongoCollection, filter_udM, obj_udM);
				if(((Document)result_udM.get("res")).getLong("ModifiedCount")==0)
				{
					flag = false;
				}
				result = new Result(task.getTaskID(), result_udM, Constants.END);
				break;
			case "findOneAndUpdate":
				Document filter_fOAU = (Document) task.getParams().get("filter");
				Document obj_fOAU = (Document) task.getParams().get("obj");
				Document options_fOAU = (Document) task.getParams().get("options");
				Document result_fOAU = Update.findOneAndUpdate(mongoCollection, filter_fOAU, obj_fOAU, options_fOAU);
				if(result_fOAU.get("res")==null)
				{
					flag =false;
				}
				result = new Result(task.getTaskID(), result_fOAU, Constants.END);
				break;
			case "insert":
				Document obj_is = (Document) task.getParams().get("obj");
				Document result_is = Insert.insert(mongoCollection, obj_is);
				result = new Result(task.getTaskID(), result_is, Constants.END);
				break;
			case "insertMany":
				@SuppressWarnings("unchecked") 
				List<Document> obj_isM = (List<Document>) task.getParams().get("obj");
				Document result_isM = Insert.insertMany(mongoCollection, obj_isM);
				result = new Result(task.getTaskID(), result_isM, Constants.END);
				break;
			case "delete":
				Document filter_dl = (Document) task.getParams().get("filter");
				Document result_dl = Delete.delete(mongoCollection, filter_dl);
				if(((Document)result_dl.get("res")).getLong("DeletedCount")==0)
				{
					flag = false;
				}
				result = new Result(task.getTaskID(), result_dl, Constants.END);
				break;
			case "deleteMany":
				Document filter_dlM = (Document) task.getParams().get("filter");
				Document result_dlM = Delete.delete(mongoCollection, filter_dlM);
				if(((Document)result_dlM.get("res")).getLong("DeletedCount")==0)
				{
					flag = false;
				}
				result = new Result(task.getTaskID(), result_dlM, Constants.END);
				break;
			case "findOneAndDelete":
				Document filter_fOAD = (Document) task.getParams().get("filter");
				Document options_fOAD = (Document) task.getParams().get("options");
				Document result_fOAD = Delete.findOneAndDelete(mongoCollection, filter_fOAD, options_fOAD);
				if(result_fOAD.get("res")==null)
				{
					flag = false;
				}
				result = new Result(task.getTaskID(), result_fOAD, Constants.END);
				break;
			case "count":
				Document filter_co = (Document) task.getParams().get("filter");
				Document result_co = Count.count(mongoCollection, filter_co);
				result = new Result(task.getTaskID(), result_co, Constants.END);
				break;
			case "createIndex":
				Document obj_ci = (Document) task.getParams().get("obj");
				Document options_ci = (Document) task.getParams().get("options");
				Document result_ci = Index.createIndex(mongoCollection, obj_ci,options_ci);
				result = new Result(task.getTaskID(), result_ci, Constants.END);
				break;
			case "createIndexes":
				@SuppressWarnings("unchecked") 
				List<Document> options_cis = (ArrayList<Document>) task.getParams().get("obj");
				Document result_cis = Index.createIndexes(mongoCollection,options_cis);
				result = new Result(task.getTaskID(), result_cis, Constants.END);
				break;
			case "listIndexes":
				Document result_lis = Index.listIndexes(mongoCollection);
				result = new Result(task.getTaskID(), result_lis, Constants.END);
				break;
			case "dropIndexes":
				Document result_dis = Index.dropIndexes(mongoCollection);
				result = new Result(task.getTaskID(), result_dis, Constants.END);
				break;
			case "dropIndexB":
				Document obj_dib = (Document) task.getParams().get("obj");
				Document result_dib = Index.dropIndexB(mongoCollection,obj_dib);
				result = new Result(task.getTaskID(), result_dib, Constants.END);
				break;
			case "dropIndexS":
				String obj_diss =  task.getParams().getString("obj");
				Document result_diss = Index.dropIndexS(mongoCollection,obj_diss);
				result = new Result(task.getTaskID(), result_diss, Constants.END);
				break;
			default:
				result = new Result(task.getTaskID(), new Document("res", "Uncorrect Command."), Constants.END);;
				break;
			}
//			System.out.println(result.serialize());
			MiddleWare.clientResultSender.sendData(task.getIP(), task.getPort(), result.serialize());
			mwLogger.info("返回任务结果：" + task.toString());
			task.setResult(Constants.DONE);
			LocalLog.insertLog(mongoClient.getDatabase("logdb").getCollection("locallog")
					,task.getUser(),task.getFunc(),task.getDb()+"."+task.getCollection(),flag);
			mwLogger.info("完成任务：" + task.toString());
		}
	}
}
