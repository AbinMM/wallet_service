package it.etoken.componet.eosblock.facade;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.model.eosblock.entity.DappInfo;

public interface DappInfoFacadeAPI {
	
	public MLResultList<DappInfo> findAll(int page,int pageSize,String name);
	
	public MLResultList<DappInfo> findAllRecommend();
	
	public MLResultList<DappInfo> findByName(String name);

}
