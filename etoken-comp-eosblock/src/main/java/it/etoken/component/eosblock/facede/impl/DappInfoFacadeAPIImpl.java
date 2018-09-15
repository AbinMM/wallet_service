package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.component.eosblock.service.DappInfoService;
import it.etoken.componet.eosblock.facade.DappInfoFacadeAPI;

@Service(version = "1.0.0")
public class DappInfoFacadeAPIImpl implements DappInfoFacadeAPI{
	private final static Logger logger = LoggerFactory.getLogger(DappCategoryFacadeAPIImpl.class);

	@Autowired
	DappInfoService dappInfoService;
	
	@Override
	public MLResultList<DappInfo> findAll(int page,int pageSize,String name) {
		try {
			Page<DappInfo>  result= dappInfoService.findAll(page,pageSize,name);
			return new MLResultList<DappInfo>(result.getResult());
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<DappInfo>(e);
		}
	}

	@Override
	public MLResultList<DappInfo> findAllRecommend() {
		try {
			List<DappInfo>  result= dappInfoService.findAllRecommend();
			return new MLResultList<DappInfo>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<DappInfo>(e);
		}
	}

	@Override
	public MLResultList<DappInfo> findByName(String name) {
		try {
			List<DappInfo>  result= dappInfoService.findByName(name);
			return new MLResultList<DappInfo>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<DappInfo>(e);
		}
	}
	


}
