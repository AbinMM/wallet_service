package it.etoken.component.user.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.user.entity.Sysconf;
import it.etoken.component.user.service.SysconfService;
import it.etoken.componet.user.facade.SysconfFacadeAPI;

@Service(version = "1.0.0")
public class SysconfFacadeAPIImpl implements SysconfFacadeAPI {

	private final static Logger logger = LoggerFactory.getLogger(SysconfFacadeAPIImpl.class);

	@Autowired
	SysconfService sysconfService;

	@Override
	public MLResultList<Sysconf> findAll() {
		try {
			List<Sysconf> list = sysconfService.findAll();
			return new MLResultList<Sysconf>(list);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<Sysconf>(e);
		}
	}

	@Override
	public MLResultObject<Boolean> updateByName(Sysconf sysconf) {
		try {
			int result = sysconfService.updateByName(sysconf);
			if (result > 0) {
				return new MLResultObject<Boolean>(true);
			}
			return new MLResultObject<Boolean>(false);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<Boolean>(e);
		}
	}
}
