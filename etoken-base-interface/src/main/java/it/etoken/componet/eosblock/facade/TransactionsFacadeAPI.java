package it.etoken.componet.eosblock.facade;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.result.MLResultList;

public interface TransactionsFacadeAPI {


	public MLResultList<JSONObject> findByAccountAndActorNew(String last_id, int pageSize,String account,String actor,String code);
	
	public MLResultList<BasicDBObject> findAccountCoins(String account,String actor);
	
	public MLResultList<JSONObject> findByAccountAndActor(int page, int pageSize,String account,String actor,String code);
	

}
