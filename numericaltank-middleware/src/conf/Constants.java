package conf;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 常量
 * @author 高翔宇
 *
 */
public class Constants {
	public static final boolean END = true;
	public static final boolean NOT_END = false;

	public static final int UNDO = 0;
	public static final int DONE = 1;
	public static final int DOING = 2;
	public static final int MAX_RETURN_SIZE = 100000;
	public static final int NO_FREE_PORT = 0;
	
	public static final String SPACE = " ";
	public static final String SPLITTER = "&&&";
	public static final String COMMAND_BEMAINMIDDLEWARE = "BECOME MAIN MIDDLEWARE";
	public static final String MESSAGE_CHECK = "CHECK MAINMIDDLEWARE AT : ";
	public static final String MESSAGE_REPLY = "STILL ALIVE AT : ";
	public static final String MESSAGE_TCPKEEP = "TCP KEEP";
	public static final String STILL = "STILL";
	public static final String CHECK = "CHECK";
	public static final String BECOME = "BECOME";
	public static final String LOCALHOST = "127.0.0.1";
	
	public static String getCheckMessage() {
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String checkMsg = MESSAGE_CHECK + sdFormat.format(nowTime);
		return checkMsg;
	}

	public static String getReplyMessage() {
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String replyMsg = MESSAGE_REPLY + sdFormat.format(nowTime);
		return replyMsg;
	}
}
