package it.etoken.component.eosblock.facede.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.ActivityInfo;
import it.etoken.base.model.eosblock.entity.ActivityStage;
import it.etoken.base.model.eosblock.entity.ActivityStageUser;
import it.etoken.component.eosblock.service.ActivityService;
import it.etoken.componet.eosblock.facade.ActivityFacadeAPI;

@Service(version = "1.0.0")
public class ActivityFacadeAPIImpl implements ActivityFacadeAPI {
	private final static Logger logger = LoggerFactory.getLogger(ActivityFacadeAPIImpl.class);
	@Autowired
	ActivityService activityService;

	@Override
	public MLResultObject<ActivityInfo> findById(long id) {
		try {
			ActivityInfo result = activityService.findById(id);
			return new MLResultObject<ActivityInfo>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultObject<ActivityInfo>(e);
		}
	}

	@Override
	public MLResultList<ActivityStage> findAllActivityStageByActivityId(long activityId) {
		try {
			List<ActivityStage> result = activityService.findAllActivityStageByActivityId(activityId);
			return new MLResultList<ActivityStage>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<ActivityStage>(e);
		}
	}

	@Override
	public MLResultList<ActivityStageUser> findAllWinUserByActivityIdAndStageId(long activityStageId,
			String accountName) {
		try {
			List<ActivityStageUser> result = activityService.findAllWinUserByActivityIdAndStageId(activityStageId,
					accountName);
			return new MLResultList<ActivityStageUser>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<ActivityStageUser>(e);
		}
	}
	
	@Override
	public MLResultList<ActivityStageUser> findAllUserByActivityIdAndStageId(long activityStageId,
			String accountName) {
		try {
			List<ActivityStageUser> result = activityService.findAllUserByActivityIdAndStageId(activityStageId,
					accountName);
			return new MLResultList<ActivityStageUser>(result);
		} catch (MLException e) {
			logger.error(e.toString());
			return new MLResultList<ActivityStageUser>(e);
		}
	}
}
