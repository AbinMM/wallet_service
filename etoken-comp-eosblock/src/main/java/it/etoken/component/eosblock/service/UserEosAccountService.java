package it.etoken.component.eosblock.service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.UserEosAccount;

public interface UserEosAccountService {
	
	void save(UserEosAccount userEosAccount) throws MLException;
	
	void update(UserEosAccount userEosAccount) throws MLException;

	UserEosAccount findbyUidAndAccount(String uid, String account) throws MLException;

}
