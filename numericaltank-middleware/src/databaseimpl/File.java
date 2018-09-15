package databaseimpl;

import org.bson.Document;

import conf.Config;

public class File {
	public static Document exportFile(String dbname,String collectionname,String ip,int port
			,Document filter,String proj,Document order,Integer limit,Integer skip
			,String filename,String type, String mongouser, String password) {
		String commandStr = "mongoexport --host "+ip+":"+port
				+" --username " + mongouser
				+" --password " + password
				+" --authenticationDatabase admin"
				+" --db "+dbname+" --collection "+collectionname
				+(proj==null ? "":(" --fields "+proj))
				+(filter==null ? "":(" --query \""+filter.toJson().replace("\"", "'")+"\""))
				+(order==null ? "":(" --sort \""+order.toJson().replace("\"", "'")+"\""))
				+(limit==null ? "":(" --limit "+limit))
				+(skip==null ? "":(" --skip "+skip))
				+(type.equals("csv") ? (" --type=csv"):"")
				+" --out " + Config.savePath + filename;
//		System.out.println(commandStr);
		String[] cmd = new String[]{"sh","-c",commandStr};
		try {
			Process p =Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return new Document("res", false);
		}
		return new Document("res", true);
	}
	
	public static Document importFile(String dbname,String collectionname,String ip,int port,
			String fields,boolean ignoreblanks,boolean headerline,boolean drop,String mode,String upsertfields
			,String filename,String type, String mongouser, String password) {
		String commandStr = "mongoimport --host "+ip+":"+port
				+" --username " + mongouser
				+" --password " + password
				+" --authenticationDatabase admin"
				+" --db "+dbname+" --collection "+collectionname
				+" --file " + Config.savePath + filename;
		switch(type)
		{
		case "csv":
		case "tsv":
			if(ignoreblanks)
				commandStr += " --ignoreBlanks";
			if(headerline)
				commandStr += " --headerline"; 
			else
			{
				if(fields != null)
					commandStr += " --fields "+fields;
			}
		case "json":
			commandStr +=" --type "+type;
			break;
		}
		if(drop)
			commandStr += " --drop";
		else
		{
			switch(mode)
			{
			case "upsert":
				if(upsertfields != null)
					commandStr += " --upsertFields "+upsertfields;
			case "insert":
			case "merge":
				commandStr += " --mode "+mode;
				break;
			}
		}
//		System.out.println(commandStr);
		String[] cmd = new String[]{"sh","-c",commandStr};
		try {
			Process p =Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return new Document("res", false);
		}
		return new Document("res", true);
	}
}
