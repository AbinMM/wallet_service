package it.etoken.component.news.service;

import java.util.List;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.news.entity.SysNotification;
import com.github.pagehelper.Page;

public interface SysNotificationService {
	
	public Page<SysNotification> findAll(int page,int pageSize);
	
	public SysNotification saveUpdate(SysNotification sysNotification) throws MLException;
	
	public void delete(Long id) throws MLException;

	List<SysNotification> findByTimeAndStatus();

}
