package it.etoken.componet.eosblock.facade;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;

public interface TransactionsFacadeAPI {


	public MLResultList<JSONObject> findByAccountAndActorNew(String last_id, int pageSize,String account,String actor,String code);
	
	public MLResultObject<Boolean>  findAccountCoins(String account,String coinName,String actor);
	
	public MLResultList<JSONObject> findByAccountAndActor(int page, int pageSize,String account,String actor,String code);

	public MLResultList<JSONObject> getEosTransactionRecord(int start, int count, String account, String sort,
			String token, String contract);
	
	public MLResultList<JSONObject> findAllTransferInByAccountAndTokenName(String account,  String tokenName, String to, int page, int pageCount);
	
	public MLResultList<JSONObject> getActions(String last_id, int pageSize, String account, String actor, String code, String type);
}
