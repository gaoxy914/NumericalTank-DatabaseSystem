package object;

import org.bson.Document;

import conf.Constants;

/**
 * 
 * @author 高翔宇
 *
 */
public class Task {
	private String IP; // Client IP地址
	private int port; // Client 接收端口
	private int filePort; // Client 文件接收端口
	private long taskID; // 任务ID
	private String time; // 任务创建时间
	private String user; // 任务发起者
	private String func; // 函数名
	private String db; // 数据库
	private String collection; // 集合
	private Document params; // 参数
	private int result; // 执行结果 0 undo 1 done 2 doing
	
	/**
	 * Task声明，参数形式
	 * @param IP Client IP地址
	 * @param port Client 接收端口
	 * @param filePort Client 文件接收端口
	 * @param taskID 任务ID
	 * @param time 任务创建时间
	 * @param user 任务发起者
	 * @param func 函数名
	 * @param db 数据库
	 * @param collection 集合
	 * @param params 参数
	 * @param result 执行结果
	 */
	public Task(String IP, Integer port, Integer filePort, long taskID, String time, String user,
			String func, String db, String collection, Document params, int result) {
		this.IP = IP;
		this.port = port;
		this.filePort = filePort;
		this.taskID = taskID;
		this.time = time;
		this.user = user;
		this.func = func;
		this.db = db;
		this.collection = collection;
		this.params = params;
		this.result = result;
	}
	
	/**
	 * Task声明，反序列化
	 * @param serializedData Task序列化信息
	 * @throws Exception 解析异常
	 */
	public Task(String serializedData) throws Exception {
		String[] args = serializedData.split(Constants.SPLITTER);
		this.IP = args[0];
		this.port = Integer.valueOf(args[1]);
		this.filePort = Integer.valueOf(args[2]);
		this.taskID = Long.parseLong(args[3]);
		this.time = args[4];
		this.user = args[5];
		this.func = args[6];
		this.db = args[7];
		this.collection = args[8];
		this.params = Document.parse(args[9]);
		this.result = Integer.parseInt(args[10]);
	}
	
	/**
	 * Task序列化
	 * @return Task序列化信息
	 */
	public String serialize() {
		String msg = "";
		msg += IP + Constants.SPLITTER;
		msg += port + Constants.SPLITTER;
		msg += filePort + Constants.SPLITTER;
		msg += taskID + Constants.SPLITTER;
		msg += time + Constants.SPLITTER;
		msg += user + Constants.SPLITTER;
		msg += func + Constants.SPLITTER;
		msg += db + Constants.SPLITTER;
		msg += collection + Constants.SPLITTER;
		msg += params.toJson() + Constants.SPLITTER;
		msg += result;
		return msg;
	}
	
	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getFilePort() {
		return filePort;
	}

	public void setFilePort(int filePort) {
		this.filePort = filePort;
	}

	public long getTaskID() {
		return taskID;
	}

	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Document getParams() {
		return params;
	}

	public void setParams(Document params) {
		this.params = params;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
	
	public boolean equals(Task task) {
		if (this.IP.equals(task.getIP())
				&& this.port == task.getPort()
				&& this.filePort == task.getFilePort()
				&& this.taskID == task.getTaskID()
				&& this.user.equals(task.getUser())
				&& this.time.equals(task.getTime())
				&& this.func.equals(task.getFunc())
				&& this.params.equals(task.getParams())
				&& this.collection.equals(task.getCollection())) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
//		detailed		
//		String str = IP + ":" + Integer.toString(port) + ":" + Integer.toString(filePort) + " id : " + Long.toString(this.taskID) + " time : " + this.time + " user : " + this.user + " func : "
//		+ this.func + " database : " + this.db + " collection : " + this.collection + " paramters : "
//		+ params.toString() + " result : " + Integer.toString(result);
//		brief
		String str = IP + ":" + Integer.toString(port) + ":" + Integer.toString(filePort) + " id : "
				+ Long.toString(this.taskID) + " time : " + this.time + " user : " + this.user + " func : "
				+ this.func;
		return str;
	}
}
