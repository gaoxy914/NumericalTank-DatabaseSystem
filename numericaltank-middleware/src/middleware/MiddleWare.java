package middleware;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import conf.Config;
import object.MiddleWareStatus;
import service.CMDServer;
import service.CheckAliveService;
import service.ExecuteService;
import service.FileSender;
import service.FileServer;
import service.ListenCheckService;
import service.ResultSender;
import service.TaskServer;

/**
 * MiddleWare
 * @author 高翔宇
 *
 */
public class MiddleWare {
	public static MiddleWare middleWare;
	
	public static MiddleWareStatus middleWareStatus; // 中间件状态
	public static ResultSender clientResultSender = new ResultSender(); // Client结果发送
	public static ResultSender cmdResultSender = new ResultSender(); // CMD结果发送
	public static Hashtable<String, FileSender> fileSenders = new Hashtable<String, FileSender>(); // 文件发送
	public static CheckAliveService checkAliveService = new CheckAliveService(); // 心跳检测服务
	public static boolean endFlag = false; // 中间件停止标志位
	
	private ListenCheckService listenCheckService = new ListenCheckService(); // 心跳监听服务
	private TaskServer taskServer = new TaskServer(); // 任务接收
	private ExecuteService executeService = new ExecuteService(); // 任务监测服务
	private FileServer fileServer = new FileServer(); // 文件接收线程
	private CMDServer cmdServer = new CMDServer(); // CMD任务接收
	private Logger mwLogger = LogManager.getLogger("middleware"); // 日志
	
	/**
	 * 启动MiddleWare
	 * @param configPath 配置文件路径
	 * @param mongouser MongoDB用户名
	 * @param password MongoDB密码
	 * @throws IOException
	 */
	public void start(String configPath, String mongouser, String password) throws IOException {
		new Config(configPath);	
		mwLogger.info("启动中间件");
		middleWareStatus = new MiddleWareStatus(Config.isMainMiddleWare);
		if (!middleWareStatus.isPrimary()) {
			checkAliveService.start();
		}
		listenCheckService.start();
		taskServer.start();
		executeService.start(mongouser, password);
		fileServer.start();
		cmdServer.start();
		if (Config.isRestart) {
			for (String client : Config.activeClient) {
				int localPort = Config.getClientPort();
				if (localPort > 0) {
					Socket sender;
					try {
						String[] args = client.split("&");
						sender = new Socket(args[0], Integer.valueOf(args[1]), null, localPort);
						MiddleWare.clientResultSender.insert(args[0], Integer.valueOf(args[1]), sender);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 关闭MiddleWare
	 */
	public void clsoe() {
		checkAliveService.close();
		listenCheckService.close();
		clientResultSender.close();
		cmdResultSender.close();
		taskServer.close();
		executeService.clsoe();
		cmdServer.start();
		fileServer.close();
		for (FileSender fileSender : fileSenders.values()) {
			fileSender.close();
		}
		mwLogger.info("关闭中间件");
		System.out.println("bye!^-^");
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		middleWare = new MiddleWare();
		middleWare.start(args[0], args[1], args[2]);
	}
}
