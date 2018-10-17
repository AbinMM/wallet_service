package it.etoken.component.eosblock.facede.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.EosAccountOrder;
import it.etoken.component.eosblock.service.EOSAccountOrderService;
import it.etoken.componet.eosblock.facade.EOSAccountOrderFacadeAPI;

@Service(version = "1.0.0")
public class EOSAccountOrderFacadeAPIImpl implements EOSAccountOrderFacadeAPI {
	private final static Logger logger = LoggerFactory.getLogger(EOSAccountOrderFacadeAPIImpl.class);

	@Autowired
	EOSAccountOrderService eOSAccountOrderService;
	
	@Override
	public MLResultObject<EosAccountOrder> queryByOrderNo(String orderNo) {
		try {
			EosAccountOrder result = eOSAccountOrderService.queryByOrderNo(orderNo);
			return new MLResultObject<EosAccountOrder>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<EosAccountOrder>(e);
		}
	}

	@Override
	public MLResultObject<EosAccountOrder> checkByAccountNameAndOwnerPublicKey(String accountName, String ownerPublicKey) {
		try {
			EosAccountOrder result = eOSAccountOrderService.checkByAccountNameAndOwnerPublicKey(accountName, ownerPublicKey);
			return new MLResultObject<EosAccountOrder>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<EosAccountOrder>(e);
		}
	}

	@Override
	public MLResultObject<Map<String, String>> createWxOrder(EosAccountOrder eOSAccountOrder) {
		try {
			Map<String, String> result = eOSAccountOrderService.createWxOrder(eOSAccountOrder);
			return new MLResultObject<Map<String, String>>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<Map<String, String>>(e);
		}
	}

	@Override
	public MLResultObject<String> notify(String strXML) {
		try {
			String result = eOSAccountOrderService.notify(strXML);
			return new MLResultObject<String>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<String>(e);
		}
	}

}
