package it.etoken.component.eosblock.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.cache.service.CacheService;
import it.etoken.component.eosblock.service.RamLargeRankSecondaryService;

@Component
@Transactional
public class RamLargeRankSecondaryServiceImpl implements RamLargeRankSecondaryService{

	@Autowired
	@Qualifier(value = "secondaryMongoTemplate")
	MongoOperations mongoTemplate;
	
	@Autowired
	CacheService cacheService;
	
	@Override
	public List<BasicDBObject> getNewestRank() {
		Query query = new Query();
		query = query.with(new Sort(new Order(Direction.DESC, "ramQuota")));
		query = query.limit(20);
		List<BasicDBObject> RamLargeRankList = mongoTemplate.find(query, BasicDBObject.class, "ram_large_user_rank");
		return RamLargeRankList;
	}

	@Override
	public void getLargeRank() {
		try {
			  String url = "http://api.ram.southex.com/v1/getram/rank";
		      String result = HttpClientUtils.doPostJson(url, "");
		      JSONObject json=JSONObject.parseObject(result); 
		      JSONArray jsonarray = json.getJSONArray("users");
		      if(jsonarray.size()==0) {
		    	  return;
		      }
		      Query query = new Query();
		      mongoTemplate.remove(query, "ram_large_user_rank");
		      for(int i=0;i<jsonarray.size();i++){
		    	  JSONObject user = jsonarray.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
		    	  Double ramQuota=user.getDouble("ramQuota");
		    	  BasicDBObject bdo = new BasicDBObject();
		    	  bdo.put("ramProportion100", json.get("ramProportion100"));
		    	  bdo.put("ramProportion200", json.get("ramProportion200"));
		    	  bdo.put("lastUpdateAt", json.get("lastUpdateAt"));
		    	  bdo.put("totalRamSelled", user.get("totalRamSelled"));
		    	  bdo.put("totalEosEarn", user.get("totalEosEarn"));
		    	  bdo.put("totalProfit", user.get("totalProfit"));
		    	  bdo.put("ramQuota",ramQuota);//可售内存
		    	  bdo.put("holdCost", user.get("holdCost"));
		    	  bdo.put("totalEosCost", user.get("totalEosCost"));
		    	  bdo.put("historyAverageCost", user.get("historyAverageCost"));
		    	  bdo.put("profit", user.get("profit"));
		    	  bdo.put("account", user.get("account"));
		    	  bdo.put("ramValue", user.get("ramValue"));
		    	  bdo.put("createdAt", System.currentTimeMillis());
		    	  bdo.put("updatedAt", System.currentTimeMillis());
		    	  mongoTemplate.insert(bdo, "ram_large_user_rank");
		    	  }  
		  	Query query2 = new Query();
			query2 = query2.with(new Sort(new Order(Direction.DESC, "ramQuota")));
			query2 = query2.limit(20);
			List<BasicDBObject> RamLargeRankList = mongoTemplate.find(query2, BasicDBObject.class, "ram_large_user_rank");
//			cacheService.set("ram_large_user_rank", RamLargeRankList,65*60);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
		
	}
}
