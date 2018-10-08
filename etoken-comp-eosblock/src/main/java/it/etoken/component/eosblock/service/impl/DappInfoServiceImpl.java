package it.etoken.component.eosblock.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;


import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.base.model.eosblock.entity.DappInfoExample;
import it.etoken.base.model.eosblock.entity.DappInfoExample.Criteria;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.component.eosblock.dao.mapper.DappInfoMapper;
import it.etoken.component.eosblock.service.DappInfoService;

@Component
@Transactional
public class DappInfoServiceImpl implements DappInfoService{

	private final static Logger logger = LoggerFactory.getLogger(DappInfoServiceImpl.class);
	
	@Autowired
	DappInfoMapper dappInfoMapper;

	@Override
	public Page<DappInfo> findAll(int page,int pageSize,String name) {
		try {
			Page<DappInfo> result = PageHelper.startPage(page,pageSize);  
			DappInfoExample example=new DappInfoExample();
			example.setOrderByClause("seq desc");
			if(null != name && !name.isEmpty()) {
				DappInfoExample.Criteria criteria=example.createCriteria();
				criteria.andNameLike(name);
			}
		    dappInfoMapper.selectByExample(example);
		    
		    return result;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
		
	}

	@Override 
	public List<DappInfo> findAllRecommend() throws MLException {
	try {
			DappInfoExample example=new DappInfoExample();
			Criteria criteria=example.createCriteria();
			criteria.andIsRecommendEqualTo("y");
			List<DappInfo> result= dappInfoMapper.selectByExample(example);
		    
		    return result;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public List<DappInfo> findByName(String name) throws MLException {
	try {

			DappInfoExample example=new DappInfoExample();
			Criteria criteria=example.createCriteria();
			criteria.andNameLike(name);
			List<DappInfo> result=dappInfoMapper.selectByExample(example);
		    
		    return result;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
	
	@Override
	public DappInfo saveUpdate(DappInfo dappInfo) throws MLException {
		try{
			if(dappInfo.getId()==null){
				dappInfoMapper.insertSelective(dappInfo);
			}else{
				dappInfoMapper.updateByPrimaryKeySelective(dappInfo);
			}
			return dappInfo;
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public void delete(Long id) throws MLException {
		try{
			dappInfoMapper.deleteByPrimaryKey(id);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
	
	@Override
	public DappInfo findById(Long id) throws MLException {
		try{
			return dappInfoMapper.selectByPrimaryKey(id);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
}
