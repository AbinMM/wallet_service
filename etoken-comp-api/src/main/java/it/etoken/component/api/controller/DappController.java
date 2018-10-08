package it.etoken.component.api.controller;

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
import it.etoken.base.model.eosblock.entity.DappCategory;
import it.etoken.base.model.eosblock.entity.DappInfo;
import it.etoken.component.api.exception.MLApiException;
import it.etoken.componet.eosblock.facade.DappCategoryFacadeAPI;
import it.etoken.componet.eosblock.facade.DappInfoFacadeAPI;

@Controller
@RequestMapping(value = "/dapp")
public class DappController extends BaseController{
	private final static Logger logger = LoggerFactory.getLogger(DappController.class);
	
	@Reference(version="1.0.0")
	DappInfoFacadeAPI dappInfoFacadeAPI;
	
	@Reference(version="1.0.0")
	DappCategoryFacadeAPI dappCategoryFacadeAPI;
	
	
	@ResponseBody
	@RequestMapping(value = "/findAll")
	public Object findAll(@RequestBody Map<String, String> requestMap,HttpServletRequest request) {
		try {
			int page=1;
			int pageSize=10;
			if(null!=requestMap.get("page")&&!"".equals(requestMap.get("page"))) {
				page =Integer.parseInt( requestMap.get("page"));
			}
			if(null!=requestMap.get("pageSize")&&!"".equals(requestMap.get("pageSize"))) {
				  pageSize =Integer.parseInt( requestMap.get("pageSize"));
			}
			String name="";
			if(null!=requestMap.get("name")&&!"".equals(requestMap.get("name"))) {
				name=requestMap.get("name");
			}
			MLResultList<DappInfo> result=dappInfoFacadeAPI.findAll(page, pageSize,name);
			if(result.isSuccess()) {
				return this.success(result.getList());
			}else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/findAllRecommend")
	public Object findAllRecommend(HttpServletRequest request) {
		try {
			MLResultList<DappInfo> result=dappInfoFacadeAPI.findAllRecommend();
			if(result.isSuccess()) {
				return this.success(result.getList());
			}else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/findByName")
	public Object findByName(@RequestParam Map<String, String> requestMap,HttpServletRequest request) {
		try {
			String name="";
			if(null!=requestMap.get("name")&&!"".equals(requestMap.get("name"))) {
				name=requestMap.get("name");
			}
			MLResultList<DappInfo> result=dappInfoFacadeAPI.findByName(name);
			if(result.isSuccess()) {
				return this.success(result.getList());
			}else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
//	@ResponseBody
//	@RequestMapping(value = "/findAllCategory")
//	public Object findAllCategory(@RequestBody Map<String, String> requestMap,HttpServletRequest request) {
//		try {
//			
//			
//			int page=1;
//			int pageSize=10;
//			if(null!=requestMap.get("page")&&!"".equals(requestMap.get("page"))) {
//				page =Integer.parseInt( requestMap.get("page"));
//			}
//			if(null!=requestMap.get("pageSize")&&!"".equals(requestMap.get("pageSize"))) {
//				  pageSize =Integer.parseInt( requestMap.get("pageSize"));
//			}
//			String name="";
//			if(null!=requestMap.get("name")&&!"".equals(requestMap.get("name"))) {
//				name=requestMap.get("name");
//			}
//			MLResultList<DappCategory> result=dappCategoryFacadeAPI.findAll(page, pageSize,name);
//			if(result.isSuccess()) {
//				return this.success(result.getList());
//			}else {
//				return this.error(MLApiException.SYS_ERROR, null);
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			return this.error(MLApiException.SYS_ERROR, null);
//		}
//	}
	
    @ResponseBody
	@RequestMapping(value = "/findAllCategory")
	public Object findByName(HttpServletRequest request) {
		try {
			
			MLResultList<DappCategory> result=dappCategoryFacadeAPI.findAll(1, 100,"");
			if(result.isSuccess()) {
				return this.success(result.getList());
			}else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
}
