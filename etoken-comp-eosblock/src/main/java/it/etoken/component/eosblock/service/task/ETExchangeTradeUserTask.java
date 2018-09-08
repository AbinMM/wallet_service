package it.etoken.component.eosblock.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.component.eosblock.service.ETExchangeTradeUserService;

@Component
public class ETExchangeTradeUserTask {

	@Autowired
	ETExchangeTradeUserService eTExchangeTradeUserService;
	
    @Scheduled(cron = "*/10 * * * * ?")
	public void getEtTradeUserInfo() {
		try {
			eTExchangeTradeUserService.getEtTradeUserInfo(1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}

	}
}
