package it.etoken.component.api.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.result.MLResultList;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.common.utils.EOSUtils;
import it.etoken.base.model.eosblock.entity.ETTradeLog;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.cache.service.CacheService;
import it.etoken.component.api.eosrpc.EosResult;
import it.etoken.component.api.eosrpc.GetEosTableRows;
import it.etoken.component.api.exception.MLApiException;
import it.etoken.component.api.utils.EosNodeUtils;
import it.etoken.componet.coins.facade.CoinsFacadeAPI;
import it.etoken.componet.eosblock.facade.ETExchangePriceFacadeAPI;
import it.etoken.componet.eosblock.facade.ETExchangeTradeUserFacadeAPI;
import it.etoken.componet.user.facade.UserFacadeAPI;

@Controller
@RequestMapping(value = "/etExchangePrice")
public class ETExchangePriceController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(ETExchangePriceController.class);
	@Autowired
	CacheService cacheService;
	
	@Autowired
	EosNodeUtils eosNodeUtils;

	@Reference(version = "1.0.0", timeout = 60000, retries = 3)
	ETExchangePriceFacadeAPI eTExchangePriceFacadeAPI;
	
	@Reference(version = "1.0.0", timeout = 60000, retries = 3)
	ETExchangeTradeUserFacadeAPI eTExchangeTradeUserFacadeAPI;
	
	@Reference(version = "1.0.0", timeout = 60000, retries = 3)
	UserFacadeAPI userFacadeAPI;
	
	@Reference(version="1.0.0", timeout = 10000)
	CoinsFacadeAPI coinsFacadeAPI;
	
	@ResponseBody
	@RequestMapping(value = "/list")
	public Object list(HttpServletRequest request) {
		try {
			List et_price_list = cacheService.get("et_price_list", List.class);
			return this.success(et_price_list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/info/{code}")
	public Object info(@PathVariable String code, HttpServletRequest request) {
		try {
			BasicDBObject et_price_info = cacheService.get("et_price_info_" + code, BasicDBObject.class);
			 return this.success(et_price_info);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/line/{code}/{type}")
	public Object line(@PathVariable String code, @PathVariable Integer type,@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/line/"+type+" request map : " + requestMap);
		try {
				if(type==null) {
					type=2;
				}
				Map<String,List> data = new HashMap<>();
				List<Double> txs = new ArrayList<>();
				List<Double> ps = new ArrayList<>();
				List<String> x = new ArrayList<>();
				data.put("txs", txs);//交易量
				data.put("ps", ps);//价格
				data.put("x", x);//时间
				
				List list = cacheService.get("et_price_hours_" + code + "_" + type, List.class);
				Collections.sort(list, new Comparator<Object>(){
		            /*
		             * 返回负数表示：p1 小于p2，
		             * 返回0 表示：p1和p2相等，
		             * 返回正数表示：p1大于p2
		             */
		            public int compare(Object p1, Object p2) {
		            	JSONObject jo1 = JSONObject.parseObject(p1.toString());
		            	JSONObject jo2 = JSONObject.parseObject(p2.toString());
		            	Long xx1 = jo1.getLong("record_date");
		            	Long xx2 = jo2.getLong("record_date");
		                //按照时间戳进行升序排列
		                if(xx1 > xx2){
		                    return 1;
		                }
		                if(xx1 == xx2){
		                    return 0;
		                }
		                return -1;
		            }
		        });
				for (Object object : list) {
					JSONObject jo = JSONObject.parseObject(object.toString());
					ps.add(jo.getDouble("price"));
					txs.add(0.0);
					Long xx = jo.getLong("record_date")+8*60*60*1000;
					if(type==2 || type==6|| type==24){
						SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
						x.add(sf.format(xx));
					}else{
						SimpleDateFormat df = new SimpleDateFormat("MM-dd");
						x.add(df.format(xx));
					}
				}
				return this.success(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/kline")
	public Object kline(@org.springframework.web.bind.annotation.RequestBody Map<String, String> requestMap,
			HttpServletRequest requestt) {
		logger.info("/kline/ request map : " + requestMap);
		String code = requestMap.get("code");
		String dateType = requestMap.get("dateType");
		String pageSize = requestMap.get("pageSize");
		
		if(code==null || code.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		if(dateType==null || dateType.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		int count = 180;
		if(pageSize!=null && !pageSize.isEmpty()) {
			count = Integer.parseInt(pageSize);
		}
		
		try {
			String cache_key = "ETExchangePriceController_kline_" + code + "_" + dateType + "_" + count;
			@SuppressWarnings("unchecked")
			List<JSONObject> cacheData = cacheService.get(cache_key, List.class);
			if(cacheData != null && !cacheData.isEmpty()) {
				return this.success(cacheData);
			}
			
			MLResultList<JSONObject> result = eTExchangePriceFacadeAPI.getKLines(code, dateType, count);
			if(result.isSuccess()) {
				List<JSONObject> list = result.getList();
				cacheService.set(cache_key, list, 10);
				return this.success(list);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/kline2")
	public Object kline2(@RequestParam Map<String, String> requestMap,
			HttpServletRequest request) {
		logger.info("/kline/ request map : " + requestMap);
		String code = requestMap.get("code");
		String dateType = requestMap.get("dateType");
		String pageSize = requestMap.get("pageSize");
		
		if(code==null || code.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		if(dateType==null || dateType.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		int count = 180;
		if(pageSize!=null && !pageSize.isEmpty()) {
			count = Integer.parseInt(pageSize);
		}
		
		try {
			String cache_key = "ETExchangePriceController_kline2_" + code + "_" + dateType + "_" + count;
			@SuppressWarnings("unchecked")
			List<JSONObject> cacheData = cacheService.get(cache_key, List.class);
			if(cacheData != null && !cacheData.isEmpty()) {
				return this.success(cacheData);
			}
			MLResultList<JSONObject> result = eTExchangePriceFacadeAPI.getKLines(code, dateType, count);
			if(result.isSuccess()) {
				List<JSONObject> list = result.getList();
				cacheService.set(cache_key, list, 10);
				return this.success(list);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/getNewTradeOrders/{code}")
	public Object getNewTradeOrders(@PathVariable String code, HttpServletRequest request) {
		logger.info("getNewTradeOrders");
		try {
			@SuppressWarnings("unchecked")
			List<ETTradeLog> list = cacheService.get("et_new_trade_orders_"+code, List.class);
			if(null == list || list.isEmpty()) {
				MLResultList<ETTradeLog> result = eTExchangePriceFacadeAPI.getNewTradeOrdersByCode(code);
				if(result.isSuccess()) {
					list = result.getList();
					return this.success(list);
				}else {
					return this.error(result.getErrorCode(),result.getErrorHint(), null);
				}
			}
			if(null != list && !list.isEmpty()) {
				return this.success(list);
			}else {
				return this.error(MLApiException.SYS_ERROR, null);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getBigTradeOrders/{code}")
	public Object getBigTradeOrders(@PathVariable String code, HttpServletRequest request) {
		logger.info("getBigTradeOrders");
		try {
			@SuppressWarnings("unchecked")
			List<ETTradeLog> list = cacheService.get("et_big_trade_orders_"+code, List.class);
//			if(null == list || list.isEmpty()) {
//				//MLResultList<ETTradeLog> result = eTExchangePriceFacadeAPI.getBigTradeOrdersByCode(code);
//				if(result.isSuccess()) {
//					list = result.getList();
//					return this.success(list);
//				}else {
//					return this.error(result.getErrorCode(),result.getErrorHint(), null);
//				}
//			}
//			if(null != list && !list.isEmpty()) {
//				return this.success(list);
//			}else {
//				return this.error(MLApiException.SYS_ERROR, null);
//			}
			return this.success(list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getNewTradeOrdersByAccountName")
	public Object getNewTradeOrdersByAccountName(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("getNewTradeOrdersByAccountName");
		String code = requestMap.get("code");
		String account_name = requestMap.get("account_name");
		if(null == account_name || account_name.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		int pageCount = 100;
		if(null != requestMap.get("pageCount") && !requestMap.get("pageCount").isEmpty()) {
			pageCount = Integer.parseInt(requestMap.get("pageCount"));
		}
		
		String last_id = "";
		if(null != requestMap.get("last_id") && !requestMap.get("last_id").isEmpty() && !requestMap.get("last_id").equalsIgnoreCase("-1")) {
			last_id = requestMap.get("last_id");
		}
		
		try {
			MLResultList<ETTradeLog> result = eTExchangePriceFacadeAPI.getNewTradeOrdersByCodeAndAccountName(code, account_name, pageCount, last_id);
			if(result.isSuccess()) {
				List<ETTradeLog> list = result.getList();
				return this.success(list);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getNewTradeOrdersByAccountName2")
	public Object getNewTradeOrdersByAccountName2(@RequestParam Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("getNewTradeOrdersByAccountName");
		String code = requestMap.get("code");
		String account_name = requestMap.get("account_name");
		if(null == account_name || account_name.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		int pageCount = 100;
		if(null != requestMap.get("pageCount") && !requestMap.get("pageCount").isEmpty()) {
			pageCount = Integer.parseInt(requestMap.get("pageCount"));
		}
		
		String last_id = "";
		if(null != requestMap.get("last_id") && !requestMap.get("last_id").isEmpty() && !requestMap.get("last_id").equalsIgnoreCase("-1")) {
			last_id = requestMap.get("last_id");
		}
		
		try {
			MLResultList<ETTradeLog> result = eTExchangePriceFacadeAPI.getNewTradeOrdersByCodeAndAccountName(code, account_name, pageCount, last_id);
			if(result.isSuccess()) {
				List<ETTradeLog> list = result.getList();
				return this.success(list);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getLargeRankByCode/{code}")
	public Object getLargeRankByCode(@PathVariable String code, HttpServletRequest request) {
		logger.info("getNewTradeOrdersByAccountName");
		if(null == code || code.isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		
		try {
			MLResultList<JSONObject> result = eTExchangeTradeUserFacadeAPI.getNewRankByCode(code);
			if(result.isSuccess()) {
				List<JSONObject> list = result.getList();
				return this.success(list);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/isOpenET")
	public Object isOpenET(HttpServletRequest request) {
		logger.info("isOpenET: ");
		try {
			MLResultObject<Boolean> result = userFacadeAPI.isOpenET();
			if(result.isSuccess()) {
				Boolean isOpenET = result.getResult();
				JSONObject jo = new JSONObject();
				jo.put("open", isOpenET);
				return this.success(jo);
			}else {
				return this.error(result.getErrorCode(),result.getErrorHint(), null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return this.error(MLApiException.SYS_ERROR, null);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getEosShareholdersInfo")
	public Object getEosShareholdersInfo(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/getEosShareholdersInfo request map : " + requestMap);
		
		if(requestMap.get("code")==null || requestMap.get("code").isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		if(requestMap.get("base_contract")==null || requestMap.get("base_contract").isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		String code = requestMap.get("code");
		String[] codes = code.split("_");
		String base_contract = requestMap.get("base_contract");

		MLResultObject<Coins> result = coinsFacadeAPI.findByName(codes[0],base_contract);
		if(!result.isSuccess()) {
			return this.error(result.getErrorCode(), result.getErrorHint(), result.getResult());
		}
		Coins info = result.getResult();
		int precision = info.getPrecisionNumber();
		EOSUtils eOSUtils = new EOSUtils();
		String boundKey = eOSUtils.getBoundKey(base_contract, precision, codes[0]);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("json", true);
		jsonObject.put("code", "etbexchanger");
		jsonObject.put("scope", "etbexchanger");
		jsonObject.put("table", "shareholders"); //markets
		jsonObject.put("key-type", "i128");
		jsonObject.put("limit", "1");
		jsonObject.put("index", "2");
		jsonObject.put("lower", boundKey);

		EosResult resp = null;
		try {
			resp = new GetEosTableRows().run(eosNodeUtils.getNodeUrls().get("url_chain"),
					eosNodeUtils.getNodeUrls().get("url_chain_backup"), jsonObject.toJSONString());

			if (resp.isSuccess()) {
				JSONObject data = JSONObject.parseObject(resp.getData());
				String idxkey = data.getJSONArray("rows").getJSONObject(0).getString("idxkey");
				if(!boundKey.equalsIgnoreCase(idxkey)) {
					return this.error("1004","资金池不存在此交易对", null);
				}
				
				JSONObject rows0 = data.getJSONArray("rows").getJSONObject(0);
				JSONArray map_acc_info = rows0.getJSONArray("map_acc_info");
				map_acc_info = this.mapAccInfoSort(map_acc_info);
				rows0.put("map_acc_info", map_acc_info);
				JSONArray rows = new JSONArray();
				rows.add(rows0);
				data.put("rows", rows);
				return this.success(data);
			} else {
				return this.error(resp.getStatus(), resp.getData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.error(MLApiException.EOSRPC_FAIL, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/getEosMarkets")
	public Object getEosMarkets(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
		logger.info("/getEosShareholdersInfo request map : " + requestMap);
		
		if(requestMap.get("code")==null || requestMap.get("code").isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		if(requestMap.get("base_contract")==null || requestMap.get("base_contract").isEmpty()) {
			return this.error(MLApiException.PARAM_ERROR, null);
		}
		String code = requestMap.get("code");
		String[] codes = code.split("_");
		String base_contract = requestMap.get("base_contract");

		MLResultObject<Coins> result = coinsFacadeAPI.findByName(codes[0],base_contract);
		if(!result.isSuccess()) {
			return this.error(result.getErrorCode(), result.getErrorHint(), result.getResult());
		}
		Coins info = result.getResult();
		int precision = info.getPrecisionNumber();
		EOSUtils eOSUtils = new EOSUtils();
		String boundKey = eOSUtils.getBoundKey(base_contract, precision, codes[0]);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("json", true);
		jsonObject.put("code", "etbexchanger");
		jsonObject.put("scope", "etbexchanger");
		jsonObject.put("table", "markets"); //markets
		jsonObject.put("key_type", "i128");
		jsonObject.put("limit", 1);
		jsonObject.put("index_position", "2");
		jsonObject.put("lower_bound", boundKey);

		EosResult resp = null;
		try {
			resp = new GetEosTableRows().run(eosNodeUtils.getNodeUrls().get("url_chain"),
					eosNodeUtils.getNodeUrls().get("url_chain_backup"), jsonObject.toJSONString());
			
			if (resp.isSuccess()) {
				JSONObject data = JSONObject.parseObject(resp.getData());
				String idxkey = data.getJSONArray("rows").getJSONObject(0).getString("idxkey");
				if(!boundKey.equalsIgnoreCase(idxkey)) {
					return this.error("1004","资金池不存在此交易对", null);
				}
				return this.success(data);
			} else {
				return this.error(resp.getStatus(), resp.getData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.error(MLApiException.EOSRPC_FAIL, null);
	}
	
	private JSONArray mapAccInfoSort(JSONArray map_acc_info) {
		JSONArray sortedJsonArray = new JSONArray();
		List<JSONObject> jsonValues = map_acc_info.toJavaList(JSONObject.class);

		Collections.sort(jsonValues, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject a, JSONObject b) {
				BigDecimal aBD = BigDecimal.ZERO;
				BigDecimal bBD = BigDecimal.ZERO;;
				try {
					String aStr = a.getJSONObject("info").getString("eos_holding");
					String[] aArray = aStr.split(" ");
					aBD = new BigDecimal(aArray[0].trim());
					String bStr = b.getJSONObject("info").getString("eos_holding");
					String[] bArray = bStr.split(" ");
					bBD = new BigDecimal(bArray[0].trim());
				} catch (JSONException e) {
					// do something
				}
				return -aBD.compareTo(bBD);
			}
		});
		sortedJsonArray.addAll(jsonValues);
		return sortedJsonArray;
	}
	
}