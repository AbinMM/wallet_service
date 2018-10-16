package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.component.eosblock.service.DappCategoryService;
import it.etoken.componet.eosblock.facade.DappCategoryFacadeAPI;


@Service(version = "1.0.0")
public class DappCategoryFacadeAPIImpl implements DappCategoryFacadeAPI{
	private final static Logger logger = LoggerFactory.getLogger(DappCategoryFacadeAPIImpl.class);
	
	@Autowired
	DappCategoryService dappCategoryService;
	
	@Override
	public MLResultObject<MLPage<DappCategory>> findAllByPage(int page, int pageSize, String name) {
		try {
			MLPage<DappCategory> result = dappCategoryService.findAllByPage(page, pageSize, name);
			return new MLResultObject<MLPage<DappCategory>>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<MLPage<DappCategory>>(e);
		}
	}

	@Override
	public MLResultList<DappCategory> findAll(int page,int pageSize,String name) {
		try {
			List<DappCategory>  result= dappCategoryService.findAll(page, pageSize,name);
			return new MLResultList<DappCategory>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return  new MLResultList<DappCategory>(e);
		}
	}

	@Override
	public MLResultObject<DappCategory> saveUpdate(DappCategory dappCategory) throws MLException {
		try {
			DappCategory result = dappCategoryService.saveUpdate(dappCategory);
			return new MLResultObject<DappCategory>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<DappCategory>(e);
		}
	}

	@Override
	public MLResult delete(Long id) throws MLException {
		try {
			dappCategoryService.delete(id);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(false);
		}
	}

	@Override
	public MLResultObject<DappCategory> findById(Long id) throws MLException {
		try {
			DappCategory result = dappCategoryService.findById(id);
			return new MLResultObject<DappCategory>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<DappCategory>(e);
		}
	}
	

}
