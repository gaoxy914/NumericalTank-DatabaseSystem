package cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bson.Document;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import service.ClientService;
import service.ServerService;

public class CMD {
	public static volatile ServerService serverService = new ServerService();
	public static volatile ClientService clientService = new ClientService();
	public static volatile Document result = null;
	public static volatile boolean flag = false;
	
	private final static String EXIT = "exit";
	
	public static void start(String ip) {
		serverService.start();
		clientService.start(ip);
	}
	
	public static void stop() {
		serverService.close();
		clientService.close();
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		boolean running = false;
		CMD.start(args[0]);
		Scanner scanner = new Scanner(System.in);
		ObjectMapper mapper = new ObjectMapper();
		String validate = args[1] + " " + args[2] + " " + ServerService.consolePort;
		if (clientService.connected()) {
			clientService.send(validate);
		}
		while (true) {
			if (flag) {
				flag = false;
				break;
			}
		}
		String feedback = result.getString("res");
		if (feedback.equals("welcome")) {
			running = true;
			System.out.println("welcome!^-^");
		} else {
			running = false;
			System.out.println(feedback);
		}
		while (running) {
			System.out.print("[command] # ");
			String command = scanner.nextLine();
			if (command.equals(EXIT)) {
				System.out.println("bye!^-^");	
				break;
			} else {
				if (clientService.connected()) {
					clientService.send(command);
				} else {
					System.out.println("connection error.");
					break;
				}
				while (true) {
					if (flag) {
						flag = false;
						break;
					}
				}
				@SuppressWarnings("unchecked")
				List<Document> results = result.get("res")==null ? new ArrayList<Document>():(ArrayList<Document>)result.get("res");
				for(Document doc:results)
				{
					Object json = mapper.readValue(doc.toJson(), Object.class);
					System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
				}
				result = null;
			}
		}
		scanner.close();
		CMD.stop();
		System.exit(0);
	}
}
