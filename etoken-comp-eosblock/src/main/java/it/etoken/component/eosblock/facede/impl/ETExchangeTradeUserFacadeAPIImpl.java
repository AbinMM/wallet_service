package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.component.eosblock.service.ETExchangeTradeUserService;
import it.etoken.componet.eosblock.facade.ETExchangeTradeUserFacadeAPI;
@Service(version = "1.0.0")
public class ETExchangeTradeUserFacadeAPIImpl implements ETExchangeTradeUserFacadeAPI{
	
	private final static Logger logger = LoggerFactory.getLogger(RamLargeRankFacadeAPIImpl.class);
	
	@Autowired
	ETExchangeTradeUserService eTExchangeTradeUserService;

	@Override
	public MLResultList<JSONObject> getNewRankByCode(String code) {
		try {
			List<JSONObject> result= eTExchangeTradeUserService.getNewRankByCode(code);
			return new MLResultList<JSONObject>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<JSONObject>(e);
		}
	}
	
	
}
