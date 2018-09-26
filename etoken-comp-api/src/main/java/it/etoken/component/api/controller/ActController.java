package it.etoken.component.api.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.ActivityInfo;
import it.etoken.base.model.eosblock.entity.ActivityStage;
import it.etoken.base.model.eosblock.entity.ActivityStageUser;
import it.etoken.component.api.exception.MLApiException;
import it.etoken.componet.eosblock.facade.ActivityFacadeAPI;

@Controller
@RequestMapping(value = "/act")
public class ActController extends BaseController {
	private final static Logger logger = LoggerFactory.getLogger(ActController.class);

	@Reference(version = "1.0.0", timeout = 30000)
	ActivityFacadeAPI activityFacadeAPI;

	@ResponseBody
	@RequestMapping(value = "/getInfo")
	public Object getInfo(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("activityId") || requestMap.get("activityId").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			int activityId = Integer.parseInt(requestMap.get("activityId"));
			MLResultObject<ActivityInfo> result = activityFacadeAPI.findById(activityId);
			if (result.isSuccess()) {
				return this.success(result.getResult());
			} else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getActivityStages")
	public Object getActivityStages(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("activityId") || requestMap.get("activityId").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			int activityId = Integer.parseInt(requestMap.get("activityId"));
			MLResultList<ActivityStage> result = activityFacadeAPI.findAllActivityStageByActivityId(activityId);
			if (result.isSuccess()) {
				return this.success(result.getList());
			} else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getWinActivityStageUsers")
	public Object getWinActivityStageUsers(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("activityStageId") || requestMap.get("activityStageId").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			int activityStageId = Integer.parseInt(requestMap.get("activityStageId"));
			String accountName = "";
			if (null != requestMap.get("accountName") && !requestMap.get("accountName").isEmpty()) {
				accountName = requestMap.get("accountName").trim();
			}

			MLResultList<ActivityStageUser> result = activityFacadeAPI.findAllWinUserByActivityIdAndStageId(activityStageId, accountName);
			if (result.isSuccess()) {
				return this.success(result.getList());
			} else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getActivityStageUsers")
	public Object getActivityStageUsers(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("activityStageId") || requestMap.get("activityStageId").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			int activityStageId = Integer.parseInt(requestMap.get("activityStageId"));
			String accountName = "";
			if (null != requestMap.get("accountName") && !requestMap.get("accountName").isEmpty()) {
				accountName = requestMap.get("accountName").trim();
			}

			MLResultList<ActivityStageUser> result = activityFacadeAPI.findAllUserByActivityIdAndStageId(activityStageId, accountName);
			if (result.isSuccess()) {
				return this.success(result.getList());
			} else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
}
