package it.etoken.componet.eosblock.facade;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.DappInfo;

public interface DappInfoFacadeAPI {
	
	public MLResultObject<MLPage<DappInfo>> findAllByPage(int page, int pageSize, String name);
	
	public MLResultList<DappInfo> findAll(int page,int pageSize,String name);
	
	public MLResultList<DappInfo> findAllRecommend();
	
	public MLResultList<DappInfo> findByName(String name);
	
	public MLResultObject<DappInfo> saveUpdate(DappInfo dappInfo) throws MLException;
	
	public MLResult delete(Long id) throws MLException;
	
	public MLResultObject<DappInfo> findById(Long id) throws MLException;

}
