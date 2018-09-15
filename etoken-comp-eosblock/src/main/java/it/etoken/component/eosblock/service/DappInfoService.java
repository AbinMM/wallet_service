package it.etoken.component.eosblock.service;


import java.util.List;

import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.DappInfo;

public interface DappInfoService {

	Page<DappInfo> findAll(int page,int pageSize,String name) throws  MLException;

	List<DappInfo> findAllRecommend() throws  MLException;

	List<DappInfo> findByName(String name) throws  MLException;
}
