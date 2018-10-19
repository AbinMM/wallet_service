package it.etoken.component.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.user.entity.Sysconf;
import it.etoken.component.admin.exception.MLAdminException;
import it.etoken.componet.user.facade.SysconfFacadeAPI;

@Controller
@RequestMapping(value = "/admin/sysConf")
public class SysConfController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(BaseController.class);

	@Reference(version = "1.0.0", timeout = 30000)
	SysconfFacadeAPI sysconfFacadeAPI;

	@ResponseBody
	@RequestMapping(value = "/list")
	public Object list(@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/list request map : " + requestMap);
		try {
			List<String> excludeConf = new ArrayList<String>();
			excludeConf.add("share_reward");
			excludeConf.add("up_reward");
			excludeConf.add("down_reward");
			excludeConf.add("acount_send");
			excludeConf.add("signin_reward");
			excludeConf.add("signin_continuity_reward");
			excludeConf.add("signin_continuity_max");
			
			MLResultList<Sysconf> result = sysconfFacadeAPI.findAll();
			if (result.isSuccess()) {
				List<Sysconf> list =  result.getList();
				List<Sysconf> resultList = new ArrayList<Sysconf>();
				for(Sysconf sysconf : list) {
					if(excludeConf.contains(sysconf.getName())) {
						continue;
					}
					resultList.add(sysconf);
				}
				return this.success(resultList);
			} else {
				return this.error(result.getErrorCode(), result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/update")
	public Object update(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/update request map : " + requestMap);
		try {
			String name = requestMap.get("name");
			String value = requestMap.get("value");
			Sysconf sysconf = new Sysconf();
			sysconf.setName(name);
			sysconf.setValue(value);
			MLResultObject<Boolean> result = sysconfFacadeAPI.updateByName(sysconf);
			if (result.isSuccess()) {
				Boolean updateResult = result.getResult();
				return this.success(updateResult);
			} else {
				return this.error(result.getErrorCode(), result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLAdminException.SYS_ERROR, null);
		}
	}
}
