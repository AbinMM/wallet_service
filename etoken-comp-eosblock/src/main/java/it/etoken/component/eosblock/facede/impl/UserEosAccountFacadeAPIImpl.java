package it.etoken.component.eosblock.facede.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.UserEosAccount;
import it.etoken.component.eosblock.service.UserEosAccountService;
import it.etoken.componet.eosblock.facade.UserEosAccountFacadeAPI;

@Service(version = "1.0.0")
public class UserEosAccountFacadeAPIImpl implements UserEosAccountFacadeAPI{
	private final static Logger logger = LoggerFactory.getLogger(UserEosAccountFacadeAPIImpl.class);

	@Autowired
	UserEosAccountService userEosAccountservice;
	
	@Override
	public MLResult save(UserEosAccount userEosAccount) {
		try {
			userEosAccountservice.save(userEosAccount);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(e);
		}
	}

	@Override
	public MLResult update(UserEosAccount userEosAccount) {
		try {
			userEosAccountservice.update(userEosAccount);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(e);
		}
	}

	@Override
	public MLResultObject<UserEosAccount> findbyUidAndAccount(String uid, String account) {
		try {
			UserEosAccount userEosAccount=userEosAccountservice.findbyUidAndAccount(uid,account);
			return new MLResultObject<UserEosAccount>(userEosAccount);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<UserEosAccount>(e);
		}
	}

}
