package it.etoken.component.eosblock.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.UserEosAccount;
import it.etoken.base.model.eosblock.entity.UserEosAccountExample;
import it.etoken.base.model.eosblock.entity.UserEosAccountExample.Criteria;
import it.etoken.component.eosblock.dao.mapper.UserEosAccountMapper;
import it.etoken.component.eosblock.service.UserEosAccountService;


@Component
@Transactional
public class UserEosAccountServiceImpl implements UserEosAccountService {
	private final static Logger logger=LoggerFactory.getLogger(UserEosAccountService.class);
		
	@Autowired
	UserEosAccountMapper userEosAccountMapper;


	@Override
	public void save(UserEosAccount userEosAccount) throws MLException {
		try {
			userEosAccountMapper.insert(userEosAccount);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
		    throw new MLException(MLCommonException.system_err);
		}
		
	}

	@Override
	public void update(UserEosAccount userEosAccount) throws MLException {
        try {
        	userEosAccountMapper.updateByPrimaryKey(userEosAccount);
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
		    throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public UserEosAccount findbyUidAndAccount(String uid, String account) throws MLException {
		try {
			UserEosAccountExample example=new UserEosAccountExample();
			Criteria criteria=example.createCriteria();
			if(null!=uid) {
				criteria.andUidEqualTo(Long.valueOf(uid));
			}else {
				criteria.andUidIsNull();
			}
			criteria.andEosAccountEqualTo(account);
			List<UserEosAccount> list=userEosAccountMapper.selectByExample(example);
			if(list.size()>0) {
				return list.get(0);
			}
			
		}catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
		    throw new MLException(MLCommonException.system_err);
		}
		return null;
	}

}
