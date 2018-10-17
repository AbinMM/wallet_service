package it.etoken.componet.news.facade;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.news.entity.SysNotification;

public interface SysNotificationFacadeAPI {
	
	public MLResultObject<MLPage<SysNotification>> findAll(int page,int pageSize);
	
	public MLResultList<SysNotification> findByTimeAndStatus();
	
	public MLResultObject<SysNotification> saveUpdate(SysNotification sysNotification) throws MLException;
	
	public MLResult delete(Long id) throws MLException;

}
