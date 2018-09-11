package it.etoken.component.eosblock.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.component.eosblock.service.RamLargeRankSecondaryService;

//@Component
public class RamLargeRankSecondaryTask {

	@Autowired
	RamLargeRankSecondaryService ramLargeRankService;
	
	

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
}
