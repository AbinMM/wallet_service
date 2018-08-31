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
import it.etoken.base.model.eosblock.entity.EosNode;
import it.etoken.base.model.eosblock.entity.EosNodeExample;
import it.etoken.component.eosblock.dao.mapper.EosNodeMapper;
import it.etoken.component.eosblock.service.EosNodeService;

@Component
@Transactional
public class EosNodeServiceImpl implements EosNodeService {
	private final static Logger logger = LoggerFactory.getLogger(EosNodeServiceImpl.class);
	@Autowired
	private EosNodeMapper eosNodeMapper;
	
	@Override
	@CacheEvict(value="eosNodeCache",allEntries=true)
	public EosNode saveUpdate(EosNode eosNode) throws MLException {
		try {
			if (eosNode.getId() == null) {
				eosNodeMapper.insertSelective(eosNode);
			} else {
				eosNodeMapper.updateByPrimaryKeySelective(eosNode);
			}
			return eosNode;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	@CacheEvict(value="eosNodeCache",allEntries=true)
	public void delete(Integer id) throws MLException {
		try {
		eosNodeMapper.deleteByPrimaryKey(id);
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
	
	@Override
//	@Cacheable(value="eosNodeCache",keyGenerator="wiselyKeyGenerator") 
	public List<EosNode> findAll() throws MLException {
		try{
			EosNodeExample example = new EosNodeExample();
			List<EosNode> result = eosNodeMapper.selectByExample(example);
			return result;
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	@Cacheable(value="eosNodeCache",keyGenerator="wiselyKeyGenerator")
	public Page<EosNode> findAllByPage(int page, int pageCount) throws MLException {
		try{
			Page<EosNode> result = PageHelper.startPage(page, pageCount);  
			EosNodeExample example = new EosNodeExample();
			eosNodeMapper.selectByExample(example);
			return result;
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	@Cacheable(value="eosNodeCache",keyGenerator="wiselyKeyGenerator")
	public EosNode findById(Integer id) throws MLException {
		try{
			EosNode result = eosNodeMapper.selectByPrimaryKey(id);
			return result;
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
	
}
