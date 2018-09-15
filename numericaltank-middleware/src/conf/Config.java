package conf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * 配置文件
 * @author 高翔宇
 *
 */
public class Config {
	private static final String LOCAL_IP = "LOCAL_IP";
	private static final String MAIN_MIDDLEWARE_IP = "MAIN_MIDDLEWARE_IP";
	private static final String MONGOS_IP = "MONGOS_IP";
	private static final String CLIENT_PORT = "CLIENT_PORT";
	private static final String CMD_PORT = "CMD_PORT";
	private static final String FILE_PORT = "FILE_PORT";
	private static final String SERVER_PORT = "SERVER_PORT";
	private static final String FILESERVER_PORT = "FILESERVER_PORT";
	private static final String LISTEN_PORT = "LISTEN_PORT";
	private static final String CHECK_PORT = "CHECK_PORT";
	private static final String THREADPOOL_SIZE = "THREADPOOL_SIZE";
	private static final String MONGOS_PORT = "MONGOS_PORT";
	private static final String FILE_SAVEPATH = "FILE_SAVEPATH";
	public static final String BACKUP_MIDDLEWARE_IP = "BACKUP_MIDDLEWARE_IP";
	public static final String CLIENT_IP = "CLIENT_IP";
	private static final String CLIENT_VALIDATE_WAY = "CLIENT_VALIDATE_WAY";
	public static final String ACTIVE_CLIENT = "ACTIVE_CLIIENT";
	public static final String SENDER_EMAIL_ACCOUNT = "SENDER_EMAIL_ACCOUNT";
	public static final String SENDER_EMAIL_PASSWORD = "SENDER_EMAIL_PASSWORD";
	public static final String SENDER_EMAIL_SMTPHOST = "SENDER_EMAIL_SMTPHOST";
	public static final String RECEIVER_EMAIL_ACCOUNT = "RECEIVER_EMAIL_ACCOUNT";
	private static final String THRESHOLD = "THRESHOLD";
	private static final String TIME_THRESHOLD = "TIME_THRESHOLD";
	
	private static final Integer CLIENTPORT_MIN = 53500;
	private static final Integer CLIENTPORT_MAX = 53700;
	private static final Integer CMDPORT_MIN = 54500;
	private static final Integer CMDPORT_MAX = 54700;
	private static final Integer FILEPORT_MIN = 55500;
	private static final Integer FILEPORT_MAX = 55700;
	
	public static String localIP = ""; // Local IP
	public static String mainMiddleWareIP = ""; // Main middleware IP
	public static List<String> backupMiddleWareIP = new ArrayList<String>(); // Backup middleware IP
	public static List<String> clientIP = new ArrayList<String>(); // Client IP
	public static List<String> mongosIP = new ArrayList<String>(); // MongoDB IP
	public static List<String> activeClient = new ArrayList<String>(); // 活跃Client
	public static String savePath = "./mfile/"; // 文件存储路径
	public static String senderEmailAccount = ""; // 发送邮箱
	public static String senderEmailPassword = ""; // 发送邮箱密码
	public static String senderEmailSMTPHost = ""; // 邮箱SMTPHost
	public static List<String> receiverEmailAccount = new ArrayList<String>(); // 接收邮箱

	public static int listenPort = 51000; // CHECKALIVE服务器端口
	public static int checkPort = 51500; // CHECKALIVE查询端口
	public static int serverPort = 52000; // 中间件TCP通信端口
	public static int cmdServerPort = 53000; // CMD通信端口
	public static int fileServerPort = 55000; // 文件通信端口
	public static Hashtable<Integer, Boolean> clientPort = new Hashtable<Integer, Boolean>(); // Client通信端口
	public static Hashtable<Integer, Boolean> cmdPort = new Hashtable<Integer, Boolean>(); // CMD通信端口
	public static Hashtable<Integer, Boolean> filePort = new Hashtable<Integer, Boolean>(); // 文件通信端口
	public static int mongosPort = 30000; // MONGODB端口
	public static int client_validate_way = 0; // CLIENT验证方式  0 ip验证 1 用户密码 2 允许所有连接 其余 拒绝所有连接
	public static int threadPoolSize = 4; // 最大执行任务线程数量
	public static int threshold = 1000; // 操作阈值
	public static int timeThreshold = 10; // 时间阈值

	public static boolean isMainMiddleWare; // 是否为主中间件
	public static boolean isRestart; // 重启标志位
	
	/**
	 * Chonfig声明
	 * @param filePath 配置文件路径
	 * @throws IOException
	 */
	public Config(String filePath) throws IOException {
		FileReader fileReader = new FileReader(filePath);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] args = line.split(":");
			if (args[0].indexOf(LOCAL_IP) != -1) {
				localIP = args[1];
			} else if (args[0].indexOf(MAIN_MIDDLEWARE_IP) != -1) {
				mainMiddleWareIP = args[1];
			} else if (args[0].indexOf(BACKUP_MIDDLEWARE_IP) != -1) { // 配置文件顺序决定继承顺序
				String[] ip = args[1].split(";");
				for (int i = 0; i < ip.length; i++) {
					backupMiddleWareIP.add(ip[i]);
				}
			} else if (args[0].indexOf((CLIENT_IP)) != -1) {
				String[] ip = args[1].split(";");
				for (int i = 0; i < ip.length; i++) {
					clientIP.add(ip[i]);
				}
			} else if (args[0].indexOf((MONGOS_IP)) != -1) {
				String[] ip = args[1].split(";");
				for (int i = 0; i < ip.length; i++) {
					mongosIP.add(ip[i]);
				}
			} else if (args[0].indexOf(CLIENT_PORT) != -1) {
				String[] port = args[1].split(";");
				for (int i = 0; i < port.length; i++) {
					clientPort.put(Integer.valueOf(port[i]), true);
				}
			} else if (args[0].indexOf(CMD_PORT) != -1) {
				String[] port = args[1].split(";");
				for (int i = 0; i < port.length; i++) {
					cmdPort.put(Integer.valueOf(port[i]), true);
				}
			} else if (args[0].indexOf(FILE_PORT) != -1) {
				String[] port = args[1].split(";");
				for (int i = 0; i < port.length; i++) {
					filePort.put(Integer.valueOf(port[i]), true);
				}
			} else if (args[0].indexOf(ACTIVE_CLIENT) != -1) {
				if (args.length > 1) {
					String[] client = args[1].split(";");
					for (int i = 0; i < client.length; i++) {
						activeClient.add(client[i]);
					}
				}
			} else if (args[0].indexOf(SERVER_PORT) != -1) {
				serverPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(LISTEN_PORT) != -1) {
				listenPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(CHECK_PORT) != -1) {
				checkPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(THREADPOOL_SIZE) != -1) {
				threadPoolSize = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(FILESERVER_PORT) != -1) {
				fileServerPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(MONGOS_PORT) != -1) {
				mongosPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(FILE_SAVEPATH) != -1) {
				savePath = args[1];
			} else if (args[0].indexOf(CLIENT_VALIDATE_WAY) != -1) {
				client_validate_way = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(SENDER_EMAIL_ACCOUNT) != -1) {
				senderEmailAccount = args[1];
			} else if (args[0].indexOf(SENDER_EMAIL_PASSWORD) != -1) {
				senderEmailPassword = args[1];
			} else if (args[0].indexOf(SENDER_EMAIL_SMTPHOST) != -1) {
				senderEmailSMTPHost = args[1];
			} else if (args[0].indexOf(RECEIVER_EMAIL_ACCOUNT) != -1) {
				String[] email = args[1].split(";");
				for (int i = 0; i < email.length; i++) {
					receiverEmailAccount.add(email[i]);
				}
			} else if (args[0].indexOf(THRESHOLD) != -1) {
				threshold = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(TIME_THRESHOLD) != -1) {
				timeThreshold = Integer.parseInt(args[1]);
			}
		}
		if (clientPort.size() == 0) {
			for (int port = CLIENTPORT_MIN; port < CLIENTPORT_MAX; port++) {
				clientPort.put(port, true);
			}
		}
		if (cmdPort.size() == 0) {
			for (int port = CMDPORT_MIN; port < CMDPORT_MAX; port++) {
				cmdPort.put(port, true);
			}
		}
		if (filePort.size() == 0) {
			for (int port = FILEPORT_MIN; port < FILEPORT_MAX; port++) {
				filePort.put(port, true);
			}
		}
		if (localIP.equals(mainMiddleWareIP)) {
			isMainMiddleWare = true;
		} else {
			isMainMiddleWare = false;
		}
		bufferedReader.close();
		fileReader.close();
	}
	
	/**
	 * 申请可用本地端口
	 * @return 可用端口； -1 无可用端口
	 */
	public static int getClientPort() {
		for (int port : clientPort.keySet()) {
			if (clientPort.get(port) && isPortAvailable(port)) {
				clientPort.replace(port, false);
				return port;
			}
		}
		return -1;
	}
	
	/**
	 * 释放端口
	 * @param port 端口
	 */
	public static void freeClientPort(int port) {
		clientPort.replace(port, true);
	}
	
	/**
	 * 申请可用本地端口
	 * @return 可用端口； -1 无可用端口
	 */
	public static int getCMDPort() {
		for (int port : cmdPort.keySet()) {
			if (cmdPort.get(port) && isPortAvailable(port)) {
				cmdPort.replace(port, false);
				return port;
			}
		}
		return -1;
	}
	
	/**
	 * 释放端口
	 * @param port 端口
	 */
	public static void freeCMDPort(int port) {
		cmdPort.replace(port, true);
	}
	
	/**
	 * 申请可用本地端口
	 * @return 可用端口； -1 无可用端口
	 */
	public static int getFilePort() {
		for (int port : filePort.keySet()) {
			if (filePort.get(port) && isPortAvailable(port)) {
				filePort.replace(port, false);
				return port;
			}
		}
		return -1;
	}
	
	/**
	 * 释放端口
	 * @param port 端口
	 */
	public static void freeFilePort(int port) {
		filePort.replace(port, true);
	}
	
	@SuppressWarnings("unused")
	private static boolean isLocalPortUsing(int port) {
		boolean flag = true;
		try {
			flag = isPortUsing(Constants.LOCALHOST, port);
		} catch (Exception e) {
			
		}
		return flag;
	}
	
	@SuppressWarnings({ "resource", "unused" })
	private static boolean isPortUsing(String host, int port) throws UnknownHostException {
		boolean flag = false;
		InetAddress theAddress = InetAddress.getByName(host);
		try {
			Socket socket = new Socket(theAddress, port);
			flag = true;
		} catch (IOException e) {
			
		}
		System.out.println(port + ":" + flag);
		return flag;
	}
	
	private static void bindPort(String host, int port) throws Exception {
	    Socket s = new Socket();
	    s.bind(new InetSocketAddress(host, port));
	    s.close();
	}
	
	@SuppressWarnings({ "unused", "resource" })
	private static boolean isPortAvailable(int port) {
	    Socket s = new Socket();
	    try {
	        bindPort("0.0.0.0", port);
	        bindPort(InetAddress.getLocalHost().getHostAddress(), port);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
