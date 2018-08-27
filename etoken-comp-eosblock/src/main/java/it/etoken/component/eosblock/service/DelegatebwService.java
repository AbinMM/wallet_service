package it.etoken.component.eosblock.service;

import java.util.List;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.Delegatebw;

public interface DelegatebwService {

    public void save(Delegatebw delegatebw) throws  MLException;
	
	public Delegatebw findByAccountName(String account_name)  throws  MLException;

	public void update(Delegatebw delegatebw)  throws  MLException;

	public List<Delegatebw> findByCreateDate()  throws  MLException;
}
