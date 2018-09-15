package mongolog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
/**
 * 自建日志说明
 * @author NiceCharles
 *
 */
public class LocalLog {
	/**
	 * 插入用户操作日志
	 * @param user 用户id
	 * @param op 操作类型
	 * @param ns 操作空间，形式:dbname.tablename
	 * @param params 具体操作内容
	 * @param re 成功:true,失败:false
	 */
	public static void insertLog(MongoCollection<Document> collection
			,String user,String op,String ns,boolean re) { //查询集合内全部
		try {
	        collection.insertOne(new Document("time",new Date())
	        			.append("user",user)
	        			.append("op",op)
	        			.append("ns",ns)//Task.getDB()+Task.getCollection
	        			.append("re",re));
		}catch(MongoException e) {
            e.printStackTrace();
	    } 
	}
	/**
	 * 查看自建操作日志
	 * @return 具体内容在insertLog已有描述
	 *Document形式<br>
	 *{<br>
	 *	 time:Date,<br>
	 *	 user:String,<br>
	 *	 op:String,<br>
	 *	 ns:String,<br>
	 *	 params:Document,<br>
	 *	 re:boolean<br>
	 *}<br>
	 */
	public static List<Document> show(MongoCollection<Document> collection,int num){
		try{
	        FindIterable<Document> findIterable = collection.find().sort(new Document("time",-1)).limit(num);
	        MongoCursor<Document> mongoCursor = findIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 查看对各个数据库不同操作数量分布
	 * @return
	 *Document形式<br>
	 *{<br>
	 *	 _id:{<br>
	 *		ns:String, //命名空间(dbname.tablename)<br>
	 *		op:String //操作类型<br>
	 *	 },<br>
	 *	 count:int<br>
	 *}<br>
	 */
	public static List<Document> countByDB(MongoCollection<Document> collection){
		try{
			Document groupfield = new Document("_id",new Document("ns","$ns").append("op", "$op"));
			groupfield.append("count", new Document("$sum",1));
			Document group = new Document("$group",groupfield);
			List<Document> cond = new ArrayList<Document>();
			cond.add(group);
			AggregateIterable<Document> AIterable = collection.aggregate(cond);
	        MongoCursor<Document> mongoCursor = AIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 查看错误操作
	 * @return 具体内容在insertLog已有描述
	 *Document形式<br>
	 *{<br>
	 *	 time:Date,<br>
	 *	 user:String,<br>
	 *	 op:String,<br>
	 *	 ns:String,<br>
	 *	 params:Document,<br>
	 *	 re:boolean<br>
	 *}<br>
	 */
	public static List<Document> errorOp(MongoCollection<Document> collection,int num){
		try{
	        FindIterable<Document> findIterable = collection.find(new Document("re",false)).sort(new Document("time",-1)).limit(num);
	        MongoCursor<Document> mongoCursor = findIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 每年不同操作数量变化趋势
	 * @return
	 * Document形式<br>
	 *{<br>
	 *	 _id:{<br>
	 *		year:int,<br>
	 *		op:String //操作类型<br>
	 *	 },<br>
	 *	 count:int<br>
	 *}<br>
	 */
	public static List<Document> trendByYear(MongoCollection<Document> collection){
		try{
			Document id = new Document("year",new Document("$year","$time"));
			id.append("op", "$op");
			Document groupfield = new Document("_id",id);
			groupfield.append("count", new Document("$sum",1));
			Document group = new Document("$group",groupfield);
			List<Document> cond = new ArrayList<Document>();
			cond.add(group);
			
			AggregateIterable<Document> AIterable = collection.aggregate(cond);
	        MongoCursor<Document> mongoCursor = AIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 每月不同操作数量变化趋势
	 * @return
	 * Document形式<br>
	 *{<br>
	 *	 _id:{<br>
	 *		year:int,<br>
	 *		month:int,<br>
	 *		op:String //操作类型<br>
	 *	 },<br>
	 *	 count:int<br>
	 *}<br>
	 */
	public static List<Document> trendByMonth(MongoCollection<Document> collection){
		try{
			Document id = new Document("year",new Document("$year","$time"));
			id.append("month",new Document("$month","$time"));
			id.append("op", "$op");
			Document groupfield = new Document("_id",id);
			groupfield.append("count", new Document("$sum",1));
			Document group = new Document("$group",groupfield);
			List<Document> cond = new ArrayList<Document>();
			cond.add(group);
			
			AggregateIterable<Document> AIterable = collection.aggregate(cond);
	        MongoCursor<Document> mongoCursor = AIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 每周不同操作数量变化趋势
	 * @return
	 * Document形式<br>
	 *{<br>
	 *	 _id:{<br>
	 *		year:int,<br>
	 *		week:int,<br>
	 *		op:String //操作类型<br>
	 *	 },<br>
	 *	 count:int<br>
	 *}<br>
	 */
	public static List<Document> trendByWeek(MongoCollection<Document> collection){
		try{    
			Document id = new Document("year",new Document("$year","$time"));
			id.append("week",new Document("$week","$time"));
			id.append("op", "$op");
			Document groupfield = new Document("_id",id);
			groupfield.append("count", new Document("$sum",1));
			Document group = new Document("$group",groupfield);
			List<Document> cond = new ArrayList<Document>();
			cond.add(group);
			
			AggregateIterable<Document> AIterable = collection.aggregate(cond);
	        MongoCursor<Document> mongoCursor = AIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 查看确定用户的操作
	 * @param user 用户id
	 * @return 具体内容在insertLog已有描述
	 *Document形式<br>
	 *{<br>
	 *	 time:Date,<br>
	 *	 user:String,<br>
	 *	 op:String,<br>
	 *	 ns:String,<br>
	 *	 params:Document,<br>
	 *	 re:boolean<br>
	 *}<br>
	 */
	public static List<Document> userOp(MongoCollection<Document> collection,String user){
		try{
	        FindIterable<Document> findIterable = collection.find(new Document("user",user))
	        		.sort(new Document("time",-1)).limit(20);
	        MongoCursor<Document> mongoCursor = findIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}
	/**
	 * 确定用户各种操作统计
	 * @param user 用户id
	 * @return
	 * Document形式<br>
	 * {<br>
	 * 	 _id:String,//操作类型<br>
	 * 	 count:int<br>
	 * }<br>
	 */
	public static List<Document> userOpCount(MongoCollection<Document> collection,String user){
		try{
			Document groupfield = new Document("_id","$op");
			groupfield.append("count", new Document("$sum",1));
			Document group = new Document("$group",groupfield);
			Document match = new Document("$match",new Document("user",user));
			List<Document> cond = new ArrayList<Document>();
			cond.add(match);
			cond.add(group);
			
			AggregateIterable<Document> AIterable = collection.aggregate(cond);
	        MongoCursor<Document> mongoCursor = AIterable.iterator();
	        List<Document> result = new ArrayList<Document>();
	        while(mongoCursor.hasNext()){
	        	result.add(mongoCursor.next());
	        }
	        return result;
		}catch(MongoException e) {
            e.printStackTrace();
	    }
		return null;
	}

}
