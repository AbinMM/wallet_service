package it.etoken.component.eosblock.service;

import java.util.List;

import it.etoken.base.model.eosblock.entity.ActivityInfo;
import it.etoken.base.model.eosblock.entity.ActivityStage;
import it.etoken.base.model.eosblock.entity.ActivityStageUser;

public interface ActivityService {

	/**
	 * 获取所有活动的购买币的用户并保存数据库
	 */
	public void getAllBuyUsers();
	
	/**
	 * 根据当前时间，修改活动和活动阶段状态
	 */
	public void updateStatus();
	
	/**
	 * 根据id查询活动信息
	 * @param id
	 * @return
	 */
	public ActivityInfo findById(long id);
	
	/**
	 * 获取所有活动所有期数信息
	 * @param activityId
	 * @return
	 */
	public List<ActivityStage> findAllActivityStageByActivityId(long activityId);
	
	/**
	 * 获取所有中奖人员
	 * @param activityStageId
	 * @param accountName
	 * @return
	 */
	public List<ActivityStageUser> findAllWinUserByActivityIdAndStageId(long activityStageId, String accountName);
	
	/**
	 * 获取满足参与活动的人员
	 * @param activityStageId
	 * @param accountName
	 * @return
	 */
	public List<ActivityStageUser> findAllUserByActivityIdAndStageId(long activityStageId, String accountName);
	
	/**
	 * 转币给获奖和幸运者
	 */
	public void transfer2WinAndLuckyUser();
}
