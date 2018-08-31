package it.etoken.component.eosblock.service;

import java.util.List;

import com.mongodb.BasicDBObject;

public interface RamLargeRankService {
	
	public List<BasicDBObject> getNewestRank();
	
	//获取内存大户排行并且放到mongodb中
	public void getLargeRank();
}
