package it.etoken.componet.user.facade;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.user.entity.Sysconf;

public interface SysconfFacadeAPI {

	public MLResultList<Sysconf> findAll();

	public MLResultObject<Boolean> updateByName(Sysconf sysconf);
}
