package it.etoken.component.news.service.impl;

import java.util.Date;
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
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.base.model.eosblock.entity.DappInfoExample;
import it.etoken.base.model.news.entity.SysNotification;
import it.etoken.base.model.news.entity.SysNotificationExample;
import it.etoken.base.model.news.entity.SysNotificationExample.Criteria;
import it.etoken.component.news.dao.mapper.SysNotificationMapper;
import it.etoken.component.news.service.SysNotificationService;

@Component
@Transactional
public class SysNotificationServiceImpl implements SysNotificationService{
	
private final static Logger logger = LoggerFactory.getLogger(SysNotificationServiceImpl.class);
	
	@Autowired
	private SysNotificationMapper sysNotificationMapper;
	
	
	@Override
	public MLPage<SysNotification> findAll(int page,int pageSize) {
		try {
			Page<SysNotification> result = PageHelper.startPage(page,pageSize);  
			SysNotificationExample example=new SysNotificationExample();
			example.setOrderByClause("seq desc");
			sysNotificationMapper.selectByExample(example);
		    
			return new MLPage<SysNotification>(result.getResult(), result.getTotal());
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
		
	}

	@Override
	public List<SysNotification> findByTimeAndStatus() {
		try {
			SysNotificationExample example=new SysNotificationExample();
			Criteria criteria=example.createCriteria();
			criteria.andStarttimeGreaterThanOrEqualTo(new Date());
			criteria.andEndtimeLessThanOrEqualTo(new Date());
			criteria.andStatusEqualTo(0L);
			
			Criteria criteria1=example.createCriteria();
			criteria1.andStarttimeIsNull();
			criteria1.andEndtimeIsNull();
			criteria1.andStatusEqualTo(0L);
	
			example.or(criteria1);
			example.setOrderByClause("modifydate desc");
			List<SysNotification> list=sysNotificationMapper.selectByExample(example);
			return list;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public SysNotification saveUpdate(SysNotification sysNotification) throws MLException {
		try{
			if(sysNotification.getId()==null){
				sysNotificationMapper.insertSelective(sysNotification);
			}else{
				sysNotificationMapper.updateByPrimaryKeySelective(sysNotification);
			}
			return sysNotification;
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
			sysNotificationMapper.deleteByPrimaryKey(id);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		}catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	

}
