package it.etoken.componet.eosblock.facade;

import com.mongodb.BasicDBObject;

import it.etoken.base.common.result.MLResultList;

public interface RamLargeRankFacadeAPI {

	public MLResultList<BasicDBObject> getNewestRank();
	
}
