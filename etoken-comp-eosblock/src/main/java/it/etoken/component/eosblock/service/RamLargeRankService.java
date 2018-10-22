package it.etoken.component.eosblock.service;

import java.util.List;

import com.mongodb.BasicDBObject;

public interface RamLargeRankService {
	
	public List<BasicDBObject> getNewestRank();
	
	//获取内存大户排行并且放到mongodb中
	public void getLargeRank();
	
	
	//获取半个小时大单交易的用户并查询他们的内存值跟内存前20的内存值比较如果大于并且放到mongodb中
	public void getNewLargeRank();
	
	//修改已经存在大户排名中的大户的内存量
	public void updateLargeRank();

}
