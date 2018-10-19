package it.etoken.component.user.service;

import java.util.List;

import it.etoken.base.model.user.entity.Sysconf;

public interface SysconfService {
	public List<Sysconf> findAll();

	public int updateByName(Sysconf sysconf);
}
