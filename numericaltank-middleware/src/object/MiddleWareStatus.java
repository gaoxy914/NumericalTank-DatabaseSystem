package object;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import conf.Config;

/**
 * MiddleWareStatus
 * @author 高翔宇
 *
 */
public class MiddleWareStatus {
	private boolean isPrimary; // 是否为主中间件
	private LinkedBlockingQueue<Task> taskQueue; // 任务序列
	private Hashtable<String, CountTable> userTable; // 操作记录
	
	private class CountTable {
		private Hashtable<String, Number> funcCount; // 操作数量表 <操作名，操作数>
		
		public CountTable() {
			funcCount = new Hashtable<String, Number>();
		}
		
		/**
		 * 操作数加一
		 * @param func 操作名
		 */
		public void add(String func) {
			if (funcCount.containsKey(func)) {
				Number number = funcCount.get(func);
				number.add();
				funcCount.replace(func, number);
			} else {
				funcCount.put(func, new Number());
			}
		}
		
		/**
		 * 操作数
		 * @param func 操作名
		 * @return 操作数
		 */
		public int getCount(String func) {
			if (funcCount.containsKey(func)) {
				return funcCount.get(func).getCount();
			}
			return 0;
		}
		
		public String toString() {
			String result = "";
			for (String func : funcCount.keySet()) {
				result += func + ":" + funcCount.get(func).toString() + ";\n";
			}
			return result;
		}
	}
	
	private class Number {
		private int count;
		private Long time;
		
		public Number() {
			count = 1;
			time = System.currentTimeMillis();
		}
		
		public void add() {
			if (System.currentTimeMillis() - time < Config.timeThreshold) {
				count++;
//				System.out.println(count);
			} else {
				count = 1;
			}
			time = System.currentTimeMillis();
		}
		
		public int getCount() {
			return count;
		}
		
		public String toString() {
			return "number:" + count + "last time:" + time;
		}
	}
	
	public MiddleWareStatus(boolean isPrimary) {
		this.isPrimary = isPrimary;
		this.taskQueue = new LinkedBlockingQueue<Task>();
		this.userTable = new Hashtable<String, CountTable>();
	}
	
	/**
	 * 用户操作加一
	 * @param user 用户名
	 * @param func 操作名
	 */
	public void add(String user, String func) {
		if (userTable.containsKey(user)) {
			userTable.get(user).add(func);
		} else {
			CountTable countTable = new CountTable();
			countTable.add(func);
			userTable.put(user, countTable);
		}
//		System.out.println(toString());
		String content = "";
		if (getCount(user, "delete") > Config.threshold || getCount(user, "deleteMany") > Config.threshold) {
//			System.out.println("删除操作数大于阈值");
			content = "用户：" + user + ",执行删除操作过于频繁。";
			try {
				sendEmail(content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (getCount(user, "update") > Config.threshold || getCount(user, "updateMany") > Config.threshold) {
//			System.out.println("更新操作数大于阈值");
			content = "用户：" + user + ",执行更新操作过于频繁。";
			try {
				sendEmail(content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (getCount(user, "find") > Config.threshold) {
//			System.out.println("查询操作数大于阈值");
			content = "用户：" + user + ",执行查询操作过于频繁。";
			try {
				sendEmail(content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 发送预警邮件
	 * @param content 邮件内容
	 * @throws Exception
	 */
	private void sendEmail(String content) throws Exception {
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocl", "smtp");
		properties.setProperty("mail.smtp.host", Config.senderEmailSMTPHost);
		properties.setProperty("mail.smtp.auth", "true");
		Authenticator authenticator = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Config.senderEmailAccount, Config.senderEmailPassword);
			}
		};
		Session session = Session.getInstance(properties, authenticator);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(Config.senderEmailAccount));
		InternetAddress[] sendTo = new InternetAddress[Config.receiverEmailAccount.size()];
		for (int i = 0; i < Config.receiverEmailAccount.size(); i++) {
			sendTo[i] = new InternetAddress(Config.receiverEmailAccount.get(i));
		}
		message.setRecipients(MimeMessage.RecipientType.TO, sendTo);
		message.setSubject("中间件系统预警", "UTF-8");
		message.setContent(content, "text/html;charset=UTF-8");
		message.setSentDate(new Date());
		message.saveChanges();
		Transport.send(message);
	}
	
	public int getCount(String user, String func) {
		if (userTable.containsKey(user)) {
			return userTable.get(user).getCount(func);
		}
		return 0;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	public LinkedBlockingQueue<Task> getTaskQueue() {
		return taskQueue;
	}
	
	public void setTaskQueue(LinkedBlockingQueue<Task> taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	/**
	 * 任务队列大小
	 * @return 队列长度
	 */
	public int getTaskQueueSize() {
		return taskQueue.size();
	}
	
	/**
	 * 添加任务
	 * @param task 任务
	 * @throws InterruptedException
	 */
	public void addTask(Task task) throws InterruptedException {
		taskQueue.put(task);
	}
	
	/**
	 * 取出第一个任务
	 * @return 任务
	 * @throws InterruptedException
	 */
	public Task getTask() throws InterruptedException {
		return taskQueue.take();
	}
	
	/**
	 * 删除任务
	 * @param task 任务
	 */
	public void delTask(Task task) {
		Iterator<Task> iterator = taskQueue.iterator();
		while (iterator.hasNext()) {
			Task task2 = iterator.next();
			if (task2.equals(task)) {
				taskQueue.remove(task2);
			}
		}
	}
	
	public String toString() {
		String string = "";
		string = isPrimary ? "MIDDLEWARE_STATUS:PRIMARY" : "MIDDLEWARE_STATUS:SECONDARY";
		string += ";\nTASKQUEUESIZE:" + taskQueue.size();
		string += ";\nUSERFUNCCOUNT:\n";
		for (String user : userTable.keySet()) {
			string += "user:" + user +"\n" + userTable.get(user).toString();
		}
		return string;
	}
}
