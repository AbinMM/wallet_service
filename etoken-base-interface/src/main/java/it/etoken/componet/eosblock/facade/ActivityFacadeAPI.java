package it.etoken.componet.eosblock.facade;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.ActivityInfo;
import it.etoken.base.model.eosblock.entity.ActivityStage;
import it.etoken.base.model.eosblock.entity.ActivityStageUser;

public interface ActivityFacadeAPI {
	public MLResultObject<ActivityInfo> findById(long id);

	public MLResultList<ActivityStage> findAllActivityStageByActivityId(long activityId);

	public MLResultList<ActivityStageUser> findAllWinUserByActivityIdAndStageId(long activityStageId,
			String accountName);
	
	public MLResultList<ActivityStageUser> findAllUserByActivityIdAndStageId(long activityStageId,
			String accountName);

}
