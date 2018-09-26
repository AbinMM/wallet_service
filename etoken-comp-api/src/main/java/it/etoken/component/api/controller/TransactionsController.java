package it.etoken.component.api.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.result.MLResultList;
import it.etoken.component.api.exception.MLApiException;
import it.etoken.componet.eosblock.facade.TransactionsFacadeAPI;

@Controller
@RequestMapping(value = "/transactions")
public class TransactionsController extends BaseController{
	private final static Logger logger = LoggerFactory.getLogger(DappController.class);
	
	@Reference(version="1.0.0")
	TransactionsFacadeAPI transactionsFacadeAPI;
	
	
	@ResponseBody
	@RequestMapping(value = "/findAllTransferInByAccountAndTokenName")
	public Object findAllTransferInByAccountAndTokenName(@RequestParam Map<String, String> requestMap,HttpServletRequest request) {
		try {
			
			if(null==requestMap.get("account") || requestMap.get("account").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			
			if(null==requestMap.get("tokenName") || requestMap.get("tokenName").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			
			if(null==requestMap.get("to") || requestMap.get("to").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			
			int page=1;
			int pageSize=100;
			if(null != requestMap.get("page") && !requestMap.get("page").isEmpty()) {
				page = Integer.parseInt(requestMap.get("page"));
			}
			if(null != requestMap.get("pageSize") && !requestMap.get("pageSize").isEmpty()) {
				  pageSize =Integer.parseInt( requestMap.get("pageSize"));
			}
			
			String account = requestMap.get("account");
			String tokenName = requestMap.get("tokenName");
			String to = requestMap.get("to");
			
			MLResultList<JSONObject> result=transactionsFacadeAPI.findAllTransferInByAccountAndTokenName(account, tokenName, to, page, pageSize);
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
