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
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.component.admin.exception.MLAdminException;
import it.etoken.componet.eosblock.facade.DappCategoryFacadeAPI;

@Controller
@RequestMapping(value = "/admin/dappCategory")
public class DappCategoryController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	@Reference(version = "1.0.0", timeout = 10000)
	DappCategoryFacadeAPI dappCategoryFacadeAPI;

	@Reference(version = "1.0.0", timeout = 30000, retries = 0)
	DappCategoryFacadeAPI dappCategoryFacadeAPI2;

	@ResponseBody
	@RequestMapping(value = "/list")
	public Object list(@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/list request map : " + requestMap);
		try {
			String page = requestMap.get("page");
			if (StringUtils.isEmpty(page) || !MathUtil.isInteger(page)) {
				page = "1";
			}
			String name = requestMap.get("name");
			MLResultObject<MLPage<DappCategory>> result = dappCategoryFacadeAPI.findAllByPage(Integer.valueOf(page), 100, name);
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
	@RequestMapping(value = "/listNoPage")
	public Object listNoPage(@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/list request map : " + requestMap);
		try {
			String page = requestMap.get("page");
			if (StringUtils.isEmpty(page) || !MathUtil.isInteger(page)) {
				page = "1";
			}
			String name = requestMap.get("name");
			MLResultList<DappCategory> result = dappCategoryFacadeAPI.findAll(Integer.valueOf(page), 100, name);
			if (result.isSuccess()) {
				return this.success(result.getList());
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
	public Object add(@RequestBody DappCategory dappCategory, HttpServletRequest request) {
		logger.info("/add request map : " + dappCategory);
		try {
			if (StringUtils.isEmpty(dappCategory.getName())) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			
			MLResultObject<DappCategory> r = dappCategoryFacadeAPI2.saveUpdate(dappCategory);
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
	public Object update(@RequestBody DappCategory dappCategory, HttpServletRequest request) {
		logger.info("/update request map : " + dappCategory);
		try {
			if (StringUtils.isEmpty(dappCategory.getName())) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}

			MLResultObject<DappCategory> r = dappCategoryFacadeAPI2.saveUpdate(dappCategory);
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
	public Object delete(@RequestBody DappCategory dappCategory, HttpServletRequest request) {
		logger.info("/update request map : " + dappCategory);
		try {
			if (dappCategory.getId() == null) {
				return this.error(MLAdminException.PARAM_ERROR, null);
			}
			MLResult r = dappCategoryFacadeAPI2.delete(dappCategory.getId());
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
