package mongolog;

import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import com.mongodb.MongoException;
/**
 * 查看数据库状态信息
 * @author NiceCharles
 *
 */
public class DBStatus {
	/**
	 * 查看函数
	 * @return 自定义document结构
	 * {<br>
	 *	 outConn:{//输出的连接<br>
	 *	 	nInUse:0, //int,正在使用<br>
	 *	 	nAvailable:0, //int,可使用<br>
	 *	 	nCreated:0, //int,创建过的<br>
	 *	 	nRefreshing:0//int,正在更新的<br>
	 *   }, <br>
	 *	 dbs:[//数据库列表及其使用硬盘大小<br>
	 *   	{<br>
	 *   	 	name:admin, //String,数据库名<br>
	 *   	 	sizeOnDisk:225280, //int,占用硬盘大小<br>
	 *   	 	empty:false<br>
	 *   	}, <br>
	 *   	{<br>
	 *   	 	name:local, <br>
	 *   	 	sizeOnDisk:81920, <br>
	 *   	 	empty:false<br>
	 *    	},<br>
	 *    	{<br>
	 *   	 	name:logdb,<br>
	 *   	 	sizeOnDisk:102400, <br>
	 *   	 	empty:false<br>
	 *    	}, <br>
	 *    	{<br>
	 *   	 	name:test, <br>
	 *   	 	sizeOnDisk:126976, <br>
	 *   	 	empty:false<br>
	 *    	}<br>
	 *	 ],<br>
	 *	 uptime:7067,//int,服务器运行时间(秒)<br>
	 *	 inConn:{//传入连接<br>
	 *		current:2, <br>
	 *		available:999998, <br>
	 *		totalCreated:38<br>
	 *	 }, <br>
	 *	 pagefaults:82263, //int,页面错误数<br>
	 *	 netflow:{//网络流量<br>
	 *		bytesIn:18943, //int,输入的字节<br>
	 *		bytesOut:271089, <br>
	 *		physicalBytesIn:18943, <br>
	 *		physicalBytesOut:271089, <br>
	 *		numRequests:349//int,网络请求数<br>
	 *	 }, <br>
	 *	 opcounters:{//int,操作统计<br>
	 *	 	insert:0, <br>
	 *		query:1, <br>
	 *		update:0, <br>
	 *		delete:0, <br>
	 *		getmore:0, <br>
	 *		command:175<br>
	 *	 }, <br>
	 *	 mem:{<br>
	 *		bits:64, //64位系统<br>
	 *		resident:22, //int,内存使用(MB)<br>
	 *		virtual:921,//虚拟内存使用<br>
	 *		supported:true//Boolean,是否支持扩展内存<br>
	 *	 }<br>
	 *}<br>
	 */
	public static List<Document> showStatus(MongoDatabase mongoDatabase){
		try{
			Document cpstats = mongoDatabase.runCommand(new Document("connPoolStats",1));
			Document links = new Document("nInUse",cpstats.getInteger("totalInUse"));
			links.append("nAvailable",cpstats.getInteger("totalAvailable"));
			links.append("nCreated",cpstats.getInteger("totalCreated"));
			links.append("nCreated",cpstats.getInteger("totalCreated"));
			links.append("nRefreshing",cpstats.getInteger("totalRefreshing"));
			Document results= new Document();
			results.append("outConn", links);//传出连接
			
			Document listDB = mongoDatabase.runCommand(new Document("listDatabases",1));
			results.append("dbs", listDB.get("databases"));
			
			Document serverStatus = mongoDatabase.runCommand(new Document("serverStatus",1));
		    results.append("uptime", serverStatus.get("uptime"));
		    results.append("inConn", serverStatus.get("connections"));//传入连接
		    results.append("pagefaults",((Document)serverStatus.get("extra_info")).get("page_faults"));
		    //磁盘IO的“硬”页面故障以及内存中移动页面的“软”页面错误
		    results.append("netflow", serverStatus.get("network"));//服务器流量统计
		    results.append("opcounters", serverStatus.get("opcounters"));
		    results.append("mem", serverStatus.get("mem"));
		    List<Document> res = new ArrayList<Document>();
		    res.add(results);
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	@SuppressWarnings("unchecked")
	public static List<Document> currentOp(MongoDatabase mongoDatabase,String shardname){
		try{
			Document cop = mongoDatabase.runCommand(new Document("currentOp",1));
			if(shardname ==null)
			{
				return (List<Document>)cop.get("inprog");
			}
			Document shard = (Document)cop.get("raw");
			Set<String> ls = shard.keySet();
			for(String s:ls) {
				if(s.contains(shardname))
					return (List<Document>)((Document)shard.get(s)).get("inprog");
			}
		    List<Document> res = new ArrayList<Document>();
		    res.add(new Document("currentOp","there is no shard named as "+shardname+" !!!"));
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	public static List<Document> listDatabases(MongoDatabase mongoDatabase){
		try{
			Document ld = mongoDatabase.runCommand(new Document("listDatabases",1));
		    List<Document> res = new ArrayList<Document>();
		    res.add(ld);
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	public static List<Document> connectionStatus(MongoDatabase mongoDatabase){
		try{
			Document cs = mongoDatabase.runCommand(new Document("connPoolStats",1));
		    List<Document> res = new ArrayList<Document>();
		    res.add((Document)cs.get("hosts"));
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	public static List<Document> nodeStatus(MongoDatabase mongoDatabase){
		try{
			Document cs = mongoDatabase.runCommand(new Document("connPoolStats",1));
		    List<Document> res = new ArrayList<Document>();
		    res.add((Document)cs.get("replicaSets"));
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	public static List<Document> listShards(MongoDatabase mongoDatabase){
		try{
			Document ls = mongoDatabase.runCommand(new Document("listShards",1));
		    List<Document> res = new ArrayList<Document>();
		    res.add(ls);
			return res;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}

}
