package it.etoken.component.market.facade.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.cache.service.CacheService;
import it.etoken.component.market.service.CoinsService;
import it.etoken.componet.coins.facade.CoinsFacadeAPI;

@Service(version = "1.0.0")
public class CoinsFacadeAPIImpl implements CoinsFacadeAPI {

	private final static Logger logger = LoggerFactory.getLogger(CoinsFacadeAPIImpl.class);

	@Autowired
	CoinsService coinsService;
	
	@Autowired CacheService cacheService;

	@Override
	public MLResultList<Coins> findAll(int page) {
		try {
			Page<Coins> result = coinsService.findAll(page);
			return new MLResultList<Coins>(result.getResult());
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<Coins>(e);
		}
	}

	@Override
	public MLResult saveUpdate(Coins coins) {
		try {
			coinsService.saveUpdate(coins);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(e);
		}
	}

	@Override
	public MLResult delete(Long id) {
		try {
			coinsService.delete(id);
			return new MLResult(true);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResult(e);
		}
	}
	
	@Override
	public MLResultList<JSONObject> findAllByPage(int page, String code) {
		try {
			Page<Coins> result = coinsService.findAllByPage(page, code);
			
			List<JSONObject> myResult = new ArrayList<JSONObject>();
			List<Coins> tempResult = result.getResult();
			for(Coins thisCoins : tempResult) {
				double peos = 0;
				JSONObject eosTicker = cacheService.get("ticker_" + thisCoins.getCode(), JSONObject.class);
				if(null != eosTicker) {
					peos = eosTicker.getDoubleValue("last_rmb");
				}
				
				JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(thisCoins), JSONObject.class);
				jo.put("value", peos);
				
				myResult.add(jo);
			}
			
			
			
			return new MLResultList<JSONObject>(myResult);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<JSONObject>(e);
		}
	}
	
	@Override
	public MLResultObject<Coins> findByName(String name) {
		try {
			Coins coins = coinsService.findByName(name);
			return new MLResultObject<Coins>(coins);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<Coins>(e);
		}
	}

	@Override
	public MLResultList<Coins> findAllCoins() {
		try {
			List<Coins> result = coinsService.findAllCoins();
			return new MLResultList<Coins>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<Coins>(e);
		}
	}
}
