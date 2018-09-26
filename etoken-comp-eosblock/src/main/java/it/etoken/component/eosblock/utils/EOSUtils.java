package it.etoken.component.eosblock.utils;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import it.etoken.cache.service.CacheService;

@Component
public class EOSUtils {
	@Autowired
	CacheService cacheService;
	public BigDecimal getPrice() {
		double peos = 0;
		JSONObject eosTicker = cacheService.get("ticker_eos",JSONObject.class);
		if(null != eosTicker) {
			peos = eosTicker.getDoubleValue("last_rmb");
		}
		return BigDecimal.valueOf(peos);
	}
}
