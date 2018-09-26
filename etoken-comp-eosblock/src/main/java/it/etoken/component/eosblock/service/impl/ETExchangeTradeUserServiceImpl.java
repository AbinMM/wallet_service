package it.etoken.component.eosblock.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.cache.service.CacheService;
import it.etoken.component.eosblock.service.ETExchangePriceService;
import it.etoken.component.eosblock.service.ETExchangeTradeUserService;
import it.etoken.component.eosblock.utils.EosNodeUtils;

@Component
@Transactional
public class ETExchangeTradeUserServiceImpl implements ETExchangeTradeUserService {

	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;

	@Autowired
	CacheService cacheService;

	@Autowired
	ETExchangePriceService eTExchangePriceService;

	@Autowired
	EosNodeUtils eosNodeUtils;

	@Override
	public List<JSONObject> getNewRankByCode(String code) {
		Query query = new Query(Criteria.where("code").is(code));
		query = query.with(new Sort(new Order(Direction.DESC, "qty")));
		query = query.limit(20);
		List<JSONObject> ramLargeRankList = mongoTemplate.find(query, JSONObject.class, "et_price_trade_user");
		
		int i = 1;
		List<JSONObject> result = new ArrayList<JSONObject>(); 
		for (JSONObject jo : ramLargeRankList) {
			jo.put("seq", i);
			result.add(jo);
			i++;
		}
		
		return result;
	}

	@Override
	public void getEtTradeUserInfo(int pastHours) {
		try {
			JSONArray eTExchangePrices = eTExchangePriceService.getEtExchangeMarketInfoAndPrice();
			for (Object o : eTExchangePrices) {
				JSONObject jo = (JSONObject) o;
				String code = jo.getString("code");
				String[] codes = code.split("_");

				long startDateTimes = (new Date()).getTime() - pastHours * 60 * 60 * 1000; // 24小时前
				Date startDate = new Date(startDateTimes);
				Date endDate = new Date();
				Set<String> exchangeUsers = this.getExchangeUsersByDate(code, startDate, endDate);
				for (String exchangeUserName : exchangeUsers) {
					JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put("json", true);
						jsonObject.put("code", codes[2]);
						jsonObject.put("account", exchangeUserName);
						jsonObject.put("symbol", codes[0]);
					} catch (JSONException e2) {
						e2.printStackTrace();
					}

					String url = eosNodeUtils.getNodeUrls().get("url_chain") + "get_currency_balance";
					String balanceStrResult = HttpClientUtils.doPostJson(url, jsonObject.toJSONString());
					if (null == balanceStrResult || balanceStrResult.isEmpty()) {
						url = eosNodeUtils.getNodeUrls().get("url_chain_backup") + "get_currency_balance";
						balanceStrResult = HttpClientUtils.doPostJson(url, jsonObject.toJSONString());
					}

					if (null == balanceStrResult || balanceStrResult.isEmpty()) {
						return;
					}
					balanceStrResult = balanceStrResult.replace("[", "").replace("]", "").replace("\"", "")
							.replace(codes[0], "").trim();
					Double balance = 0d;
					if (null != balanceStrResult && !balanceStrResult.isEmpty()) {
						balance = Double.valueOf(balanceStrResult);
					}

					Query query = new Query(Criteria.where("code").is(code).and("account").is(exchangeUserName));

					Update update = Update.update("code", code);
					update.set("account", exchangeUserName);
					update.set("uom", codes[0]);
					update.set("qty", balance);
					update.set("contract_account", codes[2]);
					update.set("updatedAt", new Date());
					
					mongoTemplate.upsert(query, update, "et_price_trade_user");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}

	}

	public Set<String> getExchangeUsersByDate(String code, Date startDate, Date endDate) {
		Set<String> result = new HashSet<String>();
		String[] codes = code.split("_");
		try {
			// buy
			Criteria buyMatchCriteria = new Criteria();
			buyMatchCriteria.andOperator(Criteria.where("actions.account").is("etbexchanger"),
					Criteria.where("actions.name").is("buytoken"),
					Criteria.where("actions.data.token_symbol").regex(".*" + codes[0]),
					Criteria.where("createdAt").gte(startDate),
					Criteria.where("createdAt").lt(endDate)
					);

			System.out.println(buyMatchCriteria.getCriteriaObject());

			Aggregation buyAggregation = Aggregation.newAggregation(Aggregation.match(buyMatchCriteria),
					Aggregation.group("actions.data.payer"));
			AggregationResults<BasicDBObject> buyOutput = mongoTemplate.aggregate(buyAggregation, "transactions",
					BasicDBObject.class);
			List<BasicDBObject> buyResult = buyOutput.getMappedResults();
			for (BasicDBObject bdo : buyResult) {
				result.add(bdo.getString("0"));
			}

			// sell
			Criteria sellMatchCriteria = new Criteria();
			sellMatchCriteria.andOperator(Criteria.where("actions.account").is("etbexchanger"),
					Criteria.where("actions.name").is("selltoken"),
					Criteria.where("actions.data.quant").regex(".*" + codes[0]),
					Criteria.where("createdAt").gte(startDate));

			System.out.println(buyMatchCriteria.getCriteriaObject());

			Aggregation sellAggregation = Aggregation.newAggregation(Aggregation.match(sellMatchCriteria),
					Aggregation.group("actions.data.receiver"));
			AggregationResults<BasicDBObject> sellOutput = mongoTemplate.aggregate(sellAggregation, "transactions",
					BasicDBObject.class);

			List<BasicDBObject> sellResult = sellOutput.getMappedResults();
			for (BasicDBObject bdo : sellResult) {
				result.add(bdo.getString("0"));
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
	}
}
