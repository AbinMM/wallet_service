package it.etoken.component.eosblock.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.base.model.eosblock.entity.DappCategoryExample;
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.base.model.eosblock.entity.DappInfoExample;
import it.etoken.component.eosblock.dao.mapper.DappCategoryMapper;
import it.etoken.component.eosblock.dao.mapper.DappInfoMapper;
import it.etoken.component.eosblock.service.DappCategoryService;
@Component
@Transactional
public class DappCategoryServiceImpl implements DappCategoryService{

	private final static Logger logger = LoggerFactory.getLogger(DappCategoryServiceImpl.class);

	@Autowired
	DappCategoryMapper dappCategoryMapper;
	
	@Override
	public List<DappCategory> findAll() {
		try {
			DappCategoryExample example=new DappCategoryExample();
		    List<DappCategory> result=dappCategoryMapper.selectByExample(example);
		    return result;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
		
	}
}
