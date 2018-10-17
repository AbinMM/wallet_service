package it.etoken.component.admin.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import it.etoken.base.common.result.MLPage;
import it.etoken.base.common.result.MLResult;
import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.common.utils.MathUtil;
import it.etoken.base.model.news.entity.SysNotification;
import it.etoken.component.admin.exception.MLAdminException;
import it.etoken.componet.news.facade.SysNotificationFacadeAPI;

@Controller
@RequestMapping(value = "/admin/sysNotification")
public class SysNotificationController extends BaseController {
	
	private final static Logger logger = LoggerFactory.getLogger(SysNotificationController.class);
	
	@Reference(version = "1.0.0", timeout = 10000)
	SysNotificationFacadeAPI sysNotificationFacadeAPI;
	
	@Reference(version = "1.0.0", timeout = 10000)
	SysNotificationFacadeAPI sysNotificationFacadeAPI2;
	

	@ResponseBody
	@RequestMapping(value = "/list")
	public Object list(@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/list request map : " + requestMap);
		try {
			String page = requestMap.get("page");
			if (StringUtils.isEmpty(page) || !MathUtil.isInteger(page)) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			MLResultObject<MLPage<SysNotification>> result = sysNotificationFacadeAPI.findAll(Integer.valueOf(page), 100);
			if (result.isSuccess()) {
				return this.success(result.getResult());
			} else {
				return this.error(result.getErrorCode(), result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/add")
	public Object add(@RequestBody SysNotification sysNotification, HttpServletRequest request) {
		logger.info("/add request map : " + sysNotification);
		try {
			if (StringUtils.isEmpty(sysNotification.getPicurl()) || StringUtils.isEmpty(sysNotification.getUrl())) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			MLResultObject<SysNotification> r = sysNotificationFacadeAPI2.saveUpdate(sysNotification);
			if (r.isSuccess()) {
				return this.success(null);
			} else {
				return this.error(r.getErrorCode(), r.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update")
	public Object update(@RequestBody SysNotification sysNotification, HttpServletRequest request) {
		logger.info("/update request map : " + sysNotification);
		try {
			if (StringUtils.isEmpty(sysNotification.getPicurl()) || StringUtils.isEmpty(sysNotification.getUrl())) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			MLResultObject<SysNotification> r = sysNotificationFacadeAPI2.saveUpdate(sysNotification);
			if (r.isSuccess()) {
				return this.success(null);
			} else {
				return this.error(r.getErrorCode(), r.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/delete")
	public Object delete(@RequestBody  SysNotification sysNotification, HttpServletRequest request) {
		logger.info("/update request map : " + sysNotification);
		try {
			if (sysNotification.getId() == null) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			MLResult r = sysNotificationFacadeAPI2.delete(sysNotification.getId());
			if (r.isSuccess()) {
				return this.success(null);
			} else {
				return this.error(r.getErrorCode(), r.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}
	
}
