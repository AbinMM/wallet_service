package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.base.model.eosblock.entity.Delegatebw;
import it.etoken.component.eosblock.service.DappCategoryService;
import it.etoken.componet.eosblock.facade.DappCategoryFacadeAPI;


@Service(version = "1.0.0")
public class DappCategoryFacadeAPIImpl implements DappCategoryFacadeAPI{
	private final static Logger logger = LoggerFactory.getLogger(DappCategoryFacadeAPIImpl.class);
	
	@Autowired
	DappCategoryService dappCategoryService;

	@Override
	public MLResultList<DappCategory> findAll() {
		try {
			List<DappCategory>  result= dappCategoryService.findAll();
			return new MLResultList<DappCategory>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<DappCategory>(e);
		}
	}
	
	

}
