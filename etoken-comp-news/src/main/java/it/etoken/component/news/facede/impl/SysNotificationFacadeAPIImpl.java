package it.etoken.component.news.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.base.model.news.entity.SysNotification;
import it.etoken.base.model.user.vo.LoginUser;
import it.etoken.component.news.service.NewsTypeService;
import it.etoken.component.news.service.SysNotificationService;
import it.etoken.componet.news.facade.SysNotificationFacadeAPI;

@Service(version = "1.0.0")
public class SysNotificationFacadeAPIImpl implements SysNotificationFacadeAPI{

	private final static Logger logger = LoggerFactory.getLogger(SysNotificationFacadeAPIImpl.class);

	@Autowired
	SysNotificationService sysNotificationService;
	
	@Override
	public MLResultObject<MLPage<SysNotification>> findAll(int page, int pageSize) {
		try {
			MLPage<SysNotification> result= sysNotificationService.findAll(page,pageSize);
			return new MLResultObject<MLPage<SysNotification>>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultObject<MLPage<SysNotification>>(e);
		}
	}

	
	@Override
	public MLResultList<SysNotification> findByTimeAndStatus() {
		try {
			List<SysNotification> list=sysNotificationService.findByTimeAndStatus();
			return new MLResultList<SysNotification>(list);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<SysNotification>(e);
		}
	}

	@Override
	public MLResultObject<SysNotification> saveUpdate(SysNotification sysNotification) throws MLException {
		try {
			SysNotification result = sysNotificationService.saveUpdate(sysNotification);
			return new MLResultObject<SysNotification>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<SysNotification>(e);
		}
	}

	@Override
	public MLResult delete(Long id) throws MLException {
		try {
			sysNotificationService.delete(id);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(false);
		}
	}


	
}
