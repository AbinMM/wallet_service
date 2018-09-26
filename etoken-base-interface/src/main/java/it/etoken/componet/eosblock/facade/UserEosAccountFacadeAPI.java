package it.etoken.componet.eosblock.facade;


import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.UserEosAccount;

public interface UserEosAccountFacadeAPI {
	
	public MLResult save(UserEosAccount userEosAccount);
	
	public MLResult update(UserEosAccount userEosAccount);
	
	public MLResultObject<UserEosAccount> findbyUidAndAccount(String uid,String account);
}
