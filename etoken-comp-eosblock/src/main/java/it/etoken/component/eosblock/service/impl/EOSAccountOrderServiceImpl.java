package it.etoken.component.eosblock.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants.SignType;
import com.github.wxpay.sdk.WXPayUtil;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.MathUtil;
import it.etoken.base.model.eosblock.entity.EosAccountOrder;
import it.etoken.base.model.eosblock.entity.EosAccountOrderExample;
import it.etoken.component.eosblock.dao.mapper.EosAccountOrderMapper;
import it.etoken.component.eosblock.payment.wxpay.WxPayConfig;
import it.etoken.component.eosblock.service.EOSAccountOrderService;
import it.etoken.component.eosblock.utils.EosNodeUtils;

@Component
@Transactional
public class EOSAccountOrderServiceImpl implements EOSAccountOrderService {
	private final static Logger logger = LoggerFactory.getLogger(EOSAccountOrderServiceImpl.class);

	@Autowired
	EosAccountOrderMapper eosAccountOrderMapper;

	@Autowired
	WxPayConfig wxPayConfig;
	
	@Autowired
	EosNodeUtils eosNodeUtils;
	
	@Value("${eos.server.api}")
	String EOS_SERVER_API;

	@Override
	public EosAccountOrder saveUpdate(EosAccountOrder eOSAccountOrder) {
		try {
			if (eOSAccountOrder.getId() == null) {
				eosAccountOrderMapper.insertSelective(eOSAccountOrder);
			} else {
				eosAccountOrderMapper.updateByPrimaryKeySelective(eOSAccountOrder);
			}
			return eOSAccountOrder;
		} catch (MLException ex) {
			logger.error(ex.toString());
			throw ex;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public EosAccountOrder queryByOrderNo(String orderNo) {
		EosAccountOrderExample example = new EosAccountOrderExample();
		EosAccountOrderExample.Criteria criteria = example.createCriteria();
		criteria.andOrderNoEqualTo(orderNo);
		List<EosAccountOrder> list = eosAccountOrderMapper.selectByExample(example);
		if (null == list || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public EosAccountOrder checkByAccountNameAndOwnerPublicKey(String accountName, String ownerPublicKey) {
		EosAccountOrder eosAccountOrder = this.queryByAccountNameAndOwnerPublicKey(accountName, ownerPublicKey);
		if(null == eosAccountOrder) {
			return null;
		}
		if (eosAccountOrder.getStatus().equalsIgnoreCase("paid")
				|| eosAccountOrder.getStatus().equalsIgnoreCase("completed")) {
			return eosAccountOrder;
		}

		Map<String, String> queryFromWxResult = this.queryByOrderFromWx(eosAccountOrder.getOrderNo());

		String notifyContent = "";
		try {
			notifyContent = WXPayUtil.mapToXml(queryFromWxResult);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (queryFromWxResult.get("return_code").equalsIgnoreCase("SUCCESS")
				&& queryFromWxResult.get("result_code").equalsIgnoreCase("SUCCESS")
				&& queryFromWxResult.get("trade_state").equalsIgnoreCase("SUCCESS")) {
			this.notify(notifyContent);
			eosAccountOrder = this.queryByAccountNameAndOwnerPublicKey(accountName, ownerPublicKey);
		}

		return eosAccountOrder;
	}

	private Map<String, String> queryByOrderFromWx(String orderNo) {
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("out_trade_no", orderNo);

		WXPay wxPay = null;
		Map<String, String> result = null;
		try {
			wxPay = new WXPay(wxPayConfig, wxPayConfig.getNotifyUrl());
			result = wxPay.orderQuery(reqData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException("209", e.getMessage());
		}

		if (!result.get("return_code").equalsIgnoreCase("SUCCESS")) {
			throw new MLException("205", "查询不成功");
		}

		boolean isSignatureValid = false;
		try {
			isSignatureValid = WXPayUtil.isSignatureValid(result, wxPayConfig.getKey(), SignType.HMACSHA256);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException("209", e.getMessage());
		}
		if (!isSignatureValid) {
			throw new MLException("107", "验证签名不正确");
		}

		if (!result.get("result_code").equalsIgnoreCase("SUCCESS")) {
			throw new MLException("206", "返回结果成功");
		}
		if (!result.get("trade_state").equalsIgnoreCase("SUCCESS")) {
			throw new MLException("207", "支付不成功");
		}

		return result;
	}

	public EosAccountOrder queryByAccountNameAndOwnerPublicKey(String accountName, String ownerPublicKey) {
		EosAccountOrderExample example = new EosAccountOrderExample();
		EosAccountOrderExample.Criteria criteria = example.createCriteria();
		criteria.andAccountNameEqualTo(accountName);
		criteria.andOwnerPublicKeyEqualTo(ownerPublicKey);
		
		//查找支付成功或者已创建成功的单
		List<String> statusList = new ArrayList<String>();
		statusList.add("paid");
		statusList.add("completed");
		criteria.andStatusIn(statusList);
		List<EosAccountOrder> list = eosAccountOrderMapper.selectByExample(example);
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		}
		
		//没有支付成功的，则返回最新一条记录
		EosAccountOrderExample exampleLatest = new EosAccountOrderExample();
		EosAccountOrderExample.Criteria criteriaLatest = exampleLatest.createCriteria();
		criteriaLatest.andAccountNameEqualTo(accountName);
		criteriaLatest.andOwnerPublicKeyEqualTo(ownerPublicKey);
		exampleLatest.setOrderByClause("id desc");
		
		list = eosAccountOrderMapper.selectByExample(exampleLatest);
		if (null == list || list.isEmpty()) {
			return null;
		}
		EosAccountOrder eosAccountOrder = list.get(0);
		return eosAccountOrder;
	}

	public EosAccountOrder queryByAccountName(String accountName) {
		EosAccountOrderExample example = new EosAccountOrderExample();
		EosAccountOrderExample.Criteria criteria = example.createCriteria();
		criteria.andAccountNameEqualTo(accountName);
		List<EosAccountOrder> list = eosAccountOrderMapper.selectByExample(example);
		if (null == list || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	public EosAccountOrder queryPaidOrCompletedByAccountName(String accountName) {
		EosAccountOrderExample example = new EosAccountOrderExample();
		EosAccountOrderExample.Criteria criteria = example.createCriteria();
		criteria.andAccountNameEqualTo(accountName);
		
		List<String> statusList = new ArrayList<String>();
		statusList.add("paid");
		statusList.add("completed");
		criteria.andStatusIn(statusList);
		
		List<EosAccountOrder> list = eosAccountOrderMapper.selectByExample(example);
		if (null == list || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public Map<String, String> createWxOrder(EosAccountOrder eOSAccountOrder) {
		Date nowDate = new Date();

		EosAccountOrder existSuccessEOSAccountOrder = this.queryPaidOrCompletedByAccountName(eOSAccountOrder.getAccountName());
		if (null != existSuccessEOSAccountOrder) {
			throw new MLException("100", "此账号已付款，请勿再次支付");
		}

		// EosAccountOrder existAccountOrder = this.queryByAccountNameAndOwnerPublicKey(
		// eOSAccountOrder.getAccountName(), eOSAccountOrder.getOwnerPublicKey());
		// if (null == existAccountOrder) {
		eOSAccountOrder.setOrderNo(MathUtil.getUUID());
		eOSAccountOrder.setCreatedate(nowDate);
		eOSAccountOrder.setUpdatedate(nowDate);
		eOSAccountOrder.setStatus("new");

		this.saveUpdate(eOSAccountOrder);
		// } else {
		// // 更新最新价格
		// EosAccountOrder updateEosAccountOrder = new EosAccountOrder();
		// updateEosAccountOrder.setId(existAccountOrder.getId());
		// updateEosAccountOrder.setAmount(eOSAccountOrder.getAmount());
		// updateEosAccountOrder.setIp(eOSAccountOrder.getIp());
		// updateEosAccountOrder.setUpdatedate(nowDate);
		//
		// this.saveUpdate(updateEosAccountOrder);
		// eOSAccountOrder =
		// this.queryByAccountNameAndOwnerPublicKey(eOSAccountOrder.getAccountName(),
		// eOSAccountOrder.getOwnerPublicKey());
		// }

		WXPay wxPay = null;
		try {
			wxPay = new WXPay(wxPayConfig, wxPayConfig.getNotifyUrl());
		} catch (Exception e) {
			throw new MLException("121", e.getMessage());
		}
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("out_trade_no", eOSAccountOrder.getOrderNo());

		BigDecimal amount_fen = eOSAccountOrder.getAmount().multiply(BigDecimal.valueOf(100)).setScale(0);
		reqData.put("total_fee", amount_fen.toPlainString());
		reqData.put("trade_type", "APP");
		reqData.put("spbill_create_ip", eOSAccountOrder.getIp());
		reqData.put("body", "购买EOS账号: " + eOSAccountOrder.getAccountName());

		Map<String, String> reqResult = new HashMap<String, String>();
		try {
			reqResult = wxPay.unifiedOrder(reqData);
		} catch (Exception e) {
			throw new MLException("122", e.getMessage());
		}

		String return_code = reqResult.get("return_code");
		String return_msg = reqResult.get("return_msg");
		if (!return_code.equalsIgnoreCase("SUCCESS")) {
			throw new MLException("101", return_msg);
		}

		String result_code = reqResult.get("result_code");
		if (!result_code.equalsIgnoreCase("SUCCESS")) {
			throw new MLException("102", reqResult.get("err_code_des"));
		}
		String prepay_id = reqResult.get("prepay_id");
		String appid = reqResult.get("appid");
		String mch_id = reqResult.get("mch_id");
		String nonceStr = WXPayUtil.generateNonceStr();
		String timeStamp = String.valueOf(WXPayUtil.getCurrentTimestamp());

		Map<String, String> payData = new HashMap<String, String>();

		payData.put("appid", appid);
		payData.put("partnerid", mch_id);
		payData.put("prepayid", prepay_id);
		payData.put("package", "Sign=WXPay");
		payData.put("noncestr", nonceStr);
		payData.put("timestamp", timeStamp);
		
		String sign = "";
		try {
			sign = WXPayUtil.generateSignature(payData, wxPayConfig.getKey(), SignType.HMACSHA256);
		} catch (Exception e) {
			throw new MLException("123", e.getMessage());
		}
		payData.put("sign", sign);

		return payData;
	}

	@Override
	public String notify(String strXML) {
		if (null == strXML || strXML.isEmpty()) {
			throw new MLException("107", "通知内容为空");
		}

		Map<String, String> notifyData = new HashMap<String, String>();
		notifyData.put("return_code", "SUCCESS");
		notifyData.put("return_msg", "OK");
		String notifyXML = "";
		try {
			notifyXML = WXPayUtil.mapToXml(notifyData);

			Date nowDate = new Date();
			Map<String, String> notifyMap = WXPayUtil.xmlToMap(strXML);
			String return_code = notifyMap.get("return_code");
			if (!return_code.equalsIgnoreCase("SUCCESS")) {
				throw new MLException("104", "通知信息失败");
			}

			boolean isSignatureValid = WXPayUtil.isSignatureValid(notifyMap, wxPayConfig.getKey(), SignType.HMACSHA256);
			if (!isSignatureValid) {
				throw new MLException("107", "验证签名不正确");
			}

			String out_trade_no = notifyMap.get("out_trade_no");
			String transactionId = notifyMap.get("transaction_id");
			EosAccountOrder eosAccountOrder = this.queryByOrderNo(out_trade_no);
			if (null == eosAccountOrder) {
				throw new MLException("105", "订单不存在");
			}

			String result_code = notifyMap.get("result_code");
			if (!eosAccountOrder.getStatus().equalsIgnoreCase("paid")
					&& !eosAccountOrder.getStatus().equalsIgnoreCase("completed")) {
				EosAccountOrder updateEosAccountOrder = new EosAccountOrder();
				updateEosAccountOrder.setId(eosAccountOrder.getId());
				BigDecimal orderAmount = eosAccountOrder.getAmount().multiply(BigDecimal.valueOf(100));
				BigDecimal notifyAmount = new BigDecimal(notifyMap.get("total_fee"));
				String configMchId = wxPayConfig.getMchID();
				String notifyMchId = notifyMap.get("mch_id");
				if (result_code.equalsIgnoreCase("SUCCESS") && orderAmount.compareTo(notifyAmount) == 0
						&& configMchId.equalsIgnoreCase(notifyMchId)) {
					updateEosAccountOrder.setStatus("paid");
				} else {
					updateEosAccountOrder.setStatus("failure");
				}

				updateEosAccountOrder.setNotifyContent(strXML);
				updateEosAccountOrder.setNotifyDate(nowDate);
				updateEosAccountOrder.setTransactionId(transactionId);
				updateEosAccountOrder.setUpdatedate(nowDate);
				this.saveUpdate(updateEosAccountOrder);

				if (result_code.equalsIgnoreCase("SUCCESS") && orderAmount.compareTo(notifyAmount) == 0
						&& configMchId.equalsIgnoreCase(notifyMchId)) {
					this.createEosAccount(eosAccountOrder.getOrderNo());
				}
			}

			return notifyXML;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return notifyXML;
	}

	@Override
	public void createEosAccount(String orderNo) {
		EosAccountOrder eosAccountOrder = this.queryByOrderNo(orderNo);
		if (!eosAccountOrder.getStatus().equalsIgnoreCase("paid")) {
			return;
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", eosAccountOrder.getAccountName());
		jsonObject.put("owner", eosAccountOrder.getOwnerPublicKey());
		jsonObject.put("active", eosAccountOrder.getActivePublicKey());
		
		try {
//			String resultStr = HttpClientUtils.doPostJson(EOS_SERVER_API + "account/create", jsonObject.toString());
			
//			JSONObject jo = JSONObject.parseObject(resultStr);
//			int code = jo.getIntValue("code");
//			String msg = jo.getString("msg");
//			
//			if(code == 0 && msg.equalsIgnoreCase("success")) {
//				 修改订单状态为完成
				Date nowDate = new Date();
				EosAccountOrder updateEosAccountOrder = new EosAccountOrder();
				updateEosAccountOrder.setId(eosAccountOrder.getId());
				updateEosAccountOrder.setStatus("completed");
				updateEosAccountOrder.setUpdatedate(nowDate);
				this.saveUpdate(updateEosAccountOrder);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	public void updatePaidOrderStatus() {
		
	}

}
