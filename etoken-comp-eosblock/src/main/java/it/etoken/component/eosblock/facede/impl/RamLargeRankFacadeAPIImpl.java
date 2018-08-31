package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.component.eosblock.service.RamLargeRankService;
import it.etoken.componet.eosblock.facade.RamLargeRankFacadeAPI;
@Service(version = "1.0.0")
public class RamLargeRankFacadeAPIImpl implements RamLargeRankFacadeAPI{
	
	private final static Logger logger = LoggerFactory.getLogger(RamLargeRankFacadeAPIImpl.class);
	
	@Autowired
	RamLargeRankService ramLargeRankService;

	@Override
	public MLResultList<BasicDBObject> getNewestRank() {
		try {
			List<BasicDBObject> result= ramLargeRankService.getNewestRank();
			return new MLResultList<BasicDBObject>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<BasicDBObject>(e);
		}
	}
	
	
}
