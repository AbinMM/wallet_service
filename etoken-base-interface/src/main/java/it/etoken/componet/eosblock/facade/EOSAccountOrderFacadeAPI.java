package it.etoken.componet.eosblock.facade;

import java.util.Map;

import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.EosAccountOrder;

public interface EOSAccountOrderFacadeAPI {
	
	public MLResultObject<EosAccountOrder> queryByOrderNo(String orderNo);
	
	public MLResultObject<EosAccountOrder> checkByAccountNameAndOwnerPublicKey(String accountName, String ownerPublicKey);
	
	public MLResultObject<Map<String, String>> createWxOrder(EosAccountOrder eOSAccountOrder);

	public MLResultObject<String> notify(String strXML);
}
