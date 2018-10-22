package it.etoken.component.eosblock.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.cache.service.CacheService;
import it.etoken.component.eosblock.eosrpc.EosResult;
import it.etoken.component.eosblock.eosrpc.GetAccountInfo;
import it.etoken.component.eosblock.service.RamLargeRankService;
import it.etoken.component.eosblock.utils.EosNodeUtils;

@Component
@Transactional
public class RamLargeRankServiceImpl implements RamLargeRankService{
	
	@Autowired
	EosNodeUtils eosNodeUtils;

	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;
	
	@Autowired
	CacheService cacheService;
	
	@Override
	public List<BasicDBObject> getNewestRank() {
		Query query = new Query();
		query = query.with(new Sort(new Order(Direction.DESC, "ramQuota")));
		query = query.limit(20);
		List<BasicDBObject> RamLargeRankList = mongoTemplate.find(query, BasicDBObject.class, "ram_large_user_rank");
		return RamLargeRankList;
	}

	@Override
	public void getLargeRank() {
		try {
			  String url = "http://api.ram.southex.com/v1/getram/rank";
		      String result = HttpClientUtils.doPostJson(url, "");
		      JSONObject json=JSONObject.parseObject(result); 
		      JSONArray jsonarray = json.getJSONArray("users");
		      if(jsonarray.size()==0) {
		    	  return;
		      }
		      Query query = new Query();
		      mongoTemplate.remove(query, "ram_large_user_rank");
		      for(int i=0;i<jsonarray.size();i++){
		    	  JSONObject user = jsonarray.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
		    	  Double ramQuota=user.getDouble("ramQuota");
		    	  BasicDBObject bdo = new BasicDBObject();
		    	  bdo.put("ramProportion100", json.get("ramProportion100"));
		    	  bdo.put("ramProportion200", json.get("ramProportion200"));
		    	  bdo.put("lastUpdateAt", json.get("lastUpdateAt"));
		    	  bdo.put("totalRamSelled", user.get("totalRamSelled"));
		    	  bdo.put("totalEosEarn", user.get("totalEosEarn"));
		    	  bdo.put("totalProfit", user.get("totalProfit"));
		    	  bdo.put("ramQuota",ramQuota);//可售内存
		    	  bdo.put("holdCost", user.get("holdCost"));
		    	  bdo.put("totalEosCost", user.get("totalEosCost"));
		    	  bdo.put("historyAverageCost", user.get("historyAverageCost"));
		    	  bdo.put("profit", user.get("profit"));
		    	  bdo.put("account", user.get("account"));
		    	  bdo.put("ramValue", user.get("ramValue"));
		    	  bdo.put("createdAt", System.currentTimeMillis());
		    	  bdo.put("updatedAt", System.currentTimeMillis());
		    	  mongoTemplate.insert(bdo, "ram_large_user_rank");
		    	  }  
		  	Query query2 = new Query();
			query2 = query2.with(new Sort(new Order(Direction.DESC, "ramQuota")));
			query2 = query2.limit(20);
			List<BasicDBObject> RamLargeRankList = mongoTemplate.find(query2, BasicDBObject.class, "ram_large_user_rank");
			cacheService.set("ram_large_user_rank", RamLargeRankList,65*60);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}
		
	}

	@Override
	public void getNewLargeRank() {
		//查询半个小时内所有内存交易的用户
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long timesx = 0;
		try {
			timesx = sf.parse(sf.format(new Date())).getTime();
		} catch (Exception e) {
		
		}
		long startTimes = timesx - 30* 60 * 1000;
		Date startDate = new Date(startTimes);
		Object[] actionsNames = new Object[] { "buyram", "sellram" };
		Query query = new Query(Criteria.where("actions.name").in(actionsNames).and("createdAt").gte(startDate).and("actions.account").is("eosio"));
		query = query.with(new Sort(new Order(Direction.DESC, "createdAt")));
		List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
		
		List<BasicDBObject> RamLargeRankList=cacheService.get("ram_large_user_rank",List.class);
		if( null==RamLargeRankList||RamLargeRankList.size()==0) {
			Query querylarge = new Query();
			querylarge = querylarge.with(new Sort(new Order(Direction.DESC, "ramQuota")));
			querylarge = querylarge.limit(200);
		    RamLargeRankList = mongoTemplate.find(querylarge, BasicDBObject.class, "ram_large_user_rank");
		}
	    List<String> listLarger=new ArrayList<>();
		for (Object  object : RamLargeRankList) {
			BasicDBObject thisBasicDBObject = (BasicDBObject) com.mongodb.util.JSON.parse(object.toString());
			listLarger.add(thisBasicDBObject.getString("account"));
		}
		List<String> listOrderActor=new ArrayList<>();
		List<String> listOrderLarger=new ArrayList<>();
		for (BasicDBObject thisBasicDBObject : transactionsList) {
			BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
			BasicDBObject action = (BasicDBObject) actions.get(0);
			BasicDBList authorizations =(BasicDBList) action.get("authorization");
			BasicDBObject authorization = (BasicDBObject) authorizations.get(0);
			String actor=authorization.getString("actor");
			if(listLarger.contains(actor)) {
				listOrderLarger.add(actor);
				cacheService.set("listOrderLarger",listOrderLarger);
			}else {
				listOrderActor.add(actor);
			}
		}
		//查询大单用户当前内存量先查询是不是在大户排行里面
		//在就忽略不在就查询内存量跟大户排行的最后一名的内存数量比较如果大于就插入
		BasicDBObject thisBasicDBObject = (BasicDBObject) com.mongodb.util.JSON.parse(RamLargeRankList.get(RamLargeRankList.size()-1).toString());
		Double ramQuotas=thisBasicDBObject.getDouble("ramQuota");
		BigDecimal  ramQuota=new BigDecimal(ramQuotas).divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
		for (String actor : listOrderActor) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("account_name", actor);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
			EosResult resp = null;
			try {
				resp = new GetAccountInfo().run(eosNodeUtils.getNodeUrls().get("url_chain"), eosNodeUtils.getNodeUrls().get("url_chain_backup"), jsonObject.toString());
				JSONObject json=JSONObject.parseObject(resp.getData());
				BigDecimal ram_bytes = json.getJSONObject("total_resources").getBigDecimal("ram_bytes");
				ram_bytes = ram_bytes.divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
			    if((ram_bytes.compareTo(ramQuota)==1)) {
			    	  BasicDBObject bdo = new BasicDBObject();
			    	  bdo.put("ramProportion100", "50.3");
			    	  bdo.put("ramProportion200", "60.75");
			    	  bdo.put("lastUpdateAt",  System.currentTimeMillis());
			    	  bdo.put("ramQuota",ram_bytes);//可售内存
			    	  bdo.put("holdCost",getholdCost(actor,ram_bytes));
			    	  bdo.put("historyAverageCost", "0");
			    	  bdo.put("totalProfit", "0");
			    	  bdo.put("profit", getprofit(ram_bytes));
			    	  bdo.put("account", actor);
			    	  bdo.put("ramValue",ram_bytes );
			    	  bdo.put("createdAt", System.currentTimeMillis());
			    	  bdo.put("updatedAt", System.currentTimeMillis());
			    	  BigDecimal totalUsedEos=cacheService.get("totalUsedEos", BigDecimal.class);
			    	  bdo.put("totalEosCost",totalUsedEos);
			    	  bdo.put("totalEosEarn",cacheService.get("totalEosEarn", BigDecimal.class));
			    	  bdo.put("totalRamSelled",cacheService.get("sellEos", BigDecimal.class));
			    	  mongoTemplate.insert(bdo, "ram_large_user_rank");
			    }
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}

	@Override
	public void updateLargeRank() {
		@SuppressWarnings("unchecked")
		List<String> listOrderLarger=cacheService.get("listOrderLarger", List.class);
		if(listOrderLarger.size()>0) {
			for (String actor : listOrderLarger) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("account_name", actor);
				} catch (JSONException e2) {
					e2.printStackTrace();
				}
				EosResult resp = null;
				try {
					resp = new GetAccountInfo().run(eosNodeUtils.getNodeUrls().get("url_chain"), eosNodeUtils.getNodeUrls().get("url_chain_backup"), jsonObject.toString());
					JSONObject json=JSONObject.parseObject(resp.getData());
					BigDecimal ram_bytes = json.getJSONObject("total_resources").getBigDecimal("ram_bytes");       
					ram_bytes = ram_bytes.divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
					Query query = new Query(Criteria.where("account").in(actor));
					mongoTemplate.updateFirst(query, Update.update("ramQuota", ram_bytes), BasicDBObject.class, "ram_large_user_rank");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	//计算当前用户成本价
	public BigDecimal getholdCost(String actor,BigDecimal ram_bytes) {
		//根据用户名查询当前用户的内存交易记录并且筛选出买卖EOS的数量
		Object[] actionsNames = new Object[] { "buyram", "sellram" };
		Criteria actorCriteria =Criteria.where("actions.authorization.actor").is(actor);

		Criteria actionsNameCriteria = Criteria.where("actions.name").in(actionsNames);
		Query query = new Query();
		query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
		query = query.limit(200);
		Criteria expirationCriteria = new Criteria();
		expirationCriteria.orOperator(Criteria.where("expiration").exists(true),
				Criteria.where("transaction_header.expiration").exists(true));
		Criteria criteria = new Criteria();
		criteria.andOperator(actorCriteria,expirationCriteria,actionsNameCriteria);
		query = query.addCriteria(criteria);
		List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
		
		BigDecimal buyEos=new BigDecimal("0");
		BigDecimal sellEos=new BigDecimal("0");
		for (BasicDBObject thisBasicDBObject : transactionsList) {
			String trx_id = thisBasicDBObject.getString("trx_id");
			BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
			Object[] thisActions = actions.toArray();
			for (Object thisAction : thisActions) {
				BasicDBObject action = (BasicDBObject) thisAction;
				String actionName = action.getString("name");
				if (!actionName.equalsIgnoreCase("sellram") && !actionName.equalsIgnoreCase("buyram")) {
					continue;
				}
				BasicDBObject data = (BasicDBObject) action.get("data");
				if (actionName.equalsIgnoreCase("buyram")) {
					Double quant = Double.parseDouble(data.getString("quant").replace("EOS", "").trim());
					BigDecimal quantEos=new BigDecimal(quant);
					buyEos=buyEos.add(quantEos);
				} else if (actionName.equalsIgnoreCase("sellram")) {
					BigDecimal quantEOS=findSellRamExactPrice(trx_id);
					sellEos=sellEos.add(quantEOS);
				}
			}
		}
		
		BigDecimal totalUsedEos=buyEos.subtract(sellEos).setScale(6, BigDecimal.ROUND_HALF_UP);
		cacheService.set("totalUsedEos", totalUsedEos);
		cacheService.set("sellEos", sellEos);
		BigDecimal holdCost= totalUsedEos.divide(ram_bytes, 6, BigDecimal.ROUND_HALF_UP);
		return holdCost;
	}
	
	//计算当前用户总盈亏
	public BigDecimal getprofit(BigDecimal ram_bytes) {
		//获取当前内存价格
		BasicDBObject ramPriceInfo=cacheService.get("ram_price_info", BasicDBObject.class);
		Double price=ramPriceInfo.getDouble("price");
		BigDecimal totalValueEos=ram_bytes.multiply(new BigDecimal(price));
		BigDecimal totalUsedEos=cacheService.get("totalUsedEos", BigDecimal.class);
		BigDecimal totalprofitEos=totalValueEos.divide(totalUsedEos,2, BigDecimal.ROUND_HALF_UP);
		cacheService.set("totalEosEarn", totalprofitEos);
		//获取当前eos价格
		Double eosPrice=findEosPrice();
		BigDecimal  profit=totalprofitEos.multiply(new BigDecimal(eosPrice)).setScale(6, BigDecimal.ROUND_HALF_UP);
		return profit;
		
	}
	
	
	//获取当前eos的价格
	public Double findEosPrice() {
			   double peos = 0;
				try {
					JSONObject eosTicker = cacheService.get("ticker_eos",JSONObject.class);
					if(null != eosTicker) {
						peos = eosTicker.getDoubleValue("last_rmb");
					}
			      System.out.println("peos值"+peos); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				return peos;
		}
	
	
	//获取最准确的买内存准确的eos
	public  BigDecimal findSellRamExactPrice(String  trsationId) {
		try {
			Criteria actorCriteria = Criteria.where("id").in(trsationId);
			Query query = new Query(actorCriteria);
			query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
			 List<BasicDBObject> list=mongoTemplate.find(query, BasicDBObject.class,"transaction_traces");
			 for (BasicDBObject thisBasicDBObject :list) {
				 BasicDBList action_traces = (BasicDBList) thisBasicDBObject.get("action_traces");
					Object[] thisActionsTraces = action_traces.toArray();
					for (Object object : thisActionsTraces) {
						BasicDBObject actionTraces = (BasicDBObject)object;
						BasicDBObject actionact=(BasicDBObject)actionTraces.get("act");
						JSONObject actiondata = JSONObject.parseObject(JSONObject.toJSONString(actionact.get("data")), JSONObject.class);
						Integer bytes= (Integer) actiondata.get("bytes");
						if(null==bytes) {
							continue;
						}
						BasicDBList inline_traces = (BasicDBList) actionTraces.get("inline_traces");;
						Object[] thisInlineTraces = inline_traces.toArray();
						if(null == thisInlineTraces || thisInlineTraces.length==0) {
							continue;
						}
						if(thisInlineTraces.length<1) {
							continue;
						}
						if(thisInlineTraces.length==1) {
							BasicDBObject inlineTraces1 = (BasicDBObject)thisInlineTraces[0];
							BasicDBObject act=(BasicDBObject) inlineTraces1.get("act");
							JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(act.get("data")), JSONObject.class);
							String quantityEos=data.getString("quantity");
							if(null==quantityEos) {
								continue;
							}
			            	String[] quantity_eos_array= quantityEos.split(" ");
			            	if(quantity_eos_array.length<1||null==quantity_eos_array) {
			            		continue;
			            	}
			            	BigDecimal eosQuantity= new  BigDecimal(quantity_eos_array[0]);
			            	//eosQuantity除以coinQuantity并保留两位小数单位是eos
			            	return eosQuantity;
						}else {
							BasicDBObject inlineTraces1 = (BasicDBObject)thisInlineTraces[0];
							BasicDBObject inlineTraces2 = (BasicDBObject)thisInlineTraces[1];
							BasicDBObject act=(BasicDBObject) inlineTraces1.get("act");
							//BasicDBObject data=(BasicDBObject)act.get("data");
							JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(act.get("data")), JSONObject.class);
							String quantityEos=data.getString("quantity");
							if(null==quantityEos) {
			            		continue;
			            	}
							BasicDBObject act2=(BasicDBObject) inlineTraces2.get("act");
							//BasicDBObject data2=(BasicDBObject)act2.get("data");
							JSONObject data2 = JSONObject.parseObject(JSONObject.toJSONString(act2.get("data")), JSONObject.class);
							if(null==data2) {
			            		continue;
			            	}
							String quantityFeeEos2=data2.getString("quantity");
							if(null==quantityFeeEos2) {
			            		continue;
			            	}
			            	String[] quantity_eos_array= quantityEos.split(" ");
			            	BigDecimal eosQuantity= new  BigDecimal(quantity_eos_array[0]);
			            	String[] quantity_fee_eos_array= quantityFeeEos2.split(" ");
			            	if(quantity_fee_eos_array.length<1||null==quantity_fee_eos_array) {
			            		continue;
			            	}
			            	BigDecimal feeEosQuantity= new  BigDecimal(quantity_fee_eos_array[0]); 
			            	BigDecimal sellRamEos=eosQuantity.subtract(feeEosQuantity);
			            	return sellRamEos;
						}
					}
			    }
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}
