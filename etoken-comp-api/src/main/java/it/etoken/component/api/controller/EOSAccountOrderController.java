package it.etoken.component.api.controller;

import java.math.BigDecimal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.eosblock.entity.EosAccountOrder;
import it.etoken.component.api.eosrpc.EosResult;
import it.etoken.component.api.eosrpc.GetAccountInfo;
import it.etoken.component.api.exception.MLApiException;
import it.etoken.component.api.utils.EosNodeUtils;
import it.etoken.componet.eosblock.facade.EOSAccountOrderFacadeAPI;
import it.etoken.componet.user.facade.UserFacadeAPI;

@Controller
@RequestMapping(value = "/eosAccountOrder")
public class EOSAccountOrderController extends BaseController {
	private final static Logger logger = LoggerFactory.getLogger(ActController.class);

	@Reference(version = "1.0.0", timeout = 60000, retries = 3)
	UserFacadeAPI userFacadeAPI;
	
	@Reference(version = "1.0.0", timeout = 30000)
	EOSAccountOrderFacadeAPI eosAccountOrderFacadeAPI;
	
	@Reference(version = "1.0.0", timeout = 60000, retries = -1)
	EOSAccountOrderFacadeAPI eosAccountOrderFacadeAPI2;
	
	@Autowired
	EosNodeUtils eosNodeUtils;

	@ResponseBody
	@RequestMapping(value = "/createWxOrder")
	public Object createWxOrder(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("accountName") || requestMap.get("accountName").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			if (null == requestMap.get("ownerPublicKey") || requestMap.get("ownerPublicKey").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			if (null == requestMap.get("activePublicKey") || requestMap.get("activePublicKey").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			String accountName = requestMap.get("accountName");
			String ownerPublicKey = requestMap.get("ownerPublicKey");
			String activePublicKey = requestMap.get("activePublicKey");
			String ip = getIpAddress(request);
			
			JSONObject getAccountJsonObject = new JSONObject();
			getAccountJsonObject.put("account_name", accountName);
//			EosResult getAccountResp = new GetAccountInfo().run(eosNodeUtils.getNodeUrls().get("url_chain"), eosNodeUtils.getNodeUrls().get("url_chain_backup"),
//					getAccountJsonObject.toString());
			EosResult getAccountResp = new GetAccountInfo().run("http://34.222.33.131:8001/v1/chain/", "http://34.222.33.131:8001/v1/chain/",
					getAccountJsonObject.toString());
			
			
			if (getAccountResp.isSuccess()) {
				return this.error(MLApiException.ACCOUNT_NAME_EXIST, null);
			}
			
			MLResultObject<String> eos_account_price_result = userFacadeAPI.getSysConfigValueByName("eos_account_price");
			if(!eos_account_price_result.isSuccess()) {
				return this.error(MLApiException.SYS_ERROR, null);
			}
			String eos_account_price_str = eos_account_price_result.getResult();
			if(null == eos_account_price_str || eos_account_price_str.isEmpty()) {
				return this.error(MLApiException.SYS_ERROR, null);
			}
			
			BigDecimal eos_account_price = new BigDecimal(eos_account_price_str);
			if(eos_account_price.compareTo(BigDecimal.ZERO) <= 0) {
				return this.error(MLApiException.SYS_ERROR, null);
			}
			
			EosAccountOrder eOSAccountOrder = new EosAccountOrder();
			eOSAccountOrder.setAccountName(accountName);
			eOSAccountOrder.setAmount(eos_account_price);
			eOSAccountOrder.setOwnerPublicKey(ownerPublicKey);
			eOSAccountOrder.setActivePublicKey(activePublicKey);
			eOSAccountOrder.setIp(ip);
			
			MLResultObject<Map<String, String>> result = eosAccountOrderFacadeAPI2.createWxOrder(eOSAccountOrder);
			if (result.isSuccess()) {
				return this.success(result.getResult());
			} else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/queryByOrderNo")
	public Object queryByOrderNo(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("orderNo") || requestMap.get("orderNo").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			String orderNo = requestMap.get("orderNo");
			MLResultObject<EosAccountOrder> result = eosAccountOrderFacadeAPI.queryByOrderNo(orderNo);
			if (result.isSuccess()) {
				return this.success(result.getClass());
			} else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/checkByAccountNameAndOwnerPublicKey")
	public Object checkByAccountNameAndOwnerPublicKey(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		try {
			if (null == requestMap.get("accountName") || requestMap.get("accountName").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			if (null == requestMap.get("ownerPublicKey") || requestMap.get("ownerPublicKey").isEmpty()) {
				return this.error(MLApiException.PARAM_ERROR, null);
			}
			String accountName = requestMap.get("accountName");
			String ownerPublicKey = requestMap.get("ownerPublicKey");
			
			MLResultObject<EosAccountOrder> result = eosAccountOrderFacadeAPI.checkByAccountNameAndOwnerPublicKey(accountName, ownerPublicKey);
			if (result.isSuccess()) {
				return this.success(result.getClass());
			} else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/wxNotify")
	public String getWinActivityStageUsers(@RequestBody String requestBody, HttpServletRequest request) {
			MLResultObject<String> result = eosAccountOrderFacadeAPI.notify(requestBody);
			return result.getResult();
			
	}
}
