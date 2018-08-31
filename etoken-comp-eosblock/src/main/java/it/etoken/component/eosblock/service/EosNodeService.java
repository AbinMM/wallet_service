package it.etoken.component.eosblock.service;

import java.util.List;

import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.eosblock.entity.EosNode;

public interface EosNodeService {
	/**
	 * 保存
	 * @param eosNode
	 * @return
	 */
	public EosNode saveUpdate(EosNode eosNode)throws MLException;
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public void delete(Integer id)throws MLException;
	
	/**
	 * 分页查询
	 * @param userId
	 * @return
	 */
	public List<EosNode> findAll()throws MLException;
	
	/**
	 * 分页查询
	 * @param userId
	 * @return
	 */
	public Page<EosNode> findAllByPage(int page, int pageCount)throws MLException;
	
	/**
	 * 查询某一条记录
	 * @param id
	 * @return
	 */
	public EosNode findById(Integer id)throws MLException;
}
