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
	
	/**
	 * 根据发币账号和币名，查询最新转入记录
	 * @param account 发币账号
	 * @param tokenName 币名
	 * @param to 转入账号
	 * @param page 第几页
	 * @param pageCount 每页条数
	 * @return
	 */
	public List<JSONObject> findAllTransferInByAccountAndTokenName(String account,  String tokenName, String to, int page, int pageCount);
	
	/**
	 * EOS转账记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param actor
	 * @param transferType
	 * @return
	 */
	public List<JSONObject> getActionsEosTransfer(String last_id, int pageSize, String actor, String transferType);

	/**
	 * EOS 抵押记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param delegateType
	 * @return
	 */
	public List<JSONObject> getActionsEosDelegatebw(String last_id, int pageSize, String account, String actor,
			String delegateType);

	/**
	 * EOS内存交易记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param tradeType
	 * @return
	 */
	public List<JSONObject> getActionsEosRam(String last_id, int pageSize, String account, String actor,
			String tradeType);

	/**
	 * EOS ET交易所币交易记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param tradeType
	 * @return
	 */
	public List<JSONObject> getActionsEosET(String last_id, int pageSize, String account, String actor,
			String tradeType);

	/**
	 * 非EOS ET交易所交易记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param code
	 * @param tradeType
	 * @return
	 */
	public List<JSONObject> getActionsOtherET(String last_id, int pageSize, String account, String actor, String code,
			String tradeType);

	/**
	 * 非EOS 转账记录
	 * 
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param code
	 * @param transferType
	 * @return
	 */
	public List<JSONObject> getActionsOtherTransfer(String last_id, int pageSize, String account, String actor,
			String code, String transferType);
	
	/**
	 * 获取交易记录
	 * @param last_id
	 * @param pageSize
	 * @param account
	 * @param actor
	 * @param code
	 * @param transferType
	 * @return
	 */
	public List<JSONObject> getActions(String last_id, int pageSize, String account, String actor, String code, String type);
}
