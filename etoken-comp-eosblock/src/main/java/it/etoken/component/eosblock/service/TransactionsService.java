package it.etoken.component.eosblock.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;


public interface TransactionsService {
	
	public List<JSONObject> findByAccountAndActorNew(String last_id, int pageSize,String account,String actor,String code);
	
	public List<BasicDBObject> findAccountCoins(String account,String actor);
	
	public List<JSONObject> findByAccountAndActor(int page, int pageSize,String account,String actor,String code);

	Map<String, String> findETExchangeExactPrice(Object[] trsationId);

	Map<String, String> findSellRamExactPrice(Object[] trsationId);

	public List<JSONObject> getEosTransactionRecord(int start, int count, String account, String sort, String token,
			String contract);

}
