package it.etoken.component.eosblock.service;


import java.util.List;

import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLPage;
import it.etoken.base.model.eosblock.entity.DappInfo;

public interface DappInfoService {

	public MLPage<DappInfo> findAllByPage(int page, int pageSize, String name) throws MLException;
	
	public Page<DappInfo> findAll(int page,int pageSize,String name) throws  MLException;

	public List<DappInfo> findAllRecommend() throws  MLException;

	public List<DappInfo> findByName(String name) throws  MLException;
	
	public DappInfo saveUpdate(DappInfo dappInfo) throws MLException;
	
	public void delete(Long id) throws MLException;
	
	public DappInfo findById(Long id) throws MLException;
}
