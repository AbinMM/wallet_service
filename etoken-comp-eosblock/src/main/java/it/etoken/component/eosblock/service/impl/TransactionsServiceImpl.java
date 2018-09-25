package it.etoken.component.eosblock.service.impl;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.utils.DateUtils;
import it.etoken.component.eosblock.service.TransactionsService;

@Component
@Transactional
public class TransactionsServiceImpl implements TransactionsService{
	
	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;
	
	@Autowired
	TransactionsService transactionsService;
	
	
	public BigDecimal getRamPriceByTimes(Long times) {
		Query query = new Query(Criteria.where("record_date").is(times));
		List<BasicDBObject> result = mongoTemplate.find(query, BasicDBObject.class, "ram_price");
		
		if(null != result && !result.isEmpty()) {
			BasicDBObject temp = result.get(0);
			String priceString = temp.getString("price");
			return BigDecimal.valueOf(Double.parseDouble(priceString));
		}else{
			return BigDecimal.valueOf(0.32115);
		}
	}
	
	@Override
	@Deprecated
	public List<JSONObject> findByAccountAndActor(int page, int pageSize, String account, String actor,String code) {
		Criteria accountCriteria = null;
		if(code.equalsIgnoreCase("eos")){
			Object[] accountNames = new Object[] { "eosio", "eosio.token"};
			accountCriteria = Criteria.where("actions.account").in(accountNames);
		}else if(null != actor || !"".equals(actor)){
			accountCriteria = Criteria.where("actions.account").is(account);
		}
		
		Criteria[] actorCriterias = new Criteria[3];
		actorCriterias[0] = Criteria.where("actions.authorization.actor").is(actor);
		actorCriterias[1] = Criteria.where("actions.data.receiver").is(actor);
		actorCriterias[2] = Criteria.where("actions.data.to").is(actor);
		
		Criteria actorCriteria = new Criteria();
		actorCriteria.orOperator(actorCriterias);
		
		Criteria codeCriteria = new Criteria();
//		Object[] actionstNames = new Object[] { "delegatebw", "sellram","undelegatebw"};
		Pattern pattern=Pattern.compile("^.*"+code+".*$", Pattern.CASE_INSENSITIVE);
		codeCriteria.orOperator(Criteria.where("actions.data.quantity").regex(pattern),
				Criteria.where("actions.name").is("delegatebw"),
				Criteria.where("actions.name").is("sellram"),
				Criteria.where("actions.name").is("undelegatebw")
//				Criteria.where("actions.name").is("newaccount")
//                Criteria.where("actions.data.stake_cpu_quantit").regex(pattern),
//                Criteria.where("actions.data.quant").regex(pattern),
               );
		
		Criteria blockIdCriteria =Criteria.where("block_id").exists(true);
		
		
		Criteria criteria = new Criteria();
		if(accountCriteria!=null) {
		   criteria.andOperator(accountCriteria,actorCriteria,codeCriteria,blockIdCriteria);
		   System.out.println(criteria.getCriteriaObject());
		}else {
		   criteria.andOperator(actorCriteria,codeCriteria,blockIdCriteria);
		   System.out.println(criteria.getCriteriaObject());
		}
	    Query query = new Query(criteria);
	    query = query.with(new Sort(new Order(Direction.DESC, "createdAt")));
	    query = query.limit(pageSize);
	    query = query.skip((page - 1) * pageSize);

	    List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
	    
	    List<JSONObject> list=new ArrayList<JSONObject>();
		for (BasicDBObject thisBasicDBObject :transactionsList) {
			String type="";
			String to="";
			String from="";
			String quantity="0.0";
			String memo="";
			String description="";
			String code_new="";
			String transactionId=thisBasicDBObject.getString("trx_id");
			String blockNum=thisBasicDBObject.getString("block_num");
			Date  blockTime=thisBasicDBObject.getDate("createdAt");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        	Long times = 0l;
			BigDecimal price = BigDecimal.ZERO;
			try {
				times = sdf.parse(sdf.format(blockTime)).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			price = this.getRamPriceByTimes(times);
			BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
			Object[] thisActions = actions.toArray();
			for(Object thisAction : thisActions) {
				BasicDBObject action = (BasicDBObject)thisAction;
				BasicDBObject data = (BasicDBObject) action.get("data");
				String actionName = action.getString("name");
				 if(actionName.equalsIgnoreCase("transfer")) {
		            	description="转账";
		            	to=data.getString("to");
		            	from=data.getString("from");
		            	if(from.equals(actor)) {
		 			    	type="转出";
		 			    }
		 			    if(to.equals(actor)) {
		 			    	type="转入";
		 			    }
		            	quantity=data.getString("quantity");
		             	String[] quantity_arra= quantity.split(" ");
		             	quantity=quantity_arra[0];
		             	code_new=quantity_arra[1];
		            	memo=data.getString("memo");
		            }else if(actionName.equalsIgnoreCase("newaccount")) {
		            	memo="";
		            	from=data.getString("creator");
		            	to=data.getString("name");
		            	type="创建账号";
		            	description="创建账号";
		            	quantity="0.0";
		            }else if(actionName.equalsIgnoreCase("delegatebw")) {
		            	memo="";
		            	from=data.getString("from");
		            	to=data.getString("receiver");
		            	String stake_net_quantity=data.getString("stake_net_quantity");
		            	String[] stake_net_quantity_array= stake_net_quantity.split(" ");
		            	String stake_cpu_quantity=data.getString("stake_cpu_quantity");
		            	String[] stake_cpu_quantity_array= stake_cpu_quantity.split(" ");
		            	BigDecimal netQuantity= new  BigDecimal(stake_net_quantity_array[0]);
		            	BigDecimal cpuQuantity= new  BigDecimal(stake_cpu_quantity_array[0]);
		            	BigDecimal quantitys = null; 
		            	code_new=stake_cpu_quantity_array[1];
		            	type="转出";
		            	description="抵押";
		            	quantitys=netQuantity.add(cpuQuantity);
		            	quantity=quantitys.toString();
		            }else if(actionName.equalsIgnoreCase("undelegatebw")) {
		            	memo="";
		            	from=data.getString("from");
		            	to=data.getString("receiver");
		            	String unstake_net_quantity=data.getString("unstake_net_quantity");
		            	String[] unstake_net_quantity_array= unstake_net_quantity.split(" ");
		            	String unstake_cpu_quantity=data.getString("unstake_cpu_quantity");
		            	String[] unstake_cpu_quantity_array= unstake_cpu_quantity.split(" ");
		            	BigDecimal netQuantity= new  BigDecimal(unstake_net_quantity_array[0]);
		            	BigDecimal cpuQuantity= new  BigDecimal(unstake_cpu_quantity_array[0]);
		            	code_new=unstake_cpu_quantity_array[1];
		            	BigDecimal quantitys = null; 
		            	type="转入";
	            		description="赎回";
	            		quantitys=netQuantity.add(cpuQuantity);
		            	quantity=quantitys.toString();
		            }else if(actionName.equalsIgnoreCase("buyram")) {
		            	description="内存购买";
		            	memo="";
		            	type="转出";
		            	from=data.getString("payer");
		             	to=data.getString("receiver");
		            	quantity=data.getString("quant");
		            	String[] quantity_arra= quantity.split(" ");
		             	quantity=quantity_arra[0];
		             	code_new=quantity_arra[1];
		            }else if(actionName.equalsIgnoreCase("sellram")) {
		            	description="内存出售";
		            	memo="";
		            	type="转入";
		            	to="";
		            	from=data.getString("account");
		            	Long bytes = data.getLong("bytes");
						BigDecimal bytesK = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(1024l), 2, BigDecimal.ROUND_HALF_UP);
						BigDecimal eos_qty = bytesK.multiply(price);
						eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
						quantity=eos_qty.toString();
						code_new=code;
		            }else if(actionName.equalsIgnoreCase("issue")) {
		            	description="发行";
		            	memo="";
		            	type="转入";
		            	to=data.getString("to");
		            	from="";
		            	quantity=data.getString("quantity");
		            	String[] quantity_arra= quantity.split(" ");
		             	quantity=quantity_arra[0];
		             	code_new=quantity_arra[1];
		            }else {
		            	continue;
					}
					    JSONObject jsonObjects = new JSONObject();
					    jsonObjects.put("quantity", quantity+code_new);//code_new是单位如EOS,MSP	
						jsonObjects.put("description", description);
						jsonObjects.put("memo",memo);
						jsonObjects.put("from", from);
						jsonObjects.put("blockNum", blockNum);
						jsonObjects.put("blockTime", sdf.format(blockTime));
						jsonObjects.put("to", to);
						jsonObjects.put("type", type);
						jsonObjects.put("transactionId", transactionId);
						if (null == code || code.isEmpty()) {
						jsonObjects.put("code", code_new);	
						}else {
						jsonObjects.put("code", code);		
						}
				        list.add(jsonObjects);
			}	
		}
		return list;
	}

	@Override
	public List<JSONObject> findByAccountAndActorNew(String last_id, int pageSize, String account, String actor,String code) {
		Date startDate = null;
		if (null != last_id && !last_id.isEmpty()) {
			Query query = new Query(Criteria.where("_id").is(new ObjectId(last_id)));
			List<BasicDBObject> existTransactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
			if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
				startDate = existTransactionsList.get(0).getDate("createdAt");
			}
		}
		
		Pattern pattern=Pattern.compile("^.*"+code+".*$", Pattern.CASE_INSENSITIVE);
		
		Criteria[] actorCriterias = new Criteria[3];
		actorCriterias[0] = Criteria.where("actions.authorization.actor").is(actor);
		actorCriterias[1] = Criteria.where("actions.data.receiver").is(actor);
		actorCriterias[2] = Criteria.where("actions.data.to").is(actor);
		
		Criteria actorCriteria = new Criteria();
		actorCriteria.orOperator(actorCriterias);
		
		//eos
		Criteria eosCriteria = new Criteria();
		Criteria transfer_and_unit_Criteria = new Criteria();
		transfer_and_unit_Criteria.andOperator(
				Criteria.where("actions.name").is("transfer"),Criteria.where("actions.data.quantity").regex(pattern)
				);
		Criteria actions_name_eos_criteria = new Criteria();
		actions_name_eos_criteria.orOperator(
				Criteria.where("actions.name").is("delegatebw"),
				Criteria.where("actions.name").is("buyram"),
				Criteria.where("actions.name").is("sellram"),
				Criteria.where("actions.name").is("undelegatebw"),
				transfer_and_unit_Criteria
				);
		
		eosCriteria.andOperator(
				actorCriteria,
				Criteria.where("actions.account").in("eosio.token","eosio"),
				actions_name_eos_criteria
				);
		
		//ET
		Criteria etCriteria = new Criteria();
		Criteria buyselltokenCriteria = new Criteria();
		Criteria buytokenCriteria = new Criteria();
		Criteria selltokenCriteria = new Criteria();
		if(code.equalsIgnoreCase("eos")){
			buytokenCriteria.andOperator(
					Criteria.where("actions.name").is("buytoken"),
					Criteria.where("actions.data.eos_quant").regex(".*"+code)
					);
			selltokenCriteria=Criteria.where("actions.name").is("selltoken");
		}else {
			buytokenCriteria.andOperator(
					Criteria.where("actions.name").is("buytoken"),
					Criteria.where("actions.data.token_symbol").regex(".*"+code)
					);
			selltokenCriteria.andOperator(
					Criteria.where("actions.name").is("selltoken"),
					Criteria.where("actions.data.quant").regex(".*"+code)
					);
		}
		buyselltokenCriteria.orOperator(buytokenCriteria, selltokenCriteria);
		if(null==account|| "".equals(account)||code.equalsIgnoreCase("eos")) {
			etCriteria.andOperator(
					Criteria.where("actions.account").is("etbexchanger"),
					actorCriteria,
					buyselltokenCriteria);
		}else {
			etCriteria.andOperator(
					Criteria.where("actions.account").is("etbexchanger"),
					actorCriteria,
					Criteria.where("actions.data.token_contract").is(account),
					buyselltokenCriteria);
		}
		//Other
		Criteria otherCriteria = new Criteria();
		Criteria actions_name_other_criteria = new Criteria();
		actions_name_other_criteria.andOperator(
				Criteria.where("actions.data.quantity").regex(pattern),
				Criteria.where("actions.name").is("transfer")
				);
        if(null==account|| "".equals(account)) {
			otherCriteria.andOperator(actions_name_other_criteria,actorCriteria);
   		}else {
			otherCriteria.andOperator(actions_name_other_criteria,actorCriteria, Criteria.where("actions.account").is(account));	
		}
        
		Criteria criteria = new Criteria();
		if(code.equalsIgnoreCase("eos")){
		    criteria.orOperator(eosCriteria, etCriteria);
		}else {
			criteria.orOperator(otherCriteria, etCriteria);
		}
		Map<String, String> existMap = new HashMap<String, String>();
		List<JSONObject> list=new ArrayList<JSONObject>();
		boolean haveList = true;
		int countN = 0;
		Object[] objs=new Object[100];
		int i=0;
		do {
		    Query query = new Query(criteria);
		    query = query.with(new Sort(new Order(Direction.DESC, "createdAt")));
		    query = query.limit(pageSize);
		    //query = query.skip((page - 1) * pageSize);
		    if (null != startDate) {
				query = query.addCriteria(Criteria.where("createdAt").lt(startDate));
			}else {
				query = query.addCriteria(Criteria.where("createdAt").exists(true));
			}
		   // List<Transactions> transactionsList = mongoTemplate.find(query, Transactions.class);
		    List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
			if(null == transactionsList || transactionsList.isEmpty()) {
				haveList = false;
				break;
			}
			startDate = transactionsList.get(transactionsList.size()-1).getDate("createdAt");
			for (BasicDBObject thisBasicDBObject :transactionsList) {
 				String transactionId=thisBasicDBObject.getString("trx_id");
				if (existMap.containsKey(transactionId)) {
					continue;
				}
				String blockNum=thisBasicDBObject.getString("block_num");
				if(blockNum==null || blockNum.isEmpty()) {
					Date time=thisBasicDBObject.getDate("createdAt");
					Date newDate=new Date();
					if(newDate.getTime()-time.getTime()>10*60*1000) {
						continue;
					}
					Query queryBlockNum = new Query(Criteria.where("trx_id").is(transactionId));
					queryBlockNum = queryBlockNum.addCriteria(Criteria.where("block_id").exists(true));
					queryBlockNum = queryBlockNum.with(new Sort(new Order(Direction.DESC, "updatedAt")));
					queryBlockNum = queryBlockNum.limit(1);
					List<BasicDBObject> existTransactionsList = mongoTemplate.find(queryBlockNum, BasicDBObject.class, "transactions");
					if (null != existTransactionsList && !existTransactionsList.isEmpty()) {
						thisBasicDBObject=existTransactionsList.get(0);
						blockNum=thisBasicDBObject.getString("block_num");
					}
				}
				String type="";
				String to="";
				String from="";
				String quantity="0.0";
				String memo="";
				String description="";
				String code_new="";
				String _id=thisBasicDBObject.getString("_id");
				Date  blockTime=thisBasicDBObject.getDate("createdAt");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        	Long times = 0l;
				BigDecimal price = BigDecimal.ZERO;
				try {
					times = sdf.parse(sdf.format(blockTime)).getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				price = this.getRamPriceByTimes(times);
				BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
				Object[] thisActions = actions.toArray();
				for(Object thisAction : thisActions) {
					BasicDBObject action = (BasicDBObject)thisAction;
					BasicDBObject data = (BasicDBObject) action.get("data");
					String actionName = action.getString("name");
					 if(actionName.equalsIgnoreCase("transfer")) {
			            	description="转账";
			            	to=data.getString("to");
			            	from=data.getString("from");
			            	if(from.equals(actor)) {
			 			    	type="转出";
			 			    }
			 			    if(to.equals(actor)) {
			 			    	type="转入";
			 			    }
			            	quantity=data.getString("quantity");
			             	String[] quantity_arra= quantity.split(" ");
			             	quantity=quantity_arra[0];
			             	code_new=quantity_arra[1];
			            	memo=data.getString("memo");
			            }else if(actionName.equalsIgnoreCase("newaccount")) {
			            	memo="";
			            	from=data.getString("creator");
			            	to=data.getString("name");
			            	type="创建账号";
			            	description="创建账号";
			            	quantity="0.0";
			            }else if(actionName.equalsIgnoreCase("delegatebw")) {
			            	memo="";
			            	from=data.getString("from");
			            	to=data.getString("receiver");
			            	String stake_net_quantity=data.getString("stake_net_quantity");
			            	String[] stake_net_quantity_array= stake_net_quantity.split(" ");
			            	String stake_cpu_quantity=data.getString("stake_cpu_quantity");
			            	String[] stake_cpu_quantity_array= stake_cpu_quantity.split(" ");
			            	BigDecimal netQuantity= new  BigDecimal(stake_net_quantity_array[0].trim());
			            	BigDecimal cpuQuantity= new  BigDecimal(stake_cpu_quantity_array[0].trim());
			            	BigDecimal quantitys = null; 
			            	code_new=stake_cpu_quantity_array[1];
			            	type="转出";
			            	description="抵押";
			            	quantitys=netQuantity.add(cpuQuantity);
			            	quantity=quantitys.toString();
			            }else if(actionName.equalsIgnoreCase("undelegatebw")) {
			            	memo="";
			            	from=data.getString("from");
			            	to=data.getString("receiver");
			            	String unstake_net_quantity=data.getString("unstake_net_quantity");
			            	String[] unstake_net_quantity_array= unstake_net_quantity.split(" ");
			            	String unstake_cpu_quantity=data.getString("unstake_cpu_quantity");
			            	String[] unstake_cpu_quantity_array= unstake_cpu_quantity.split(" ");
			            	BigDecimal netQuantity= new  BigDecimal(unstake_net_quantity_array[0]);
			            	BigDecimal cpuQuantity= new  BigDecimal(unstake_cpu_quantity_array[0]);
			            	code_new=unstake_cpu_quantity_array[1];
			            	BigDecimal quantitys = null; 
			            	type="转入";
		            		description="赎回";
		            		quantitys=netQuantity.add(cpuQuantity);
			            	quantity=quantitys.toString();
			            }else if(actionName.equalsIgnoreCase("buyram")) {
			            	description="内存购买";
			            	memo="";
			            	type="转出";
			            	from=data.getString("payer");
			            	to=data.getString("receiver");
			            	quantity=data.getString("quant");
			            	String[] quantity_arra= quantity.split(" ");
			             	quantity=quantity_arra[0];
			             	code_new=quantity_arra[1];
			            }else if(actionName.equalsIgnoreCase("sellram")) {
//			            	Object[] obj=new Object[1];
//			            	obj[0]=transactionId;
//			            	Map<String, String> priceMap=transactionsService.findSellRamExactPrice(obj);
//			        		price=new BigDecimal(priceMap.get(transactionId));
			            	description="内存出售";
			            	memo="";
			            	type="转入";
			            	to="";
			            	from=data.getString("account");
			            	Long bytes = data.getLong("bytes");
							BigDecimal bytesK = BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(1024l), 2, BigDecimal.ROUND_HALF_UP);
							BigDecimal eos_qty = bytesK.multiply(price);
							eos_qty.setScale(4, BigDecimal.ROUND_HALF_UP);
							quantity=eos_qty.toString();
							code_new=code;
			            }else if(actionName.equalsIgnoreCase("issue")) {
			            	description="发行";
			            	memo="";
			            	type="转入";
			            	to=data.getString("to").trim();
			            	if(!actor.equals(to)) {
			            		continue;
			            	}
			            	from="";
			            	quantity=data.getString("quantity");
			            	String[] quantity_arra= quantity.split(" ");
			             	quantity=quantity_arra[0];
			             	code_new=quantity_arra[1];
			            }else if(actionName.equalsIgnoreCase("buytoken")) {
			            	description="购买";
			            	memo="";
			            	type="转入";
			            	to=data.getString("payer").trim();
			            	from="";
							if(code.equalsIgnoreCase("eos")){
								quantity=data.getString("eos_quant").trim();
				            	String[] quantity_arra= quantity.split(" ");
				             	quantity=quantity_arra[0];
				             	code_new=quantity_arra[1];
				             	String quant=data.getString("token_symbol").trim();
				            	String[] quant_arra= quant.split(",");
				             	description="购买"+quant_arra[1];
				              	type="转出";
							}else {
								objs[i]=transactionId;
								i++;
							}
			            	code_new=code;
			            }else if(actionName.equalsIgnoreCase("selltoken")) {
			            	description="出售";
			            	memo="";
			            	type="转出";
			            	to="";
			            	from=data.getString("receiver").trim();
                            if(!code.equalsIgnoreCase("eos")){
                            	quantity=data.getString("quant");
    			            	String[] quantity_arra= quantity.split(" ");
    			             	quantity=quantity_arra[0];
    			             	code_new=quantity_arra[1];
							}else {
								String quant=data.getString("quant");
    			            	String[] quantity_arra= quant.split(" ");
    			             	code_new=quantity_arra[1];
								description="出售"+code_new;
								type="转入";
								objs[i]=transactionId;
								i++;
							}
			            
			            }else {
			            	continue;
						}
						    JSONObject jsonObjects = new JSONObject();
						    jsonObjects.put("_id", _id);
						    jsonObjects.put("quantity", quantity+" "+code_new);//code_new是单位如EOS,MSP	
							jsonObjects.put("description", description);
							jsonObjects.put("memo",memo);
							jsonObjects.put("from", from);
							jsonObjects.put("blockNum", blockNum);
							jsonObjects.put("blockTime", sdf.format(blockTime));
							jsonObjects.put("to", to);
							jsonObjects.put("type", type);
							jsonObjects.put("transactionId", transactionId);
							if (null == code || code.isEmpty()) {
							jsonObjects.put("code", code_new);	
							}else {
							jsonObjects.put("code", code);		
							}
							existMap.put(transactionId, transactionId);
					        list.add(jsonObjects);
					    	countN++;
							if(countN == pageSize) {
								if(null!=objs) {
									Map<String, String> quateMap=findbuyETExchangeExactQuant(objs);
								    for (JSONObject jsonObject : list) {
								    	String transactionId1=jsonObject.getString("transactionId");
								    	String quantity1=quateMap.get(transactionId1);
								    	if(null==quantity1) {
											continue;
										}
								    	jsonObject.put("quantity", quantity1);
									}
								}
								existMap.clear();
								return list;
					}
				}
		    }
        } while (haveList);
		if(null!=objs) {
			Map<String, String> quateMap=findbuyETExchangeExactQuant(objs);
		    for (JSONObject jsonObject : list) {
		    	String transactionId=jsonObject.getString("transactionId");
		    	String quantity=quateMap.get(transactionId);
		    	if(null==quantity) {
					continue;
				}
		    	jsonObject.put("quantity", quantity);
			}
		}
		existMap.clear();
		return list;
	}

	@Override
	public List<BasicDBObject> findAccountCoins(String account, String actor) {
		try {
			Criteria[]  accountCriterias =  new Criteria[2];
			accountCriterias[0]=Criteria.where("actions.account").is(actor);//合约账号
			accountCriterias[1]=Criteria.where("actions.data.token_contract").is(actor);//合约账号
			Criteria[] actorCriterias = new Criteria[3];
			actorCriterias[0] = Criteria.where("actions.authorization.actor").is(account);
			actorCriterias[1] = Criteria.where("actions.data.receiver").is(account);
			actorCriterias[2] = Criteria.where("actions.data.to").is(account);
			Criteria actorCriteria = new Criteria();
			actorCriteria.orOperator(actorCriterias);
			Criteria accountCriteria = new Criteria();
			accountCriteria.orOperator(accountCriterias);
			Criteria criteria = new Criteria();
			criteria.andOperator(accountCriteria,actorCriteria);
			Query query = new Query(criteria);
		    query = query.limit(1);
		    List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
		    return transactionsList;
		} catch (Exception e) {
			e.printStackTrace();
			 return null;
		}
	}
	
	@Override
	public  Map<String, String> findETExchangeExactPrice(Object[] trsationId) {
		try {
			Criteria actorCriteria = Criteria.where("id").in(trsationId);
			Query query = new Query(actorCriteria);
			query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
			 List<BasicDBObject> list=mongoTemplate.find(query, BasicDBObject.class,"transaction_traces");
			 Map<String, String> pricetMap = new HashMap<String, String>();
			 for (BasicDBObject thisBasicDBObject :list) {
				 String id=(String) thisBasicDBObject.get("id");
				 BasicDBList action_traces = (BasicDBList) thisBasicDBObject.get("action_traces");
					Object[] thisActionsTraces = action_traces.toArray();
					for (Object object : thisActionsTraces) {
						BasicDBObject actionTraces = (BasicDBObject)object;
						BasicDBList inline_traces = (BasicDBList) actionTraces.get("inline_traces");;
						Object[] thisInlineTraces = inline_traces.toArray();
						if(null == thisInlineTraces || thisInlineTraces.length==0||thisInlineTraces.length<2) {
							continue;
						}
						BasicDBObject inlineTraces1 = (BasicDBObject)thisInlineTraces[0];	
						BasicDBObject inlineTraces2= (BasicDBObject)thisInlineTraces[1];
						BasicDBObject act=(BasicDBObject) inlineTraces1.get("act");
						BasicDBObject data=(BasicDBObject)act.get("data");
						String quantity1=(String)data.get("quantity");//如果是sell就是买的币的数量如果是buy就是eos的数量
						BasicDBObject act1=(BasicDBObject) inlineTraces2.get("act");
						//BasicDBObject data1=(BasicDBObject)act1.get("data");
						JSONObject data1 = JSONObject.parseObject(JSONObject.toJSONString(act1.get("data")), JSONObject.class);
						String quantity2=(String)data1.get("quantity");//如果是sell就是eos的数量的数量如果是buy就是币的数量
						if(null==quantity2) {
							continue;
						}
		            	String[] quantity1_array= quantity1.split(" ");
		            	String[] quantity2_array= quantity2.split(" ");
		            	BigDecimal quantityarr1= new  BigDecimal(quantity1_array[0]);
		            	String code1=quantity1_array[1];
		            	BigDecimal quantityarr2= new  BigDecimal(quantity2_array[0]);
		            	String code2=quantity2_array[1];
		                if(code1.equals("EOS")) {
		                	BigDecimal price= quantityarr1.divide(quantityarr2, 10, BigDecimal.ROUND_HALF_UP);
		                	pricetMap.put(id,price.toPlainString());
		                }
		            	if(code2.equals("EOS")) {
		            		BigDecimal price= quantityarr2.divide(quantityarr1, 10, BigDecimal.ROUND_HALF_UP);
		            		pricetMap.put(id,price.toPlainString());
		            	}
					}
			    }
			return pricetMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	public Map<String, String> findbuyETExchangeExactQuant(Object[] trsationId) {
		try {
			Criteria actorCriteria = Criteria.where("id").in(trsationId);
			Query query = new Query(actorCriteria);
			query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
			 List<BasicDBObject> list=mongoTemplate.find(query, BasicDBObject.class,"transaction_traces");
			 Map<String, String> quantMap = new HashMap<String, String>();
			 for (BasicDBObject thisBasicDBObject :list) {
				 String id=(String) thisBasicDBObject.get("id");
				 BasicDBList action_traces = (BasicDBList) thisBasicDBObject.get("action_traces");
					Object[] thisActionsTraces = action_traces.toArray();
					for (Object object : thisActionsTraces) {
						BasicDBObject actionTraces = (BasicDBObject)object;
						BasicDBList inline_traces = (BasicDBList) actionTraces.get("inline_traces");;
						Object[] thisInlineTraces = inline_traces.toArray();
						if(null == thisInlineTraces || thisInlineTraces.length==0) {
							continue;
						}
						BasicDBObject inlineTraces1= (BasicDBObject)thisInlineTraces[1];
						BasicDBObject act1=(BasicDBObject) inlineTraces1.get("act");
						BasicDBObject data1=(BasicDBObject)act1.get("data");
						String quantity=(String)data1.get("quantity");//如果是sell就是eos的数量的数量如果是buy就是币的数量
						quantMap.put(id,quantity);
					}
			    }
			return quantMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public  Map<String, String> findSellRamExactPrice(Object[] trsationId) {
		try {
			Criteria actorCriteria = Criteria.where("id").in(trsationId);
			Query query = new Query(actorCriteria);
			query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
			 List<BasicDBObject> list=mongoTemplate.find(query, BasicDBObject.class,"transaction_traces");
			 Map<String, String> pricetMap = new HashMap<String, String>();
			 for (BasicDBObject thisBasicDBObject :list) {
				 String id=(String) thisBasicDBObject.get("id");
				 BasicDBList action_traces = (BasicDBList) thisBasicDBObject.get("action_traces");
					Object[] thisActionsTraces = action_traces.toArray();
					for (Object object : thisActionsTraces) {
						BasicDBObject actionTraces = (BasicDBObject)object;
						BasicDBObject actionact=(BasicDBObject)actionTraces.get("act");
						//BasicDBObject actiondata=(BasicDBObject)actionact.get("data");
						JSONObject actiondata = JSONObject.parseObject(JSONObject.toJSONString(actionact.get("data")), JSONObject.class);
						Integer bytes= (Integer) actiondata.get("bytes");
						if(null==bytes) {
							continue;
						}
						BigDecimal bytes1=new BigDecimal(bytes.toString());
						BigDecimal kb= bytes1.divide(new BigDecimal(1024), 10, BigDecimal.ROUND_HALF_UP);
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
							//BasicDBObject data=(BasicDBObject)act.get("data");
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
			            	BigDecimal price= eosQuantity.divide(kb, 6, BigDecimal.ROUND_HALF_UP);
			            	pricetMap.put(id,price.toPlainString());
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
			            	//eosQuantity除以coinQuantity并保留两位小数单位是eos
			            	BigDecimal price= sellRamEos.divide(kb, 6, BigDecimal.ROUND_HALF_UP);
			            	pricetMap.put(id,price.toPlainString());
						}
					}
			    }
			return pricetMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	@Override
	public List<JSONObject> getEosTransactionRecord(int start, int count, String account, String sort, String token,
			String contract) {
		Criteria accountCriteria = null;
		if(!"".equals(contract)&&null != contract){
			accountCriteria = Criteria.where("actions.account").is(contract);
		}
		Criteria[] actorCriterias = new Criteria[3];
		actorCriterias[0] = Criteria.where("actions.authorization.actor").is(account);
		actorCriterias[1] = Criteria.where("actions.data.receiver").is(account);
		actorCriterias[2] = Criteria.where("actions.data.to").is(account);
		
		Criteria actorCriteria = new Criteria();
		actorCriteria.orOperator(actorCriterias);
		
		Criteria codeCriteria = new Criteria();
		if(!"".equals(token)&&null != token) {
			Pattern pattern=Pattern.compile("^.*"+token+".*$", Pattern.CASE_INSENSITIVE);
			codeCriteria.orOperator(Criteria.where("actions.data.quantity").regex(pattern),
					Criteria.where("actions.name").is("delegatebw"),
					Criteria.where("actions.name").is("sellram"),
					Criteria.where("actions.name").is("undelegatebw")
	               );
		}
		Criteria criteria = new Criteria();
		if(accountCriteria!=null&& codeCriteria!=null) {
		   criteria.andOperator(accountCriteria,actorCriteria,codeCriteria);
		   System.out.println(criteria.getCriteriaObject());
		}else {
		   criteria=actorCriteria;
		   System.out.println(criteria.getCriteriaObject());
		}
		 Query query = new Query(criteria);
		 if(sort.equals("desc")) {
		       query = query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
		 }
		 if(sort.equals("asc")) {
		       query = query.with(new Sort(new Order(Direction.ASC, "expiration"),new Order(Direction.ASC, "transaction_header.expiration")));
		 }
		 query = query.limit(count);
		 query = query.skip(start);
		 List<BasicDBObject> transactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");	    
		 List<JSONObject> list=new ArrayList<JSONObject>();
		 for (BasicDBObject thisBasicDBObject :transactionsList) {
				String transactionId=thisBasicDBObject.getString("trx_id");
				String blockNum=thisBasicDBObject.getString("block_num");
				Date  blockTime=null;
				if(null!=thisBasicDBObject.getString("expiration")) {
				    blockTime=new Date(DateUtils.formateDate(thisBasicDBObject.getString("expiration")).getTime()-30*1000);
				}else {
					JSONObject obj=JSONObject.parseObject(thisBasicDBObject.get("transaction_header").toString());
					blockTime=new Date(DateUtils.formateDate(obj.getString("expiration")).getTime()-30*1000);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        	Long times = 0l;
				BigDecimal price = BigDecimal.ZERO;
				try {
					times = sdf.parse(sdf.format(blockTime)).getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				price = this.getRamPriceByTimes(times);
				BasicDBList actions = (BasicDBList) thisBasicDBObject.get("actions");
				Object[] thisActions = actions.toArray();
				for(Object thisAction : thisActions) {
					BasicDBObject action = (BasicDBObject)thisAction;
					BasicDBObject data = (BasicDBObject) action.get("data");
					String actionName = action.getString("name");
					String memo= data.getString("memo");
					if(null==memo) {
						memo="";
					}
					String from=data.getString("from");
					if(null==from) {
						from="";
					}
					String account1=action.getString("account");
					if(null==account1) {
						account1="";
					}
					String quantity=data.getString("quantity");
					String count1="";
					String symbol= "";
					if(null==quantity) {
						quantity="";
					}else {
						String[] quantity_arra= data.getString("quantity").split(" ");
					    count1=quantity_arra[0];
						symbol= quantity_arra[1];
					}
					String bytes=data.getString("bytes");
					if(null==bytes) {
						bytes="";
					}
					JSONObject jsonObjects = new JSONObject();
				    jsonObjects.put("quantity", quantity);//code_new是单位如EOS,MSP	
					jsonObjects.put("memo",memo);
					jsonObjects.put("from", from);
					jsonObjects.put("blockNum", blockNum);
					jsonObjects.put("timestamp", sdf.format(blockTime));	
					jsonObjects.put("transactionId", transactionId);
					jsonObjects.put("account", account1);
					jsonObjects.put("name", actionName);
					jsonObjects.put("data", data);
					jsonObjects.put("count", count1);
					jsonObjects.put("symbol",symbol);	
					jsonObjects.put("ram_price", price);	
					jsonObjects.put("bytes", bytes);	
			        list.add(jsonObjects);       
				}	
			}
			return list;
	} 
	
	@Override
	public List<JSONObject> findAllTransferInByAccountAndTokenName(String account,  String tokenName, String to, int page, int pageCount) {
		Query query = new Query();
		
		Criteria criteria = new Criteria();
		criteria.andOperator(
				Criteria.where("actions.account").is(account),
				Criteria.where("actions.name").in("transfer"),
				Criteria.where("actions.data.to").is(to),
				Criteria.where("actions.data.quantity").regex(".*" + tokenName)
				);
		query.addCriteria(criteria);
		query.with(new Sort(new Order(Direction.DESC, "expiration"),new Order(Direction.DESC, "transaction_header.expiration")));
		query.limit(pageCount);
		query.skip((page-1)*pageCount);
		
		List<JSONObject> result = mongoTemplate.find(query, JSONObject.class, "transactions");
		return result;
	}
}
