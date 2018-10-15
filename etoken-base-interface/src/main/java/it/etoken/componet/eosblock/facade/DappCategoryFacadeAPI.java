package it.etoken.componet.eosblock.facade;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.DappCategory;

public interface DappCategoryFacadeAPI {
	
	public MLResultObject<MLPage<DappCategory>> findAllByPage(int page, int pageSize, String name);
	
	public MLResultList<DappCategory> findAll(int page,int pageSize,String name);
	
    public MLResultObject<DappCategory> saveUpdate(DappCategory dappCategory) throws MLException;
	
	public MLResult delete(Long id) throws MLException;
	
	public MLResultObject<DappCategory> findById(Long id) throws MLException;

}
