package object;

import org.bson.Document;

import conf.Constants;

/**
 * 
 * @author 高翔宇
 *
 */
public class Result {
	private long taskID; // 任务ID
	private Document result; // 返回值
	private boolean end; // 结束标志位
	
	/**
	 * Result声明，参数形式
	 * @param taskID 任务ID
	 * @param result 返回值
	 * @param end 结束标志位
	 */
	public Result(long taskID, Document result, boolean end) {
		this.taskID = taskID;
		this.result = result;
		this.end = end;
	}
	
	/**
	 * Result声明，反序列化
	 * @param serializedData Result序列化信息
	 * @throws Exception 解析异常
	 */
	public Result(String serializedData) throws Exception {
		String[] args = serializedData.split(Constants.SPLITTER);
		this.taskID = Long.parseLong(args[0]);
		this.result = Document.parse(args[1]);
		this.end = Boolean.parseBoolean(args[2]);
	}
	
	/**
	 * Result序列化
	 * @return Result序列化信息
	 */
	public String serialize() {
		String msg = taskID + Constants.SPLITTER + result.toJson() + Constants.SPLITTER + end;
		return msg;
	}

	public long getTaskID() {
		return taskID;
	}

	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}

	public Document getResult() {
		return result;
	}

	public void setResult(Document result) {
		this.result = result;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}
}
