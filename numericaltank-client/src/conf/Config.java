package conf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件
 * @author 高翔宇
 *
 */
public class Config {
	private static final String LOCAL_IP = "LOCAL_IP";
	private static final String SERVER_PORT = "SERVER_PORT";
	private static final String MIDDLEWARE_SERVER_PORT = "MIDDLEWARE_SERVER_PORT";
	private static final String FILESERVER_PORT = "FILESERVER_PORT";
	private static final String MIDDLEWARE_FILESERVER_PORT = "MIDDLEWAER_FILESERVER_PORT";
	private static final String MIDDLEWARE_IP = "MIDDLEWARE_IP";
	private static final String FILE_SAVEPATH = "FILE_SAVEPATH";

	public static String localIP = ""; // 本机IP
	public static List<String> middleWareIP = new ArrayList<String>(); // 中间件IP
	public static String savePath = "./cfile/"; // 文件存储位置

	public static int mwServerPort = 52000; // 中间件TCP通信端口
	public static int mwFileServerPort = 55000; // 中间件文件通信端口
	public static int serverPort; // 通信端口
	public static int fileServerPort; // 文件通信端口

	/**
	 * Config声明
	 * @param filePath 配置文件路径
	 * @throws IOException 文件读取异常
	 */
	public Config(String filePath) throws IOException {
		FileReader fileReader = new FileReader(filePath);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] args = line.split(":");
			if (args[0].indexOf(LOCAL_IP) != -1) {
				localIP = args[1];
			} else if (args[0].indexOf(SERVER_PORT) != -1) {
				serverPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(MIDDLEWARE_IP) != -1) {
				String[] ips = args[1].split(";");
				for (String ip : ips) {
					middleWareIP.add(ip);
				}
			} else if (args[0].indexOf(FILE_SAVEPATH) != -1) {
				savePath = args[1];
			} else if (args[0].indexOf(FILESERVER_PORT) != -1) {
				fileServerPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(MIDDLEWARE_SERVER_PORT) != -1) {
				mwServerPort = Integer.parseInt(args[1]);
			} else if (args[0].indexOf(MIDDLEWARE_FILESERVER_PORT) != -1) {
				mwFileServerPort = Integer.parseInt(args[1]);
			}
		}
		bufferedReader.close();
		fileReader.close();
	}
}
