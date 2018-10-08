package it.etoken.component.eosblock.service;

import java.util.List;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.DappCategory;

public interface DappCategoryService {

	List<DappCategory> findAll(int page, int pageSize, String name);

	DappCategory saveUpdate(DappCategory dappCategory) throws  MLException;

	void delete(Long id) throws  MLException;

	DappCategory findById(Long id) throws  MLException;

}
