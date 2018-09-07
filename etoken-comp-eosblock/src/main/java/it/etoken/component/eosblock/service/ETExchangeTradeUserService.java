package it.etoken.component.eosblock.service;

import java.util.List;

import com.mongodb.BasicDBObject;

public interface ETExchangeTradeUserService {
	public List<BasicDBObject> getNewestRank();
	public void getEtTradeUserInfo(int pastHours);
}
