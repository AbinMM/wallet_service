package it.etoken.component.eosblock.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.base.model.eosblock.entity.DappCategoryExample;
import it.etoken.component.eosblock.dao.mapper.DappCategoryMapper;
import it.etoken.component.eosblock.service.DappCategoryService;
@Component
@Transactional
public class DappCategoryServiceImpl implements DappCategoryService{

	private final static Logger logger = LoggerFactory.getLogger(DappCategoryServiceImpl.class);

	@Autowired
	DappCategoryMapper dappCategoryMapper;
	
	@Override
	public MLPage<DappCategory> findAllByPage(int page, int pageSize, String name) throws MLException {
		try {
			Page<DappCategory> result = PageHelper.startPage(page, pageSize);
			DappCategoryExample example = new DappCategoryExample();
			example.setOrderByClause("seq desc");
			if (null != name && !name.isEmpty()) {
				DappCategoryExample.Criteria criteria = example.createCriteria();
				criteria.andNameLike(name);
			}
			dappCategoryMapper.selectByExample(example);
			return new MLPage<DappCategory>(result.getResult(), result.getTotal());
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
	}
	
	@Override
	public List<DappCategory> findAll(int page,int pageSize,String name) {
		try {	    
		    Page<DappCategory> result = PageHelper.startPage(page,pageSize);  
		    DappCategoryExample example=new DappCategoryExample();
			example.setOrderByClause("seq desc");
			if(null != name && !name.isEmpty()) {
				DappCategoryExample.Criteria criteria=example.createCriteria();
				criteria.andNameLike(name);
			}
			dappCategoryMapper.selectByExample(example);
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
	public DappCategory saveUpdate(DappCategory dappCategory) throws MLException {
		try{
			if(dappCategory.getId()==null){
				dappCategoryMapper.insertSelective(dappCategory);
			}else{
				dappCategoryMapper.updateByPrimaryKeySelective(dappCategory);
			}
			return dappCategory;
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
			dappCategoryMapper.deleteByPrimaryKey(id);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public DappCategory findById(Long id) throws MLException {
		try{
			return dappCategoryMapper.selectByPrimaryKey(id);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}
}
