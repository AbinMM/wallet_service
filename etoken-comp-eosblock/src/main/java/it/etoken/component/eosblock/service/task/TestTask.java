package it.etoken.component.eosblock.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;

@Component
public class TestTask {

	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;

//	// 获取当前内存价格表的数据
//	@Scheduled(cron = "*/1 * * * * ?")
//	public void test() {
////		String collection_name = "test";
//////		if(!mongoTemplate.collectionExists(collection_name)) {
////			BasicDBObject record_date_keys = new BasicDBObject();
////			record_date_keys.put("record_date", -1);
////			BasicDBObject record_date_options = new BasicDBObject();
////			record_date_options.put("background", true);
////			record_date_options.put("unique", true);
////			mongoTemplate.getCollection(collection_name).createIndex(record_date_keys, record_date_options);
////			
////			BasicDBObject code_keys = new BasicDBObject();
////			code_keys.put("code", "hashed");
////			
////			BasicDBObject code_options = new BasicDBObject();
////			code_options.put("background", true);
////			mongoTemplate.getCollection(collection_name).createIndex(code_keys, code_options);
////		}
//
//	}
}
