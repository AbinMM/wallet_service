package it.etoken.component.eosblock.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.etoken.component.eosblock.service.ActivityService;

@Component
public class ActivityTask {

	@Autowired
	ActivityService activityService;
	
	// 获取购买币的用户
	@Scheduled(cron = "*/10 * * * * ?")
	public void getBuyUser() {
		System.out.println("活动-------------------------------------------------------开始获取交易用户信息...");
		activityService.getAllBuyUsers();
		System.out.println("活动-------------------------------------------------------获取交易用户信息结束.");
	}
	
	@Scheduled(cron = "*/5 * * * * ?")
	public void updateStatus() {
		System.out.println("活动-------------------------------------------------------开始更新活动状态...");
		activityService.updateStatus();
		System.out.println("活动-------------------------------------------------------更新活动状态结束.");
	}
	
	@Scheduled(cron = "0 */15 * * * ?")
	public void transfer2WinAndLuckyUser() {
		System.out.println("活动-------------------------------------------------------开始转账给用户...");
		activityService.transfer2WinAndLuckyUser();
		System.out.println("活动-------------------------------------------------------转账给用户结束");
	}
}
