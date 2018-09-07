package it.etoken.component.eosblock.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface ETExchangeTradeUserService {
	/**
	 * 根据code获取持币前20名用户
	 * @param code 币代码
	 * @return
	 */
	public List<JSONObject> getNewRankByCode(String code);
	
	/**
	 * 获取最近pastHours小时参与交易的用户
	 * @param pastHours 最近小时数
	 */
	public void getEtTradeUserInfo(int pastHours);
}
