package it.etoken.component.eosblock.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.component.eosblock.service.RamLargeRankService;
import it.etoken.component.eosblock.service.RamPriceService;

@Component
public class RamLargeRankTask {

	@Autowired
	RamLargeRankService ramLargeRankService;
	
	

	//得到内存大户排行并保存
	@Scheduled(cron = "0 0 */1 * * ?")
	public void getLargeRank() {
		try {
			System.out.println("获取内存大户排行榜开始");
			ramLargeRankService.getLargeRank();
			System.out.println("获取内存大户排行榜结束");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}

	}
	
	
	@Scheduled(cron = "0 */1 * * * ?")
	public void getNewLargeRank() {
		try {
			System.out.println("计算半个小时内是否有大单产生开始---------------------");
			ramLargeRankService.getNewLargeRank();
			System.out.println("计算半个小时内是否有大单产生结束---------------------");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
	}
	
	@Scheduled(cron = "0 */1 * * * ?")
	public void updateLargeRank() {
		try {
			System.out.println("修改半个小时内交易过的大户的内存开始---------------------");
			ramLargeRankService.updateLargeRank();
			System.out.println("修改半个小时内交易过的大户的内结束---------------------");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
	}
	
}
