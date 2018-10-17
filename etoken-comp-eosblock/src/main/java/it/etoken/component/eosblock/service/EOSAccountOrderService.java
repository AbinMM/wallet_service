package it.etoken.component.eosblock.service;

import java.util.Map;

import it.etoken.base.model.eosblock.entity.EosAccountOrder;

public interface EOSAccountOrderService {
	/**
	 * 保存订单
	 * @param eOSAccountOrder
	 * @return
	 */
	public EosAccountOrder saveUpdate(EosAccountOrder eOSAccountOrder);
	
	/**
	 * 创建订单，并请求微信预创建订单
	 * @param eOSAccountOrder
	 * @return
	 */
	public Map<String, String> createWxOrder(EosAccountOrder eOSAccountOrder);

	/**
	 * 根据订单号查询订单
	 * @param orderNo
	 * @return
	 */
	public EosAccountOrder queryByOrderNo(String orderNo);
	
	/**
	 * 根据eos账号和Owner公钥查询订单
	 * @param accountName
	 * @param ownerPublicKey
	 * @return
	 */
	public EosAccountOrder checkByAccountNameAndOwnerPublicKey(String accountName, String ownerPublicKey);

	/**
	 * 通知处理
	 * @param params
	 * @return
	 */
	public String notify(String strXML);
	
	/**
	 * 根据订单号创建EOS账号
	 * @param orderNo
	 */
	public void createEosAccount(String orderNo);
}
