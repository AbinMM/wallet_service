package it.etoken.componet.eosblock.facade;

import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.result.MLResultList;

public interface ETExchangeTradeUserFacadeAPI {

	public MLResultList<JSONObject> getNewRankByCode(String code);
	
}
