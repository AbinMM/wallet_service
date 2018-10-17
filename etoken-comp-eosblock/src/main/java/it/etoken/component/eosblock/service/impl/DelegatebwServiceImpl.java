package it.etoken.component.eosblock.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.Delegatebw;
import it.etoken.base.model.eosblock.entity.DelegatebwExample;
import it.etoken.base.model.eosblock.entity.DelegatebwExample.Criteria;
import it.etoken.component.eosblock.dao.mapper.DelegatebwMapper;
import it.etoken.component.eosblock.service.DelegatebwService;
@Component
@Transactional
public class DelegatebwServiceImpl implements DelegatebwService{
	
	private final static Logger logger = LoggerFactory.getLogger(DelegatebwServiceImpl.class);

	@Autowired
	DelegatebwMapper delegatebwMapper;

	@Override
	public void save(Delegatebw delegatebw) {
		try {
			delegatebwMapper.insert(delegatebw);
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public Delegatebw findByAccountName(String account_name) {
		try {
			DelegatebwExample example=new DelegatebwExample();
		    Criteria  criteria = example.createCriteria();
		    criteria.andAccountNameEqualTo(account_name);
		    criteria.andStatusEqualTo(0L);
			List<Delegatebw> delegatebws= delegatebwMapper.selectByExample(example);
			if(delegatebws.size()>0) {
				return delegatebws.get(0);
			}else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public void update(Delegatebw delegatebw) {
		try {
			DelegatebwExample example=new DelegatebwExample();
		    Criteria  criteria = example.createCriteria();
		    criteria.andAccountNameEqualTo(delegatebw.getAccountName());
		    criteria.andIdEqualTo(delegatebw.getId());
			delegatebwMapper.updateByExampleSelective(delegatebw,example);
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public List<Delegatebw> findByCreateDate() {
		try {
			Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			calendar1.add(Calendar.DATE, -3);
			String three_days_ago = sdf.format(calendar1.getTime());
			DelegatebwExample example=new DelegatebwExample();
		    Criteria  criteria = example.createCriteria();
		    criteria.andStatusEqualTo(0L);
		    criteria.andCreatedateLessThanOrEqualTo(sdf.parse(three_days_ago));
		    List<Delegatebw> result=delegatebwMapper.selectByExample(example);
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
	public List<Delegatebw> findAllDelegatebw() throws MLException {
		try {
			DelegatebwExample example=new DelegatebwExample();
		    Criteria  criteria = example.createCriteria();
		    criteria.andIdLessThan(755L);
		    criteria.andIdGreaterThan(601L);
		   // criteria.andStatusEqualTo(1L);
		    List<Delegatebw> result=delegatebwMapper.selectByExample(example);
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
