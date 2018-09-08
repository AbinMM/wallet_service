package it.etoken.component.eosblock.service;

import java.util.List;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.DappCategory;

public interface DappCategoryService {

	List<DappCategory> findAll() throws  MLException;

}
