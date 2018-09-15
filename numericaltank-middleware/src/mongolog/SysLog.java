package mongolog;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
/**
 * 查看系统自带日志
 * @author NiceCharles
 *
 */
public class SysLog {
	/**
	 * 按时间排序
	 * @param dbname 数据库名
	 * @return 
	 * document<br>
	 * {<br>
	 * 	 ts:Date,<br>
	 * 	 ns:String,//命名空间<br>
	 * 	 op:String,<br>
	 * 	 millis:int,//运行时间(ms)<br>
	 * 	 docsExamined:int,//文件搜索数<br>
	 * 	 keysExamined:int,//索引搜索数<br>
	 * 	 nreturned:int,//文件返回数<br>
	 * 	 ninserted:int,<br>
	 * 	 keysInserted:int,<br>
	 * 	 filter:document,//过滤器<br>
	 * 	 obj:document//插入或更新内容<br>
	 * }<br>
	 */
	public static List<Document> showByTs(MongoCollection<Document> collection,int num){
		try{
			List<Document> cond= new ArrayList<Document>();
	        List<Document> orcond = new ArrayList<Document>();
	        orcond.add(new Document("op","query"));
	        orcond.add(new Document("op","update"));
	        orcond.add(new Document("op","remove"));
	        orcond.add(new Document("op","insert"));
	        cond.add(new Document("$or",orcond));
	        cond.add(new Document("ns",new Document("$ne","project.system.profile")));
			FindIterable<Document> findIterable = collection.find(new Document("$and",cond)).sort(new Document("ts",-1)).limit(num);
	        List<Document> results = new ArrayList<Document>();
	        for(Document doc:findIterable)
	        {
	        	Document res=new Document("ts",doc.get("ts"));
	        	res.append("ns", doc.get("ns"));
	        	res.append("op", doc.get("op"));
	        	res.append("millis", doc.get("millis"));
	        	//res.append("user",doc.get("user"));
	        	res.append("docsExamined", doc.getInteger("docsExamined", 0));
	        	res.append("keysExamined", doc.getInteger("keysExamined", 0));
	        	res.append("nreturned", doc.getInteger("nreturned", 0));
	        	res.append("ninserted", doc.getInteger("ninserted", 0)-doc.getInteger("ndeleted", 0));
	        	res.append("keysInserted", doc.getInteger("keysInserted", 0)-doc.getInteger("keysDeleted", 0));
	        	if(doc.getString("op").equals("query"))
	        		res.append("filter", ((Document)doc.get("query")).get("filter"));
	        	else if(doc.getString("op").equals("update"))
	        	{
	        		res.append("filter", doc.get("query"));
	        		res.append("obj", doc.get("updateobj"));
	        	}
	        	else if(doc.getString("op").equals("remove"))
	        		res.append("filter", doc.get("query"));
	        	else
	        		res.append("obj", ((Document)doc.get("query")).get("documents"));
	        	results.add(res);
	        }
	        return results;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 按耗时排序
	 * @param dbname 数据库名
	 * @return 
	 * document<br>
	 * {<br>
	 * 	 ts:Date,<br>
	 * 	 ns:String,//命名空间<br>
	 * 	 op:String,<br>
	 * 	 millis:int,//运行时间(ms)<br>
	 * 	 docsExamined:int,//文件搜索数<br>
	 * 	 keysExamined:int,//索引搜索数<br>
	 * 	 nreturned:int,//文件返回数<br>
	 * 	 ninserted:int,<br>
	 * 	 keysInserted:int,<br>
	 * 	 filter:document,//过滤器<br>
	 * 	 obj:document//插入或更新内容<br>
	 * }<br>
	 */
	public static List<Document> showByMillis(MongoCollection<Document> collection,int num){
		try{
			List<Document> cond= new ArrayList<Document>();
	        List<Document> orcond = new ArrayList<Document>();
	        orcond.add(new Document("op","query"));
	        orcond.add(new Document("op","update"));
	        orcond.add(new Document("op","remove"));
	        orcond.add(new Document("op","insert"));
	        cond.add(new Document("$or",orcond));
	        cond.add(new Document("ns",new Document("$ne","profile.system.profile")));
			FindIterable<Document> findIterable = collection.find(new Document("$and",cond)).sort(new Document("millis",-1)).limit(num);
	        List<Document> results = new ArrayList<Document>();
	        for(Document doc:findIterable)
	        {
	        	Document res=new Document("ts",doc.get("ts"));
	        	res.append("ns", doc.get("ns"));
	        	res.append("op", doc.get("op"));
	        	res.append("millis", doc.get("millis"));
	        	//res.append("user",doc.get("user"));
	        	res.append("docsExamined", doc.getInteger("docsExamined", 0));
	        	res.append("keysExamined", doc.getInteger("keysExamined", 0));
	        	res.append("nreturned", doc.getInteger("nreturned", 0));
	        	res.append("ninserted", doc.getInteger("ninserted", 0)-doc.getInteger("ndeleted", 0));
	        	res.append("keysInserted", doc.getInteger("keysInserted", 0)-doc.getInteger("keysDeleted", 0));
	        	if(doc.getString("op").equals("query"))
	        		res.append("filter", ((Document)doc.get("query")).get("filter"));
	        	else if(doc.getString("op").equals("update"))
	        	{
	        		res.append("filter", doc.get("query"));
	        		res.append("obj", doc.get("updateobj"));
	        	}
	        	else if(doc.getString("op").equals("remove"))
	        		res.append("filter", doc.get("query"));
	        	else
	        		res.append("obj", ((Document)doc.get("query")).get("documents"));
	        	results.add(res);
	        }
	        return results;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
}
