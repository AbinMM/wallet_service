package it.etoken.component.eosblock.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.DateUtils;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.base.model.eosblock.entity.ETTradeLog;
import it.etoken.cache.service.CacheService;
import it.etoken.component.eosblock.service.ETExchangePriceService;
import it.etoken.component.eosblock.service.TransactionsService;
import it.etoken.component.eosblock.utils.EOSUtils;
import it.etoken.component.eosblock.utils.EosNodeUtils;

@Component
@Transactional
public class ETExchangePriceServiceImpl implements ETExchangePriceService {
	private final int[] lines_hour = new int[] { 1, 2, 6, 24, 48 };
	private final int[] lines_second = new int[] {300,3600,21600,86400};
	private final long BIG_BILLS_AMMOUNT = 2000;
	private final long MID_BILLS_AMMOUNT = 500;

//	@Value("${nodeos.path.chain}")
//	String URL_CHAIN;
//	@Value("${nodeos.path.chain.backup}")
//	String URL_CHAIN_BACKUP;
	
	@Autowired
	EosNodeUtils eosNodeUtils;

	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;

	@Autowired
	CacheService cacheService;
	
	@Autowired
	TransactionsService transactionsService;
	
	@Autowired
	EOSUtils  eOSUtils;

	@Override
	public JSONArray getEtPrices() throws MLException {
		JSONArray etExchangeMarketInfoAndPriceResult = this.getEtExchangeMarketInfoAndPrice();

		Long chineseTimes = DateUtils.getUtcTimes() + 8 * 60 * 60 * 1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date localDate = new Date(chineseTimes);

		long open_record_date = 0;
		try {
			open_record_date = sdf.parse(sdf.format(localDate)).getTime() - 8 * 60 * 60 * 1000;
		} catch (Exception e) {

		}
		Query query = new Query(Criteria.where("record_date").lte(open_record_date));
		query = query.with(new Sort(new Order(Direction.DESC, "record_date")));

		long utcTimes = DateUtils.getUtcTimes();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long times = 0;
		try {
			times = sf.parse(sf.format(new Date(utcTimes))).getTime();
		} catch (Exception e) {

		}

		JSONArray result = new JSONArray();
		for (Object o : etExchangeMarketInfoAndPriceResult) {
			JSONObject thisExchage = (JSONObject) o;

			String code = thisExchage.getString("code");
			BigDecimal price = thisExchage.getBigDecimal("price");
			String trade_uom = thisExchage.getString("trade_uom");
			String base_contract = thisExchage.getString("base_contract");
			String quote_contract = thisExchage.getString("quote_contract");
			BigDecimal base_balance_num = thisExchage.getBigDecimal("base_balance_num");
			String base_balance_uom = thisExchage.getString("base_balance_uom");
			BigDecimal quote_balance_num = thisExchage.getBigDecimal("quote_balance_num");
			String quote_balance_uom = thisExchage.getString("quote_balance_uom");
			String precision_number = thisExchage.getString("precision_number");

			String collection_name = "et_price_" + code;

			BasicDBObject openPriceInfo = mongoTemplate.findOne(query, BasicDBObject.class, collection_name);
			BigDecimal open = BigDecimal.valueOf(0);
			if (openPriceInfo != null) {
				String temPrice = openPriceInfo.getString("price");
				if (null != temPrice && !temPrice.equalsIgnoreCase("0")) {
					Double temPriceDouble = Double.parseDouble(temPrice);
					open = BigDecimal.valueOf(temPriceDouble);
				}
			}

			BigDecimal increase = BigDecimal.ZERO;
			if (open.compareTo(BigDecimal.ZERO) != 0) {
				increase = price.subtract(open).divide(open, 4, BigDecimal.ROUND_HALF_UP);
			}
			
			BigDecimal eosPrice = eOSUtils.getPrice();
			BigDecimal price_rmb = price.multiply(eosPrice);

			BasicDBObject priceInfo = new BasicDBObject();
			priceInfo.put("record_date", times);
			priceInfo.put("code", code);
			priceInfo.put("price", price.doubleValue());
			priceInfo.put("price_rmb", price_rmb.doubleValue());
			priceInfo.put("open", open.doubleValue());
			priceInfo.put("trade_uom", trade_uom);
			priceInfo.put("base_contract", base_contract);
			priceInfo.put("quote_contract", quote_contract);
			priceInfo.put("base_balance_num", base_balance_num.doubleValue());
			priceInfo.put("base_balance_uom", base_balance_uom);
			priceInfo.put("base_balance_uom", base_balance_uom);
			priceInfo.put("quote_balance_num", quote_balance_num.doubleValue());
			priceInfo.put("quote_balance_uom", quote_balance_uom);
			priceInfo.put("increase", increase);
			
			priceInfo.put("precision_number", precision_number);
			

			// 获取交易量
			JSONObject tradingVolumeResult = this.getTradingVolumeByCode(code, price);	
			priceInfo.put("trading_volum", tradingVolumeResult.getDouble("trading_volum"));
			priceInfo.put("buy_volum", tradingVolumeResult.getDouble("buy_volum"));
			priceInfo.put("sell_volum", tradingVolumeResult.getDouble("sell_volum"));
			
			BigDecimal todayVolum = this.getTodayVolum(code);
			priceInfo.put("today_volum", todayVolum.doubleValue());
			
			if(!mongoTemplate.collectionExists(collection_name)) {
				//创建索引
				BasicDBObject record_date_keys = new BasicDBObject();
				record_date_keys.put("record_date", -1);
				BasicDBObject record_date_options = new BasicDBObject();
				record_date_options.put("background", true);
				record_date_options.put("unique", true);
				mongoTemplate.getCollection(collection_name).createIndex(record_date_keys, record_date_options);
				
				BasicDBObject code_keys = new BasicDBObject();
				code_keys.put("code", "hashed");
				
				BasicDBObject code_options = new BasicDBObject();
				code_options.put("background", true);
				mongoTemplate.getCollection(collection_name).createIndex(code_keys, code_options);
			}

			Query queryExists = new Query(Criteria.where("record_date").is(times));
			BasicDBObject existsEtPriceInfo = mongoTemplate.findOne(queryExists, BasicDBObject.class, collection_name);
			if (existsEtPriceInfo == null) {
				mongoTemplate.save(priceInfo, collection_name);

			}
			
			//显示处理
			priceInfo.put("price", price.toPlainString());
			priceInfo.put("price_rmb", price_rmb.toPlainString());
			priceInfo.put("open", open.toPlainString());
			priceInfo.put("base_balance_num", base_balance_num.toPlainString());
			priceInfo.put("quote_balance_num", quote_balance_num.toPlainString());
			priceInfo.put("trading_volum", tradingVolumeResult.getBigDecimal("trading_volum").toPlainString());
			priceInfo.put("buy_volum", tradingVolumeResult.getBigDecimal("buy_volum").toPlainString());
			priceInfo.put("sell_volum", tradingVolumeResult.getBigDecimal("sell_volum").toPlainString());
			priceInfo.put("today_volum", todayVolum);
			
			result.add(priceInfo);
			
			cacheService.set("et_price_info_"+code, priceInfo);
		}
		cacheService.set("et_price_list", result);
		return result;
	}

	@Override
	public JSONArray getEtExchangeMarketInfoAndPrice() {
		JSONArray result = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("json", true);
			jsonObject.put("code", "etbexchanger");
			jsonObject.put("scope", "etbexchanger");
			jsonObject.put("table", "markets");
			String resultStr = HttpClientUtils.doPostJson(eosNodeUtils.getNodeUrls().get("url_chain") + "get_table_rows", jsonObject.toString());
			if (null == resultStr || resultStr.isEmpty()) {
				resultStr = HttpClientUtils.doPostJson(eosNodeUtils.getNodeUrls().get("url_chain_backup") + "get_table_rows", jsonObject.toString());
			}

			JSONObject jo = JSONObject.parseObject(resultStr);
			JSONArray ja = jo.getJSONArray("rows");

			for (Object jx : ja) {
				JSONObject newJo = new JSONObject();
				JSONObject row = (JSONObject) jx;
				String baseContractStr = row.getJSONObject("base").getString("contract");
				String baseBalanceStr = row.getJSONObject("base").getString("balance");

				String quoteContractStr = row.getJSONObject("quote").getString("contract");
				String quoteBalanceStr = row.getJSONObject("quote").getString("balance");

				String[] baseBalanceArray = baseBalanceStr.split(" ");
				BigDecimal baseBalanceNum = new BigDecimal(baseBalanceArray[0].trim());
				String baseBalanceUom = baseBalanceArray[1].trim();
				String[] quoteBalanceArray = quoteBalanceStr.split(" ");
				BigDecimal quoteBalanceNum = new BigDecimal(quoteBalanceArray[0].trim());
				String quoteBalanceUom = quoteBalanceArray[1].trim();
				
				int precision_number = 0;
				if(baseBalanceArray[0].trim().indexOf(".") > 0) {
					precision_number = baseBalanceArray[0].trim().length() - baseBalanceArray[0].trim().indexOf(".") - 1;
				}
				

				BigDecimal price = quoteBalanceNum.divide(baseBalanceNum, 10, BigDecimal.ROUND_HALF_UP);
				String tradeUom = baseBalanceUom + "/" + quoteBalanceUom;
				String code = baseBalanceUom + "_" + quoteBalanceUom + "_" + baseContractStr;
				newJo.put("code", code);
				newJo.put("trade_uom", tradeUom);
				newJo.put("price", price.toPlainString());
				newJo.put("base_contract", baseContractStr);
				newJo.put("quote_contract", quoteContractStr);
				newJo.put("base_balance_num", baseBalanceNum);
				newJo.put("base_balance_uom", baseBalanceUom);
				newJo.put("quote_balance_num", quoteBalanceNum);
				newJo.put("quote_balance_uom", quoteBalanceUom);
				newJo.put("precision_number", precision_number);

				result.add(newJo);
			}
			return result;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public JSONObject getTradingVolumeByCode(String code, BigDecimal price) {
		BigDecimal buytradingVolum = BigDecimal.ZERO;
		BigDecimal selltradingVolum = BigDecimal.ZERO;
		BigDecimal tradingVolum = BigDecimal.ZERO;

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long timesx = 0;
		try {
			timesx = sf.parse(sf.format(new Date())).getTime();
		} catch (Exception e) {

		}
		long startTimes = timesx - 1 * 60 * 1000;
		Date startDate = new Date(startTimes+30*1000);
		Date endDate = new Date(timesx+30*1000);

		String[] codes = code.split("_");

		Query query = new Query(Criteria.where("actions.data.token_contract").is(codes[2]));
		Criteria createDateCriteria = new Criteria();
		createDateCriteria=createDateCriteria.andOperator(Criteria.where("expiration").exists(true),
				Criteria.where("expiration").gte(DateUtils.formateDate(startDate)), Criteria.where("expiration").lt(DateUtils.formateDate(endDate)));
		Criteria createDateCriteria1 = new Criteria();
		createDateCriteria1=createDateCriteria1.andOperator(Criteria.where("transaction_header.expiration").exists(true),
				Criteria.where("transaction_header.expiration").gte(DateUtils.formateDate(startDate)), Criteria.where("transaction_header.expiration").lt(DateUtils.formateDate(endDate)));

		Criteria DateCriteria = new Criteria();
		DateCriteria.orOperator(createDateCriteria,createDateCriteria1);
		//		query.addCriteria(createDateCriteria);
		
		Criteria buyCriteria = new Criteria();
		Criteria sellCriteria = new Criteria();
		Criteria buysellCriteria = new Criteria();
		Criteria tempCriteria = new Criteria();
		
		buyCriteria = buyCriteria.andOperator(Criteria.where("actions.name").is("buytoken"), Criteria.where("actions.data.token_symbol").regex(".*"+codes[0]));
		sellCriteria = sellCriteria.andOperator(Criteria.where("actions.name").is("selltoken"), Criteria.where("actions.data.quant").regex(".*"+codes[0]));
		buysellCriteria = buysellCriteria.orOperator(buyCriteria, sellCriteria);
		
		tempCriteria.andOperator(DateCriteria, buysellCriteria);
		query.addCriteria(tempCriteria);
		System.out.println("query:"+query.toString());
		List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");

		Map<String, String> existMap = new HashMap<String, String>();
		for (BasicDBObject thisBasicDBObject : transactionsList) {
			BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
			String trx_id = thisBasicDBObject.getString("trx_id");
			if (existMap.containsKey(trx_id)) {
				continue;
			}
			Object[] thisActions = actions.toArray();
			for (Object thisAction : thisActions) {
				BasicDBObject action = (BasicDBObject) thisAction;
				String actionName = action.getString("name");
				if (!actionName.equalsIgnoreCase("buytoken") && !actionName.equalsIgnoreCase("selltoken")) {
					continue;
				}

				BasicDBObject data = (BasicDBObject) action.get("data");
				
				if (actionName.equalsIgnoreCase("buytoken")) {
					String eos_quant = data.getString("eos_quant");
					
					String token_symbol = data.getString("token_symbol");
					String[] token_symbols = token_symbol.split(",");
					if(!token_symbols[1].trim().equalsIgnoreCase(codes[0])) {
						continue;
					}
					
					String[] eos_quants = eos_quant.split(" ");
					
					BigDecimal buyTokenQty = BigDecimal.ZERO;
					if(price.compareTo(BigDecimal.ZERO) !=0) {
						buyTokenQty = (new BigDecimal(eos_quants[0].trim())).divide(price, 5, BigDecimal.ROUND_HALF_UP);
					}

					buytradingVolum = buytradingVolum.add(buyTokenQty);
				} else if (actionName.equalsIgnoreCase("selltoken")) {
					String quant = data.getString("quant");
					String[] quants = quant.split(" ");
					
					if(!quants[1].trim().equalsIgnoreCase(codes[0])) {
						continue;
					}
					
					BigDecimal sellTokenQty = new BigDecimal(quants[0].trim());
					selltradingVolum = selltradingVolum.add(sellTokenQty);
				}
			}
			existMap.put(trx_id, trx_id);
		}

		tradingVolum = buytradingVolum.add(selltradingVolum);

		JSONObject result = new JSONObject();
		result.put("trading_volum", tradingVolum);
		result.put("buy_volum", buytradingVolum);
		result.put("sell_volum", selltradingVolum);

		return result;
	}

	public BigDecimal getPriceByCodeAndTimes(String code, Long times) {
		String collection_name = "et_price_" + code;
		Query query = new Query(Criteria.where("record_date").is(times));
		BasicDBObject result = mongoTemplate.findOne(query, BasicDBObject.class, collection_name);

		BigDecimal price = BigDecimal.ZERO;
		if(result != null) {
			String priceString = result.getString("price");
			price = BigDecimal.valueOf(Double.parseDouble(priceString));
		}
			
		return price;
	}
	
	@Override
	public void buildLineData() throws MLException {
		long utcTimes = DateUtils.getUtcTimes();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long times = 0;
		try {
			times = sdf.parse(sdf.format(new Date(utcTimes))).getTime();
		} catch (Exception e) {

		}

		
		JSONArray etExchangeMarketInfoAndPriceResult = this.getEtExchangeMarketInfoAndPrice();
		for (Object o : etExchangeMarketInfoAndPriceResult) {
			JSONObject thisExchage = (JSONObject) o;
			String code = thisExchage.getString("code");
			String collection_name = "et_price_" + code;
			
			for (int thisLineHour : this.lines_hour) {
				long start_time = 0;
				start_time = times - thisLineHour * 60 * 60 * 1000;

				Query query = new Query(Criteria.where("record_date").gte(start_time));
				query = query.with(new Sort(new Order(Direction.ASC, "record_date")));

				List<JSONObject> etPriceInfoList = mongoTemplate.find(query, JSONObject.class, collection_name);
				List<JSONObject> tempPriceInfoList = new ArrayList<JSONObject>();
				
				for(JSONObject thisEtPriceInfo : etPriceInfoList) {
					thisEtPriceInfo.put("price", thisEtPriceInfo.getBigDecimal("price").toPlainString());
					thisEtPriceInfo.put("price_rmb", thisEtPriceInfo.getBigDecimal("price_rmb").toPlainString());
					thisEtPriceInfo.put("open", thisEtPriceInfo.getBigDecimal("open").toPlainString());
					
					thisEtPriceInfo.put("base_balance_num", thisEtPriceInfo.getBigDecimal("base_balance_num").toPlainString());
					thisEtPriceInfo.put("quote_balance_num", thisEtPriceInfo.getBigDecimal("quote_balance_num").toPlainString());
					thisEtPriceInfo.put("trading_volum", thisEtPriceInfo.getBigDecimal("trading_volum").toPlainString());
					thisEtPriceInfo.put("buy_volum", thisEtPriceInfo.getBigDecimal("buy_volum").toPlainString());
					thisEtPriceInfo.put("sell_volum", thisEtPriceInfo.getBigDecimal("sell_volum").toPlainString());
					thisEtPriceInfo.put("today_volum", thisEtPriceInfo.getBigDecimal("today_volum").toPlainString());
					
					tempPriceInfoList.add(thisEtPriceInfo);
				}
				

				cacheService.set("et_price_hours_" + code + "_" + thisLineHour, tempPriceInfoList);
			}
		}
		
	}
	
	@Override
	public void buildLineDataSecond() throws MLException {
		long utcTimes = DateUtils.getUtcTimes();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long times = 0;
		try {
			times = sdf.parse(sdf.format(new Date(utcTimes))).getTime();
		} catch (Exception e) {

		}

		
		JSONArray etExchangeMarketInfoAndPriceResult = this.getEtExchangeMarketInfoAndPrice();
		for (Object o : etExchangeMarketInfoAndPriceResult) {
			JSONObject thisExchage = (JSONObject) o;
			String code = thisExchage.getString("code");
			String collection_name = "et_price_" + code;
			
			for (int thisLineHour : this.lines_second) {
				long start_time = 0;
				start_time = times - thisLineHour * 1000;

				Query query = new Query(Criteria.where("record_date").gte(start_time));
				query = query.with(new Sort(new Order(Direction.ASC, "record_date")));

				List<JSONObject> etPriceInfoList = mongoTemplate.find(query, JSONObject.class, collection_name);
				List<JSONObject> tempPriceInfoList = new ArrayList<JSONObject>();
				
				for(JSONObject thisEtPriceInfo : etPriceInfoList) {
					thisEtPriceInfo.put("price", thisEtPriceInfo.getBigDecimal("price").toPlainString());
					thisEtPriceInfo.put("price_rmb", thisEtPriceInfo.getBigDecimal("price_rmb").toPlainString());
					thisEtPriceInfo.put("open", thisEtPriceInfo.getBigDecimal("open").toPlainString());
					
					thisEtPriceInfo.put("base_balance_num", thisEtPriceInfo.getBigDecimal("base_balance_num").toPlainString());
					thisEtPriceInfo.put("quote_balance_num", thisEtPriceInfo.getBigDecimal("quote_balance_num").toPlainString());
					thisEtPriceInfo.put("trading_volum", thisEtPriceInfo.getBigDecimal("trading_volum").toPlainString());
					thisEtPriceInfo.put("buy_volum", thisEtPriceInfo.getBigDecimal("buy_volum").toPlainString());
					thisEtPriceInfo.put("sell_volum", thisEtPriceInfo.getBigDecimal("sell_volum").toPlainString());
					thisEtPriceInfo.put("today_volum", thisEtPriceInfo.getBigDecimal("today_volum").toPlainString());
					
					tempPriceInfoList.add(thisEtPriceInfo);
				}
				
				String [] codes=code.split("_");
				System.out.println("code" + codes[0]);
				cacheService.set("code" + codes[0], code);
				cacheService.set("et_price_second_" + codes[0] + "_" + thisLineHour, tempPriceInfoList);
			}
		}
		
	}
	
	
	@SuppressWarnings("unused")
	@Override
	public List<ETTradeLog> getNewTradeOrdersByCode(String code) throws MLException {
		String[] codes = code.split("_");
		Criteria expirationCriteria = new Criteria();
		expirationCriteria.andOperator(Criteria.where("actions.data.token_contract").is(codes[2]),
				Criteria.where("expiration").exists(true));
		Query query = new Query();
		
		Criteria buyCriteria = new Criteria();
		Criteria sellCriteria = new Criteria();
		Criteria buysellCriteria = new Criteria();
		Criteria criteria = new Criteria();
		
		buyCriteria = buyCriteria.andOperator(
				Criteria.where("actions.name").is("buytoken"),
				Criteria.where("actions.data.token_symbol").regex(".*"+codes[0]));
		sellCriteria = sellCriteria.andOperator(
				Criteria.where("actions.name").is("selltoken"), 
				Criteria.where("actions.data.quant").regex(".*"+codes[0]));
		buysellCriteria = buysellCriteria.orOperator(buyCriteria, sellCriteria);
		
		criteria.andOperator(expirationCriteria,buysellCriteria);
		
		query.addCriteria(criteria);
		
		int page = 1;
		int pageSize = 100;
		int count = 20;
		
		query = query.with(new Sort(new Order(Direction.DESC, "expiration")));
		query = query.limit(pageSize);
		query = query.skip((page - 1) * pageSize);
		List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");

		Map<String, String> existMap = new HashMap<String, String>();
		List<ETTradeLog> result = new ArrayList<ETTradeLog>();

		for (BasicDBObject thisBasicDBObject : transactionsList) {
			BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
			Object[] thisActions = actions.toArray();
			String trx_id = thisBasicDBObject.getString("trx_id");
			if (existMap.size() == count) {
				break;
			}
			if (existMap.containsKey(trx_id)) {
				continue;
			}
			String blockNum=thisBasicDBObject.getString("block_num");
			if(null==blockNum || blockNum.isEmpty()) {
				Date  time=new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
				Date newDate=new Date();
				
				if(newDate.getTime()-time.getTime()>10*60*1000) {
					continue;
				}
				Query queryBlockNum = new Query(Criteria.where("trx_id").is(trx_id));
				queryBlockNum = queryBlockNum.addCriteria(Criteria.where("block_id").exists(true));
				queryBlockNum = queryBlockNum.with(new Sort(new Order(Direction.DESC, "updatedAt")));
				queryBlockNum = queryBlockNum.limit(1);
				List<BasicDBObject> existTransactionsList = mongoTemplate.find(queryBlockNum, BasicDBObject.class, "transactions");
				if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
					thisBasicDBObject=existTransactionsList.get(0);
				}
			}
			for (Object thisAction : thisActions) {
				BasicDBObject action = (BasicDBObject) thisAction;
				String actionName = action.getString("name");
				if (!actionName.equalsIgnoreCase("selltoken") && !actionName.equalsIgnoreCase("buytoken")) {
					continue;
				}

				BasicDBObject data = (BasicDBObject) action.get("data");
			
				Date  createdAt = new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
			
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				Long times = 0l;
				BigDecimal price = BigDecimal.ZERO;
				try {
					times = sdf.parse(sdf.format(createdAt)).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				price = this.getPriceByCodeAndTimes(code, times);

				ETTradeLog etTradeLog = new ETTradeLog();
				etTradeLog.set_id(thisBasicDBObject.getString("_id"));
				etTradeLog.setTrx_id(trx_id);
				etTradeLog.setBlock_id(thisBasicDBObject.getString("block_id"));
				etTradeLog.setBlock_num(thisBasicDBObject.getString("block_num"));
				etTradeLog.setRecord_date(sdf2.format(createdAt));
				etTradeLog.setAction_name(actionName);
				etTradeLog.setPrice(price.toPlainString());
				
				BigDecimal eosPrice = eOSUtils.getPrice();
				BigDecimal price_rmb = price.multiply(eosPrice);
				etTradeLog.setPrice_rmb(price_rmb.toPlainString());
				
				if (actionName.equalsIgnoreCase("buytoken")) {
					String eos_quant = data.getString("eos_quant");
					String token_symbol = data.getString("token_symbol");
					String[] eos_quants = eos_quant.split(" ");
					BigDecimal buyQty = new BigDecimal(eos_quants[0].trim());
					
					BigDecimal buyTokenQty = BigDecimal.ZERO;
					String[] token_symbols = token_symbol.split(",");
					if(price.compareTo(BigDecimal.ZERO)!=0) {
						buyTokenQty = buyQty.divide(price, 10, BigDecimal.ROUND_HALF_UP);
					}

					String token_uom = token_symbol.trim();
					if(token_symbols.length==2) {
						token_uom = token_symbols[1].trim();
					}
					
					if(!token_uom.equalsIgnoreCase(codes[0])) {
						continue;
					}
					
					etTradeLog.setAccount(data.getString("payer"));
					etTradeLog.setToken_contract(data.getString("token_contract"));
					etTradeLog.setEos_qty(eos_quant);
					etTradeLog.setToken_qty(buyTokenQty + " " + token_uom);
				} else if (actionName.equalsIgnoreCase("selltoken")) {
					String quant = data.getString("quant");
					String[] quants = quant.split(" ");
					if(!quants[1].trim().equalsIgnoreCase(codes[0])) {
						continue;
					}
					BigDecimal qty = new BigDecimal(quants[0].trim());
					BigDecimal eos_qty = qty.multiply(price);
					eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
					
					etTradeLog.setAccount(data.getString("receiver"));
					etTradeLog.setToken_contract(data.getString("token_contract"));
					etTradeLog.setEos_qty(eos_qty + " EOS");
					etTradeLog.setToken_qty(quant);
				}
				
				existMap.put(trx_id, trx_id);
				result.add(etTradeLog);
			}
		}
		Object[] obj=new Object[existMap.size()];
		int i=0;
		for (Map.Entry<String, String> entry : existMap.entrySet()) { 
			if(entry.getValue().isEmpty()||entry.getValue().length()==0) {
				continue;
			}
			    obj[i]=entry.getValue();
			    i++;
			}
		Map<String, String> priceMap=transactionsService.findETExchangeExactPrice(obj);
		for (ETTradeLog eTTradeLog : result) {
			String price=priceMap.get(eTTradeLog.getTrx_id());
			if(null==price||price.length()==0) {
				Object[] obj1=new Object[1];
				obj1[0]=eTTradeLog.getTrx_id();
				Map<String, String> priceMap1=transactionsService.findETExchangeExactPrice(obj1);
				price=priceMap1.get(eTTradeLog.getTrx_id());
			}
			String quant = eTTradeLog.getToken_qty();
			String[] quants = quant.split(" ");
			if(!quants[1].trim().equalsIgnoreCase(codes[0])&&quants[0].trim().equals("0")) {
				continue;
			}
			BigDecimal price1=new BigDecimal(price);
			BigDecimal qty = new BigDecimal(quants[0].trim());
			BigDecimal eos_qty = qty.multiply(price1);
			eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
			String actionName=eTTradeLog.getAction_name();
			if(eos_qty.compareTo(BigDecimal.ZERO)!=0 && actionName.equals("selltoken")) {
				eTTradeLog.setEos_qty(eos_qty + " EOS");
			}
			eTTradeLog.setPrice(price);
		}
		existMap.clear();
		cacheService.set("et_new_trade_orders_" + code, result);

		return result;
	}
	
	
	
	
	
	@SuppressWarnings("unused")
	@Override
	public List<ETTradeLog> getBigTradeOrdersByCode(String code) throws MLException {
		String[] codes = code.split("_");
		
		BigDecimal big_order_qty = BigDecimal.valueOf(10);
		Query settingsQuery = new Query(Criteria.where("code").is(code));
		BasicDBObject bigOrderSettings = mongoTemplate.findOne(settingsQuery, BasicDBObject.class, "et_price_big_order_settings");
		if(null != bigOrderSettings) {
			big_order_qty = BigDecimal.valueOf(bigOrderSettings.getDouble("value"));
		}
		Date endDate = new Date(new Date().getTime()+30*1000); 
		Date startDate = new Date(endDate.getTime()-24*60*60*1000);
		
		Query query = new Query();
		Criteria expirationCriteria = new Criteria();
		expirationCriteria.andOperator(Criteria.where("actions.data.token_contract").is(codes[2]),
				Criteria.where("expiration").exists(true),Criteria.where("expiration").gte(DateUtils.formateDate(startDate)),
				Criteria.where("expiration").lt(DateUtils.formateDate(endDate)));
		Criteria buyCriteria = new Criteria();
		Criteria sellCriteria = new Criteria();
		Criteria buysellCriteria = new Criteria();
		
		buyCriteria = buyCriteria.andOperator(Criteria.where("actions.name").is("buytoken"), Criteria.where("actions.data.token_symbol").regex(".*"+codes[0]));
		sellCriteria = sellCriteria.andOperator(Criteria.where("actions.name").is("selltoken"), Criteria.where("actions.data.quant").regex(".*"+codes[0]));
		buysellCriteria = buysellCriteria.orOperator(buyCriteria, sellCriteria);
		Criteria criteria = new Criteria();
		criteria.andOperator(expirationCriteria,buysellCriteria);
		
		query.addCriteria(criteria);
		
		
		int page = 1;
		int pageSize = 200;    
		int count = 20;

		Map<String, String> existMap = new HashMap<String, String>();
		List<ETTradeLog> result = new ArrayList<ETTradeLog>();
		do {
		    query = query.with(new Sort(new Order(Direction.DESC, "expiration")));
			query = query.limit(pageSize);
			query = query.skip((page - 1) * pageSize);
			List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");

			for (BasicDBObject thisBasicDBObject : transactionsList) {
				BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
				Object[] thisActions = actions.toArray();
				String trx_id = thisBasicDBObject.getString("trx_id");
				if (existMap.size() == count) {
					break;
				}
				if (existMap.containsKey(trx_id)) {
					continue;
				}
				String blockNum=thisBasicDBObject.getString("block_num");
				if(null==blockNum || blockNum.isEmpty()) {
				    Date time=new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
					Date newDate=new Date();
					if(newDate.getTime()-time.getTime()>10*60*1000) {
						continue;
					}
					Query queryBlockNum = new Query(Criteria.where("trx_id").is(trx_id));
					queryBlockNum = queryBlockNum.addCriteria(Criteria.where("block_id").exists(true));
					queryBlockNum = queryBlockNum.with(new Sort(new Order(Direction.DESC, "updatedAt")));
					queryBlockNum = queryBlockNum.limit(1);
					List<BasicDBObject> existTransactionsList = mongoTemplate.find(queryBlockNum, BasicDBObject.class, "transactions");
					if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
						thisBasicDBObject=existTransactionsList.get(0);
					}
				}
				for (Object thisAction : thisActions) {
					BasicDBObject action = (BasicDBObject) thisAction;
					String actionName = action.getString("name");
					if (!actionName.equalsIgnoreCase("selltoken") && !actionName.equalsIgnoreCase("buytoken")) {
						continue;
					}

					BasicDBObject data = (BasicDBObject) action.get("data");
					Date createdAt=new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					Long times = 0l;
					BigDecimal price = BigDecimal.ZERO;
					try {
						times = sdf.parse(sdf.format(createdAt)).getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					price = this.getPriceByCodeAndTimes(code, times);

					ETTradeLog etTradeLog = new ETTradeLog();
					etTradeLog.set_id(thisBasicDBObject.getString("_id"));
					etTradeLog.setTrx_id(trx_id);
					etTradeLog.setBlock_id(thisBasicDBObject.getString("block_id"));
					etTradeLog.setBlock_num(thisBasicDBObject.getString("block_num"));
					etTradeLog.setRecord_date(sdf2.format(createdAt));
					etTradeLog.setAction_name(actionName);
					etTradeLog.setPrice(price.toPlainString());
					
					BigDecimal eosPrice = eOSUtils.getPrice();
					BigDecimal price_rmb = price.multiply(eosPrice);
					etTradeLog.setPrice_rmb(price_rmb.toPlainString());
					
					if (actionName.equalsIgnoreCase("buytoken")) {
						String eos_quant = data.getString("eos_quant");
						String token_symbol = data.getString("token_symbol");
						String[] eos_quants = eos_quant.split(" ");
						BigDecimal buyQty = new BigDecimal(eos_quants[0].trim());
						
						BigDecimal buyTokenQty = BigDecimal.ZERO;
						String[] token_symbols = token_symbol.split(",");
						if(price.compareTo(BigDecimal.ZERO)!=0) {
							buyTokenQty = buyQty.divide(price, 10, BigDecimal.ROUND_HALF_UP);
						}
						
						String token_uom = token_symbol.trim();
						if(token_symbols.length==2) {
							token_uom = token_symbols[1].trim();
						}
						
						if(!token_uom.equalsIgnoreCase(codes[0])) {
							continue;
						}
						
						BigDecimal eos_qty = BigDecimal.valueOf(Double.valueOf(eos_quants[0].trim()));
						if(eos_qty.compareTo(big_order_qty) < 0) {
							continue;
						}
						
						etTradeLog.setAccount(data.getString("payer"));
						etTradeLog.setToken_contract(data.getString("token_contract"));
						etTradeLog.setEos_qty(eos_quant);
						etTradeLog.setToken_qty(buyTokenQty + " " + token_uom);
						etTradeLog.setEos_qty_nounit(buyQty);
						etTradeLog.setToken_qty_nounit(buyTokenQty);
					} else if (actionName.equalsIgnoreCase("selltoken")) {
						String quant = data.getString("quant");
						String[] quants = quant.split(" ");
						if(!quants[1].trim().equalsIgnoreCase(codes[0])) {
							continue;
						}
						BigDecimal qty = new BigDecimal(quants[0].trim());
						BigDecimal eos_qty = qty.multiply(price);
						eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
						
						if(eos_qty.compareTo(big_order_qty) < 0) {
							continue;
						}
						
						etTradeLog.setAccount(data.getString("receiver"));
						etTradeLog.setToken_contract(data.getString("token_contract"));
						etTradeLog.setEos_qty(eos_qty+" EOS");
						etTradeLog.setToken_qty(quant);
						etTradeLog.setEos_qty_nounit(eos_qty);
						etTradeLog.setToken_qty_nounit(qty);
					}
					
					existMap.put(trx_id, trx_id);
					result.add(etTradeLog);
				}
			}
			page++;
		} while (existMap.size() < count && page < 6);
		Object[] obj=new Object[existMap.size()];
		int i=0;
		for (Map.Entry<String, String> entry : existMap.entrySet()) { 
			if(entry.getValue().isEmpty()||entry.getValue().length()==0) {
				continue;
			}
			    obj[i]=entry.getValue();
			    i++;
			}
		Map<String, String> priceMap=transactionsService.findETExchangeExactPrice(obj);
		for (ETTradeLog eTTradeLog : result) {
			String price=priceMap.get(eTTradeLog.getTrx_id());
			if(null==price||price.isEmpty()) {
				Object[] obj1=new Object[1];
				obj1[0]=eTTradeLog.getTrx_id();
				Map<String, String> priceMap1=transactionsService.findETExchangeExactPrice(obj1);
				price=priceMap1.get(eTTradeLog.getTrx_id());
			}
			String quant = eTTradeLog.getToken_qty();
			String[] quants = quant.split(" ");
			if(!quants[1].trim().equalsIgnoreCase(codes[0])&&quants[0].trim().equals("0")) {
				continue;
			}
			BigDecimal price1=new BigDecimal(price);
			BigDecimal qty = new BigDecimal(quants[0].trim());
			BigDecimal eos_qty = qty.multiply(price1);
			eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
			if(eos_qty.compareTo(BigDecimal.ZERO)!=0) {
				eTTradeLog.setEos_qty(eos_qty + " EOS");
			}
			eTTradeLog.setPrice(price);
		}
		existMap.clear();
		//cacheService.set("et_big_trade_orders_" + code, result);
		String collection_name = "et_big_trade_orders_"+code;
		List<ETTradeLog> lastresult = new ArrayList<ETTradeLog>();
        for (ETTradeLog etTradeLog : result) {
        	Query  queryExist=new Query(Criteria.where("trx_id").is(etTradeLog.getTrx_id()));
    		BasicDBObject exist  = mongoTemplate.findOne(queryExist, BasicDBObject.class,collection_name);
    		if(null == exist) {
    			lastresult.add(etTradeLog);
    		}
		}
		mongoTemplate.insert(lastresult, collection_name);
		findBigOrder(code);
		return result;

	}
	

	//查询数据库中的存储的大单根据时间倒叙查询后放入缓存中
	@Override
	public List<ETTradeLog>  findBigOrder(String code){
		String collection_name = "et_big_trade_orders_"+code;
		Query  query=new Query();
		query = query.with(new Sort(new Order(Direction.DESC, "record_date")));
		query = query.limit(20);
		List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class,collection_name);
		List<ETTradeLog> result = new ArrayList<ETTradeLog>();
		for (BasicDBObject basicDBObject : transactionsList) {
			ETTradeLog etTradeLog = new ETTradeLog();
			etTradeLog.set_id(basicDBObject.getString("_id"));
			etTradeLog.setTrx_id(basicDBObject.getString("trx_id"));
			etTradeLog.setBlock_id(basicDBObject.getString("block_id"));
			etTradeLog.setBlock_num(basicDBObject.getString("block_num"));
			etTradeLog.setRecord_date(basicDBObject.getString("record_date"));
			etTradeLog.setAction_name(basicDBObject.getString("action_name"));
			etTradeLog.setPrice(basicDBObject.getString("price"));
			etTradeLog.setAccount(basicDBObject.getString("account"));
			etTradeLog.setToken_contract(basicDBObject.getString("token_contract"));
			etTradeLog.setEos_qty(basicDBObject.getString("eos_qty"));
			etTradeLog.setToken_qty(basicDBObject.getString("token_qty"));
			result.add(etTradeLog);	
		}
		cacheService.set("et_big_trade_orders_" + code, result);
		return result;
		
	}
	
	
	@SuppressWarnings("unused")
	public List<ETTradeLog> getNewTradeOrdersByCodeAndAccountName(String code, String accountName, int pageSize, String last_id)
			throws MLException {
		String[] codes = code.split("_");
		Date startDate = null;
		if (null != last_id && !last_id.isEmpty()) {
			Query query = new Query(Criteria.where("_id").is(new ObjectId(last_id)));
			List<BasicDBObject> existTransactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
			if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
				if(null!=existTransactionsList.get(0).getString("expiration")) {
					startDate =new Date(DateUtils.formateDate(existTransactionsList.get(0).getString("expiration")).getTime()-30*1000);
				}else {
					JSONObject obj=JSONObject.parseObject(existTransactionsList.get(0).get("transaction_header").toString());
					startDate=new Date(DateUtils.formateDate(obj.getString("expiration")).getTime()-30*1000);
				}
			}
		}
		Criteria actorCriteria = Criteria.where("actions.authorization.actor").is(accountName);


		List<ETTradeLog> result = new ArrayList<ETTradeLog>();
		boolean haveList = true;
		Map<String, String> existMap = new HashMap<String, String>();
		Object[] obj=new Object[pageSize];
		int countN = 0;
		do {
			Query query = new Query(actorCriteria);

			query = query.addCriteria(Criteria.where("actions.data.token_contract").is(codes[2]));
			
			Criteria buyCriteria = new Criteria();
			Criteria sellCriteria = new Criteria();
			Criteria buysellCriteria = new Criteria();
			
			buyCriteria = buyCriteria.andOperator(Criteria.where("actions.name").is("buytoken"), Criteria.where("actions.data.token_symbol").regex(".*"+codes[0]));
			sellCriteria = sellCriteria.andOperator(Criteria.where("actions.name").is("selltoken"), Criteria.where("actions.data.quant").regex(".*"+codes[0]));
			buysellCriteria = buysellCriteria.orOperator(buyCriteria, sellCriteria);
			
			//query.addCriteria(buysellCriteria);

			query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
			query = query.limit(pageSize);
			if (null != startDate) {
				Criteria expirationCriteria = new Criteria();
				expirationCriteria.orOperator(Criteria.where("expiration").lt(DateUtils.formateDate(startDate)),
						Criteria.where("transaction_header.expiration").lt(DateUtils.formateDate(startDate)));
				Criteria criteria = new Criteria();
				criteria.andOperator(buysellCriteria,expirationCriteria);
				query = query.addCriteria(criteria);
				System.out.println("getNewTradeOrdersByCodeAndAccountNamequerystartDate:"+query);
			} else {
				Criteria expirationCriteria = new Criteria();
				expirationCriteria.orOperator(Criteria.where("expiration").exists(true),
						Criteria.where("transaction_header.expiration").exists(true));
				Criteria criteria = new Criteria();
				criteria.andOperator(buysellCriteria,expirationCriteria);
				query = query.addCriteria(criteria);
				System.out.println("getNewTradeOrdersByCodeAndAccountNamequery:"+query);
			}

			List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
			if (null == transactionsList || transactionsList.isEmpty()) {
				haveList = false;
				break;
			}
			if(null!=transactionsList.get(transactionsList.size() - 1).getString("expiration")) {
				startDate = new Date(DateUtils.formateDate(transactionsList.get(transactionsList.size() - 1).getString("expiration")).getTime()-30*1000);
			}else {
				JSONObject bj=JSONObject.parseObject(transactionsList.get(transactionsList.size() - 1).get("transaction_header").toString());
				startDate=new Date(DateUtils.formateDate(bj.getString("expiration")).getTime()-30*1000);
			}
			for (BasicDBObject thisBasicDBObject : transactionsList) {
				BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
				String trx_id = thisBasicDBObject.getString("trx_id");
				if (existMap.containsKey(trx_id)) {
					continue;
				}
				String blockNum=thisBasicDBObject.getString("block_num");
				if(null==blockNum || blockNum.isEmpty()) {
					Date time=null;
					if(null!=thisBasicDBObject.getString("expiration")) {
						 time=new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
					}else {
						JSONObject bj=JSONObject.parseObject(transactionsList.get(transactionsList.size() - 1).get("transaction_header").toString());
						time=new Date(DateUtils.formateDate(bj.getString("expiration")).getTime()-30*1000);
					}
					Date newDate=new Date();
					if(newDate.getTime()-time.getTime()>10*60*1000) {
						continue;
					}
					Query queryBlockNum = new Query(Criteria.where("trx_id").is(trx_id));
					queryBlockNum = queryBlockNum.addCriteria(Criteria.where("block_id").exists(true));
					queryBlockNum = queryBlockNum.with(new Sort(new Order(Direction.DESC, "updatedAt")));
					queryBlockNum = queryBlockNum.limit(1);
					List<BasicDBObject> existTransactionsList = mongoTemplate.find(queryBlockNum, BasicDBObject.class, "transactions");
					if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
						thisBasicDBObject=existTransactionsList.get(0);
					}
				}
				Object[] thisActions = actions.toArray();
				for (Object thisAction : thisActions) {
					BasicDBObject action = (BasicDBObject) thisAction;
					String actionName = action.getString("name");
					if (!actionName.equalsIgnoreCase("selltoken") && !actionName.equalsIgnoreCase("buytoken")) {
						continue;
					}

					BasicDBObject data = (BasicDBObject) action.get("data");
					Date createdAt=null;
					if(null!=thisBasicDBObject.getString("expiration")) {
						createdAt = new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
					}else {
						JSONObject bj=JSONObject.parseObject(transactionsList.get(transactionsList.size() - 1).get("transaction_header").toString());
						createdAt=new Date(DateUtils.formateDate(bj.getString("expiration")).getTime()-30*1000);
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					Long times = 0l;
					BigDecimal price = BigDecimal.ZERO;
					try {
						times = sdf.parse(sdf.format(createdAt)).getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					price = this.getPriceByCodeAndTimes(code, times);

					ETTradeLog etTradeLog = new ETTradeLog();
					etTradeLog.set_id(thisBasicDBObject.getString("_id"));
					etTradeLog.setTrx_id(trx_id);
					etTradeLog.setBlock_id(thisBasicDBObject.getString("block_id"));
					etTradeLog.setBlock_num(thisBasicDBObject.getString("block_num"));
					etTradeLog.setRecord_date(sdf2.format(createdAt));
					etTradeLog.setAction_name(actionName);
					etTradeLog.setPrice(price.toPlainString());
					
					BigDecimal eosPrice = eOSUtils.getPrice();
					BigDecimal price_rmb = price.multiply(eosPrice);
					etTradeLog.setPrice_rmb(price_rmb.toPlainString());
					
					if (actionName.equalsIgnoreCase("buytoken")) {
						String eos_quant = data.getString("eos_quant");
						String token_symbol = data.getString("token_symbol");
						String[] eos_quants = eos_quant.split(" ");
						
						BigDecimal buyQty = new BigDecimal(eos_quants[0].trim());
						
						BigDecimal buyTokenQty = BigDecimal.ZERO;
						String[] token_symbols = token_symbol.split(",");
						if(price.compareTo(BigDecimal.ZERO)!=0) {
							buyTokenQty = buyQty.divide(price, 10, BigDecimal.ROUND_HALF_UP);
						}
						
						String token_uom = token_symbol.trim();
						if(token_symbols.length==2) {
							token_uom = token_symbols[1].trim();
						}
						
						if(!token_uom.equalsIgnoreCase(codes[0])) {
							continue;
						}

						etTradeLog.setAccount(data.getString("payer"));
						etTradeLog.setToken_contract(data.getString("token_contract"));
						etTradeLog.setEos_qty(eos_quant);
						etTradeLog.setToken_qty(buyTokenQty + " " + token_uom);
					} else if (actionName.equalsIgnoreCase("selltoken")) {
						String quant = data.getString("quant");
						String[] quants = quant.split(" ");
						if(!quants[1].trim().equalsIgnoreCase(codes[0])) {
							continue;
						}
						BigDecimal qty = new BigDecimal(quants[0].trim());
						BigDecimal eos_qty = qty.multiply(price);
						eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
						
						etTradeLog.setAccount(data.getString("receiver"));
						etTradeLog.setToken_contract(data.getString("token_contract"));
						etTradeLog.setEos_qty(eos_qty + " EOS");
						etTradeLog.setToken_qty(quant);
					}
					result.add(etTradeLog);
					countN++;
					existMap.put(trx_id, trx_id);
					if (countN == pageSize) {
						int i=0;
						for (Map.Entry<String, String> entry : existMap.entrySet()) { 
								if(entry.getValue().isEmpty()||entry.getValue().length()==0) {
									continue;
								}
							    obj[i]=entry.getValue();
							    i++;
							}
						Map<String, String> priceMap=transactionsService.findETExchangeExactPrice(obj);
						for (ETTradeLog eTTradeLog : result) {
							String price1=priceMap.get(eTTradeLog.getTrx_id());
							if(null==price1||price1.length()==0) {
								Object[] obj1=new Object[1];
								obj1[0]=eTTradeLog.getTrx_id();
								Map<String, String> priceMap1=transactionsService.findETExchangeExactPrice(obj1);
							    price1=priceMap1.get(eTTradeLog.getTrx_id());
							}
							String quant = eTTradeLog.getToken_qty();
							String[] quants = quant.split(" ");
							if(!quants[1].trim().equalsIgnoreCase(codes[0])&&quants[0].trim().equals("0")) {
								continue;
							}
							BigDecimal price2=new BigDecimal(price1);
							BigDecimal qty = new BigDecimal(quants[0].trim());
							BigDecimal eos_qty = qty.multiply(price2);
							eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
							String Action_name=etTradeLog.getAction_name();
							if(eos_qty.compareTo(BigDecimal.ZERO)!=0 && Action_name.equals("selltoken")) {
								eTTradeLog.setEos_qty(eos_qty + " EOS");
							}
							eTTradeLog.setPrice(price1);
						}
						existMap.clear();
						return result;
					}
				}
				//existMap.put(trx_id, trx_id);
			}
		} while (haveList);
		int i=0;
		for (Map.Entry<String, String> entry : existMap.entrySet()) {
			if(entry.getValue().isEmpty()||entry.getValue().length()==0) {
				continue;
			}
			    obj[i]=entry.getValue();
			    i++;
			}
		Map<String, String> priceMap=transactionsService.findETExchangeExactPrice(obj);
		for (ETTradeLog eTTradeLog : result) {
			String price=priceMap.get(eTTradeLog.getTrx_id());
			if(null==price||price.isEmpty()) {
				Object[] obj1=new Object[1];
				obj1[0]=eTTradeLog.getTrx_id();
				Map<String, String> priceMap1=transactionsService.findETExchangeExactPrice(obj1);
				price=priceMap1.get(eTTradeLog.getTrx_id());
			}
			String quant = eTTradeLog.getToken_qty();
			String[] quants = quant.split(" ");
			if(!quants[1].trim().equalsIgnoreCase(codes[0])&&quants[0].trim().equals("0")) {
				continue;
			}
			BigDecimal price1=new BigDecimal(price);
			BigDecimal qty = new BigDecimal(quants[0].trim());
			BigDecimal eos_qty = qty.multiply(price1);
			eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
			String actionName=eTTradeLog.getAction_name();
			if(eos_qty.compareTo(BigDecimal.ZERO)!=0 && actionName.equals("selltoken")) {
				eTTradeLog.setEos_qty(eos_qty + " EOS");
			}
			eTTradeLog.setPrice(price);
		}
		existMap.clear();
		return result;
	}

//	public Map<String, Object> calculateAmountStatisticsByCode(String code) {
//
//		Map<String, Object> result = new HashMap<String, Object>();
//
//		int pageSize = 2000;
//
//		BigDecimal buy_big_amount = BigDecimal.ZERO;
//		BigDecimal buy_mid_amount = BigDecimal.ZERO;
//		BigDecimal buy_small_amount = BigDecimal.ZERO;
//		BigDecimal sell_big_amount = BigDecimal.ZERO;
//		BigDecimal sell_mid_amount = BigDecimal.ZERO;
//		BigDecimal sell_small_amount = BigDecimal.ZERO;
//
//		Object[] actionsNames = new Object[] { "buyram", "sellram" };
//
//		Criteria actionsNameCriteria = Criteria.where("actions.name").in(actionsNames).and("block_id").exists(true);
//		long utcTimes = DateUtils.getUtcTimes();
//		long calculateTimes = utcTimes - 24 * 60 * 60 * 1000;
//		Date date = new Date(calculateTimes);
//		Criteria todayCriteria = Criteria.where("createdAt").gte(date);
//
//		Query query = new Query(todayCriteria);
//		query = query.addCriteria(actionsNameCriteria);
//
//		// Criteria myCriteria = todayCriteria;
//		// myCriteria = myCriteria.andOperator(actionsNameCriteria);
//
//		// Aggregation aggregation =
//		// Aggregation.newAggregation(Aggregation.unwind("actions"),
//		// Aggregation.project("eos").andExpression("{ $toDouble: { $substrBytes: [
//		// \"$actions.data.quant\", 0, 4 ] } }").as("eos"),
//		// Aggregation.match(myCriteria),
//		// Aggregation.group("null").sum("eos").as("myEOS"));
//		//// String xyz = aggregation.toString();
//		// AggregationResults<BasicDBObject> outputTypeCount1 =
//		// mongoTemplate.aggregate(aggregation, "pt", BasicDBObject.class);
//
//		long total = mongoTemplate.count(query, Transactions.class);
//		long xcount = 1;
//		BigDecimal totalPage = BigDecimal.valueOf(total).divide(BigDecimal.valueOf(pageSize), 0,
//				BigDecimal.ROUND_CEILING);
//		for (int i = 1; i <= totalPage.intValue(); i++) {
//			query = query.limit(pageSize);
//			query = query.skip((i - 1) * pageSize);
//
//			List<Transactions> transactionsList = mongoTemplate.find(query, Transactions.class);
//			for (BasicDBObject thisBasicDBObject : transactionsList) {
//				BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
//				Object[] thisActions = actions.toArray();
//				for (Object thisAction : thisActions) {
//					BasicDBObject action = (BasicDBObject) thisAction;
//					String actionName = action.getString("name");
//					if (!actionName.equalsIgnoreCase("sellram") && !actionName.equalsIgnoreCase("buyram")) {
//						continue;
//					}
//
//					Date createdAt = thisBasicDBObject.getDate("createdAt");
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//
//					BasicDBObject data = (BasicDBObject) action.get("data");
//					if (actionName.equalsIgnoreCase("buyram")) {
//						Double quant = Double.parseDouble(data.getString("quant").replace("EOS", "").trim());
//						BigDecimal eos_qty = BigDecimal.valueOf(quant);
//						if (eos_qty.compareTo(BigDecimal.valueOf(BIG_BILLS_AMMOUNT)) >= 0) {
//							buy_big_amount = buy_big_amount.add(eos_qty);
//						} else if (eos_qty.compareTo(BigDecimal.valueOf(MID_BILLS_AMMOUNT)) < 0) {
//							buy_small_amount = buy_small_amount.add(eos_qty);
//						} else {
//							buy_mid_amount = buy_mid_amount.add(eos_qty);
//						}
//					} else if (actionName.equalsIgnoreCase("sellram")) {
//						Long times = 0l;
//						BigDecimal price = BigDecimal.ZERO;
//						try {
//							times = sdf.parse(sdf.format(createdAt)).getTime();
//						} catch (ParseException e) {
//							e.printStackTrace();
//						}
//						price = this.getRamPriceByTimes(times);
//
//						Long bytes = data.getLong("bytes");
//						BigDecimal bytesK = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(1024l), 2,
//								BigDecimal.ROUND_HALF_UP);
//						BigDecimal eos_qty = bytesK.multiply(price);
//						eos_qty = eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
//
//						if (eos_qty.compareTo(BigDecimal.valueOf(BIG_BILLS_AMMOUNT)) >= 0) {
//							sell_big_amount = sell_big_amount.add(eos_qty);
//						} else if (eos_qty.compareTo(BigDecimal.valueOf(MID_BILLS_AMMOUNT)) < 0) {
//							sell_small_amount = sell_small_amount.add(eos_qty);
//						} else {
//							sell_mid_amount = sell_mid_amount.add(eos_qty);
//						}
//					}
//					System.out.println(
//							xcount + " 处理中。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。" + xcount);
//					xcount++;
//				}
//			}
//		}
//
//		BigDecimal buy_all_amount = buy_big_amount.add(buy_mid_amount).add(buy_small_amount);
//		BigDecimal sell_all_amount = sell_big_amount.add(sell_mid_amount).add(sell_small_amount);
//		BigDecimal all_amount = buy_all_amount.add(sell_all_amount);
//
//		BigDecimal buy_big_amount_percent = buy_big_amount.divide(all_amount, 2, BigDecimal.ROUND_HALF_UP)
//				.multiply(BigDecimal.valueOf(100));
//		BigDecimal buy_mid_amount_percent = buy_mid_amount.divide(all_amount, 2, BigDecimal.ROUND_HALF_UP)
//				.multiply(BigDecimal.valueOf(100));
//		BigDecimal buy_small_amount_percent = buy_small_amount.divide(all_amount, 2, BigDecimal.ROUND_HALF_UP)
//				.multiply(BigDecimal.valueOf(100));
//
//		BigDecimal sell_big_amount_percent = sell_big_amount.divide(all_amount, 2, BigDecimal.ROUND_HALF_UP)
//				.multiply(BigDecimal.valueOf(100));
//		BigDecimal sell_mid_amount_percent = sell_mid_amount.divide(all_amount, 2, BigDecimal.ROUND_HALF_UP)
//				.multiply(BigDecimal.valueOf(100));
//		BigDecimal sell_small_amount_percent = BigDecimal.valueOf(100).subtract(buy_big_amount_percent)
//				.subtract(buy_mid_amount_percent).subtract(buy_small_amount_percent).subtract(sell_big_amount_percent)
//				.subtract(sell_mid_amount_percent);
//
//		result.put("buy_big_amount",
//				buy_big_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("buy_mid_amount",
//				buy_mid_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("buy_small_amount",
//				buy_small_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("buy_all_amount", buy_all_amount);
//
//		result.put("buy_big_amount_percent", buy_big_amount_percent + "%");
//		result.put("buy_mid_amount_percent", buy_mid_amount_percent + "%");
//		result.put("buy_small_amount_percent", buy_small_amount_percent + "%");
//
//		result.put("sell_big_amount",
//				sell_big_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("sell_mid_amount",
//				sell_mid_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("sell_small_amount",
//				sell_small_amount.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP) + " k");
//		result.put("sell_all_amount", sell_all_amount);
//
//		result.put("sell_big_amount_percent", sell_big_amount_percent + "%");
//		result.put("sell_mid_amount_percent", sell_mid_amount_percent + "%");
//		result.put("sell_small_amount_percent", sell_small_amount_percent + "%");
//
//		result.put("big_amount_desc", "大单  >" + BIG_BILLS_AMMOUNT);
//		result.put("mid_amount_desc", "中单 " + MID_BILLS_AMMOUNT + "-" + BIG_BILLS_AMMOUNT);
//		result.put("small_amount_desc", "小单<" + MID_BILLS_AMMOUNT);
//
//		cacheService.set("calculateAmountStatistics", result);
//
//		return result;
//	}

	@Override
	public BasicDBObject saveKLineData(String code, String dateType) throws MLException {
		String collection_name = this.getKlineCollection(code, dateType); //"et_price_kline_"+ code + "_" + dateType;
		String price_collection_name = "et_price_" + code;
		
		long utcTimes = DateUtils.getUtcTimes();

		long times = 0l;
		long startTimes = 0;
		long endTimes = 0;
		if (dateType.contains("m")) {
			times = Long.parseLong(dateType.replace("m", "").trim()) * 60 * 1000;
			SimpleDateFormat sfm = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			try {
				endTimes = sfm.parse(sfm.format(new Date(utcTimes))).getTime();
			} catch (Exception e) {

			}
			startTimes = endTimes - times;
		} else if (dateType.contains("h")) {
			times = Long.parseLong(dateType.replace("h", "").trim()) * 60 * 60 * 1000;
			SimpleDateFormat sfh = new SimpleDateFormat("yyyy-MM-dd HH");
			try {
				endTimes = sfh.parse(sfh.format(new Date(utcTimes))).getTime();
			} catch (Exception e) {

			}
			startTimes = endTimes - times;
		} else if (dateType.contains("d")) {
			times = Long.parseLong(dateType.replace("d", "").trim()) * 24 * 60 * 60 * 1000;
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
			try {
				endTimes = sfd.parse(sfd.format(new Date(utcTimes))).getTime();
			} catch (Exception e) {

			}
			startTimes = endTimes - times;
		} else if (dateType.contains("w")) {
			times = Long.parseLong(dateType.replace("w", "").trim()) * 7 * 24 * 60 * 60 * 1000;
			SimpleDateFormat sfM = new SimpleDateFormat("yyyy-MM-dd");
			try {
				endTimes = sfM.parse(sfM.format(new Date(utcTimes))).getTime();
			} catch (Exception e) {

			}
			startTimes = endTimes - times;
		} else if (dateType.contains("M")) {
			times = Long.parseLong(dateType.replace("M", "").trim()) * 30 * 24 * 60 * 60 * 1000;
			SimpleDateFormat sfM = new SimpleDateFormat("yyyy-MM");
			try {
				endTimes = sfM.parse(sfM.format(new Date(utcTimes))).getTime();
			} catch (Exception e) {

			}
			startTimes = endTimes - times;
		}

		Criteria recordDateCriteria = new Criteria();
		recordDateCriteria.andOperator(Criteria.where("record_date").gte(startTimes),
				Criteria.where("record_date").lt(endTimes));
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(recordDateCriteria),
				Aggregation.group("null").min("record_date").as("record_date").max("record_date").as("record_date_max")
						.max("price").as("max").min("price").as("min").first("price").as("close").last("price")
						.as("open").sum("trading_volum").as("volum"));
		AggregationResults<BasicDBObject> output = mongoTemplate.aggregate(aggregation, price_collection_name,
				BasicDBObject.class);

		if(output.getMappedResults().size()==0) {
			return null;
		}
		BasicDBObject result = output.getMappedResults().get(0);

		BigDecimal record_date = new BigDecimal(result.getString("record_date"));
		BigDecimal record_date_max = new BigDecimal(result.getString("record_date_max"));

		BigDecimal open = this.getPriceByCodeAndTimes(code, record_date.longValue());
		BigDecimal close = this.getPriceByCodeAndTimes(code, record_date_max.longValue());

		BigDecimal max = new BigDecimal(result.getString("max"));
		BigDecimal min = new BigDecimal(result.getString("min"));
		BigDecimal volum = new BigDecimal(result.getString("volum"));

		BigDecimal increase = close.subtract(open).divide(open, 4, BigDecimal.ROUND_HALF_UP);
		BigDecimal amplitude = max.subtract(min).divide(open, 4, BigDecimal.ROUND_HALF_UP);

		result.put("max", max.doubleValue());
		result.put("min", min.doubleValue());
		result.put("close", close.doubleValue());
		result.put("open", open.doubleValue());
		result.put("volum", volum.doubleValue());

		result.put("increase", increase);
		result.put("amplitude", amplitude);
		
		if(!mongoTemplate.collectionExists(collection_name)) {
			//创建索引
			BasicDBObject record_date_keys = new BasicDBObject();
			record_date_keys.put("record_date", -1);
			BasicDBObject record_date_options = new BasicDBObject();
			record_date_options.put("background", true);
			record_date_options.put("unique", true);
			mongoTemplate.getCollection(collection_name).createIndex(record_date_keys, record_date_options);
		}
		

		Query queryExists = new Query(Criteria.where("record_date").is(startTimes));
		BasicDBObject existsKline = mongoTemplate.findOne(queryExists, BasicDBObject.class,
				collection_name);
		if (existsKline == null) {
			mongoTemplate.save(result, collection_name);
		}

		return result;
	}

	@Override
	public List<JSONObject> getKLines(String code, String dateType, int count) {
//		String collection_name = "et_price_kline_"+ code + "_" + dateType;
//		if(dateType.equals("1d")||dateType.equals("7d")||dateType.equals("1w")) {
//			dateType="1h";
//		}
//		if(dateType.equals("1M")){
//			dateType="2h";
//		}
		String collection_name = this.getKlineCollection(code, dateType);
		Query query = new Query();
		
		query = query.limit(count);
		query = query.with(new Sort(new Order(Direction.DESC, "record_date")));
		List<JSONObject> result = mongoTemplate.find(query, JSONObject.class, collection_name);
		
		Collections.sort(result, new Comparator<Object>(){
            public int compare(Object p1, Object p2) {
            	JSONObject jo1 = (JSONObject)p1;
            	JSONObject jo2 = (JSONObject)p2;
            	Long xx1 = jo1.getLong("record_date");
            	Long xx2 = jo2.getLong("record_date");
                //按照时间戳进行升序排列
                if(xx1 > xx2){
                    return 1;
                }else if(xx1 == xx2){
                    return 0;
                }
                return -1;
            }
        });
		
		List<JSONObject> tempResult = new ArrayList<JSONObject>();
		for(JSONObject thisO : result) {
			thisO.put("max", thisO.getBigDecimal("max"));
			thisO.put("min", thisO.getBigDecimal("min"));
			thisO.put("close", thisO.getBigDecimal("close"));
			thisO.put("open", thisO.getBigDecimal("open"));
			
			tempResult.add(thisO);
		}
		
		//获取不满一个周期的数据
		if(result.size()>0) {
			Long startDateTimes = result.get(result.size()-1).getLong("record_date_max");
			JSONObject latest = this.getLatestInfo(code, startDateTimes);
			if(latest != null) {
				tempResult.add(latest);
			}
		}

		return result;
	}
	
	@Override
	public JSONArray getTimeLines(String code, int count){
		JSONArray result = new JSONArray();
		String collection_name = "et_price_" + code;
		Query query = new Query();
		
		query = query.limit(count);
		query = query.with(new Sort(new Order(Direction.DESC, "record_date")));
		List<BasicDBObject> tempResult = mongoTemplate.find(query, BasicDBObject.class, collection_name);
		for(BasicDBObject bdo : tempResult) {
			result.add(bdo);
		}
		return result;
	}
	
	public BigDecimal getTodayVolum(String code) throws MLException {
		String price_collection_name = "et_price_" + code;
		
		Long chineseTimes = DateUtils.getUtcTimes() + 8 * 60 * 60 * 1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date localDate = new Date(chineseTimes);

		long open_record_date = 0;
		try {
			open_record_date = sdf.parse(sdf.format(localDate)).getTime() - 8 * 60 * 60 * 1000;
		} catch (Exception e) {

		}

		Criteria recordDateCriteria = Criteria.where("record_date").gte(open_record_date);
		
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(recordDateCriteria),
				Aggregation.group("null").sum("trading_volum").as("volum"));
		AggregationResults<BasicDBObject> output = mongoTemplate.aggregate(aggregation, price_collection_name,
				BasicDBObject.class);

		BigDecimal volum = BigDecimal.ZERO;
		if(output.getMappedResults().size()>0) {
			BasicDBObject result = output.getMappedResults().get(0);
			volum = new BigDecimal(result.getString("volum"));
		}

		return volum;
	}
	
	private String getKlineCollection(String code, String dateType) {
		String collection_name = "et_price_"+ code +"_kline_" + dateType;
		return collection_name;
	}
	
	@Override
	public JSONObject getLatestInfo(String code, Long startDateTimes) {
		String price_collection_name = "et_price_" + code;
		Criteria recordDateCriteria = Criteria.where("record_date").gt(startDateTimes);
		
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(recordDateCriteria),
				Aggregation.group("null").sum("trading_volum").as("volum")
				.max("price").as("max")
				.min("price").as("min")
				.min("record_date").as("record_date")
				.max("record_date").as("record_date_max")
				);
		AggregationResults<BasicDBObject> output = mongoTemplate.aggregate(aggregation, price_collection_name,
				BasicDBObject.class);

		BigDecimal volum = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
		BigDecimal min = BigDecimal.ZERO;
		BigDecimal open = BigDecimal.ZERO;
		BigDecimal close = BigDecimal.ZERO;
		BigDecimal increase = BigDecimal.ZERO;
		BigDecimal amplitude = BigDecimal.ZERO;
		
		if(output.getMappedResults().size()==0) {
			return null;
		}
		BasicDBObject resultBasic = output.getMappedResults().get(0);
		
		BigDecimal record_date = new BigDecimal(resultBasic.getString("record_date"));
		BigDecimal record_date_max = new BigDecimal(resultBasic.getString("record_date_max"));

		open =this.getPriceByCodeAndTimes(code, record_date.longValue());
		close = this.getPriceByCodeAndTimes(code, record_date_max.longValue());
		
		max = new BigDecimal(resultBasic.getString("max"));
		min = new BigDecimal(resultBasic.getString("min"));
		volum = new BigDecimal(resultBasic.getString("volum"));
		
		if(open.compareTo(BigDecimal.ZERO)!=0) {
			increase = close.subtract(open).divide(open, 4, BigDecimal.ROUND_HALF_UP);
			amplitude = max.subtract(min).divide(open, 4, BigDecimal.ROUND_HALF_UP);
		}
			
		JSONObject result = new JSONObject();
		result.put("record_date", record_date.longValue());
		result.put("record_date_max", record_date.longValue());
		result.put("volum", volum.doubleValue());
		result.put("max", max);
		result.put("min", min);
		result.put("open", open);
		result.put("close", close);
		result.put("increase", increase);
		result.put("amplitude", amplitude);
		result.put("_class", "com.mongodb.BasicDBObject");
		
		return result;
	}
	
	@Override
	public JSONObject getTodayKInfo(String code) {
		Long chineseTimes = DateUtils.getUtcTimes() + 8 * 60 * 60 * 1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date localDate = new Date(chineseTimes);

		long open_record_date = 0;
		try {
			open_record_date = sdf.parse(sdf.format(localDate)).getTime() - 8 * 60 * 60 * 1000;
		} catch (Exception e) {

		}
		
		JSONObject result = this.getLatestInfo(code, open_record_date);
		
		return result;
	}
}
