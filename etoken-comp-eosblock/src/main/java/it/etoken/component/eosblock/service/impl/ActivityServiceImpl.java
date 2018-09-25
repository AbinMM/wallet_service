package it.etoken.component.eosblock.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mongodb.BasicDBObject;

import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.base.model.eosblock.entity.ActivityInfo;
import it.etoken.base.model.eosblock.entity.ActivityInfoExample;
import it.etoken.base.model.eosblock.entity.ActivityStage;
import it.etoken.base.model.eosblock.entity.ActivityStageExample;
import it.etoken.base.model.eosblock.entity.ActivityStageUser;
import it.etoken.base.model.eosblock.entity.ActivityStageUserExample;
import it.etoken.component.eosblock.dao.mapper.ActivityInfoMapper;
import it.etoken.component.eosblock.dao.mapper.ActivityStageMapper;
import it.etoken.component.eosblock.dao.mapper.ActivityStageUserMapper;
import it.etoken.component.eosblock.service.ActivityService;

@Component
@Transactional
public class ActivityServiceImpl implements ActivityService {

	private final static Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

	@Autowired
	ActivityInfoMapper activityInfoMapper;

	@Autowired
	ActivityStageMapper activityStageMapper;

	@Autowired
	ActivityStageUserMapper activityStageUserMapper;

	@Value("${eos.server.api}")
	String EOS_SERVER_API;

	@Autowired
	@Qualifier(value = "primaryMongoTemplate")
	MongoOperations mongoTemplate;

	public List<ActivityInfo> getDoingActivityInfoList() {
		Date nowDate = new Date();
		ActivityInfoExample example = new ActivityInfoExample();
		ActivityInfoExample.Criteria astCriteria = example.createCriteria();
		astCriteria.andStatusEqualTo("doing");

		Date endDate = new Date(nowDate.getTime() - 1 * 60 * 60 * 1000); // 延长过期一个小时以免没有取全数据
		ActivityInfoExample.Criteria orCriteria = example.or();
		orCriteria.andStartDateLessThanOrEqualTo(nowDate);
		orCriteria.andEndDateGreaterThanOrEqualTo(endDate);
		List<ActivityInfo> activityInfoList = activityInfoMapper.selectByExample(example);

		return activityInfoList;
	}

	public List<ActivityInfo> getCompletedActivityInfoList() {
		ActivityInfoExample example = new ActivityInfoExample();
		ActivityInfoExample.Criteria astCriteria = example.createCriteria();
		astCriteria.andStatusEqualTo("completed");
		List<ActivityInfo> activityInfoList = activityInfoMapper.selectByExample(example);

		return activityInfoList;
	}

	public List<ActivityStage> getNeedGetUserActivityStage(long activityId) {
		Date nowDate = new Date();
		Date endDate = new Date(nowDate.getTime() - 1 * 60 * 60 * 1000); // 延长过期一个小时以免没有取全数据
		ActivityStageExample example = new ActivityStageExample();
		ActivityStageExample.Criteria astCriteria = example.createCriteria();
		astCriteria.andActivityIdEqualTo(activityId);
		astCriteria.andStartDateLessThanOrEqualTo(nowDate);
		astCriteria.andEndDateGreaterThanOrEqualTo(endDate);
		List<ActivityStage> activityStages = activityStageMapper.selectByExample(example);

		return activityStages;
	}

	public void getCurrentBuyUsers(ActivityStage currentActivityStage) {
		Date nowDate = new Date();
		if (null == currentActivityStage) {
			return;
		}
		long activityId = currentActivityStage.getActivityId();

		Date startDate = currentActivityStage.getStartDate();
		Date endDate = currentActivityStage.getEndDate();
		String tokenContract = currentActivityStage.getTokenContract();
		String tokenName = currentActivityStage.getTokenName();
		long needBuyEos = currentActivityStage.getNeedBuyEos();

		Date expirationStartDate = new Date(startDate.getTime() - 30 * 1000); // 减去30秒
		Date expirationEndDate = new Date(endDate.getTime() - 30 * 1000); // 减去30秒

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String expirationStartDateStr = sdf.format(expirationStartDate);
		String expirationEndDateStr = sdf.format(expirationEndDate);

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("actions.data.token_contract").is(tokenContract),
				Criteria.where("actions.name").is("buytoken"),
				Criteria.where("actions.data.token_symbol").regex(".*" + tokenName),
				Criteria.where("expiration").gte(expirationStartDateStr),
				Criteria.where("expiration").lte(expirationEndDateStr), Criteria.where("accepted").is(true));

		Query query = new Query(criteria);
		query.with(new Sort(new Order(Direction.ASC, "expiration")));
		
		mongoTemplate.count(query, "transactions");

		List<BasicDBObject> buyTransactionsList = mongoTemplate.find(query, BasicDBObject.class, "transactions");
		
		
		for (BasicDBObject thisBuyTransactions : buyTransactionsList) {
			JSONObject thisBuyTransactionsJO = JSONObject.parseObject(JSONObject.toJSONString(thisBuyTransactions));
			ActivityStageUser activityStageUser = new ActivityStageUser();
			JSONObject data = thisBuyTransactionsJO.getJSONArray("actions").getJSONObject(0).getJSONObject("data");
			String accountName = data.getString("payer");
			String eos_quant = data.getString("eos_quant");
			String[] eos_quant_array = eos_quant.split(" ");
			BigDecimal eos_qty = new BigDecimal(eos_quant_array[0].trim());
			if (eos_qty.compareTo(BigDecimal.valueOf(needBuyEos)) < 0) {
				continue;
			}

			ActivityStageUserExample activityStageUserExample = new ActivityStageUserExample();

			ActivityStageUserExample.Criteria activityStageUserCriteria = activityStageUserExample.createCriteria();
			activityStageUserCriteria.andActivityIdEqualTo(activityId);
			activityStageUserCriteria.andActivityStageIdEqualTo(currentActivityStage.getId());
			activityStageUserCriteria.andAccountNameEqualTo(accountName);
			ActivityStageUserExample.Criteria activityStageUserCriteriaOr = activityStageUserExample.or();
			activityStageUserCriteriaOr.andTrxIdEqualTo(thisBuyTransactionsJO.getString("trx_id"));

			long count = activityStageUserMapper.countByExample(activityStageUserExample);
			if (count > 0) {
				continue;
			}

			activityStageUser.setActivityId(activityId);
			activityStageUser.setActivityStageId(currentActivityStage.getId());
			activityStageUser.setIsWinner("n");
			activityStageUser.setIsLucky("n");
			activityStageUser.setAccountName(accountName);
			activityStageUser.setTrxId(thisBuyTransactionsJO.getString("trx_id"));
			activityStageUser.setTradeDate(thisBuyTransactionsJO.getDate("expiration"));
			activityStageUser.setTradeQty(eos_qty);
			activityStageUser.setCreateDate(nowDate);
			activityStageUser.setUpdateDate(nowDate);

			activityStageUserMapper.insertSelective(activityStageUser);
		}
	}

	@Override
	public void getAllBuyUsers() {
		List<ActivityInfo> activityInfoDoingList = this.getDoingActivityInfoList();
		for (ActivityInfo thisActivityInfo : activityInfoDoingList) {
			List<ActivityStage> currentActivityStages = this.getNeedGetUserActivityStage(thisActivityInfo.getId());
			for (ActivityStage currentActivityStage : currentActivityStages) {
				this.getCurrentBuyUsers(currentActivityStage);
			}
		}
	}

	@Override
	public void updateStatus() {
		Date nowDate = new Date();

		// 根据时间修改活动为完成状态
		ActivityInfoExample example = new ActivityInfoExample();
		ActivityInfoExample.Criteria criteria = example.createCriteria();
		criteria.andEndDateLessThan(nowDate);
		criteria.andStatusNotEqualTo("completed");
		ActivityInfo expireUpdate = new ActivityInfo();
		expireUpdate.setStatus("completed");
		activityInfoMapper.updateByExampleSelective(expireUpdate, example);

		// 修改活动阶段状态为完成
		ActivityStageExample completedStageExample = new ActivityStageExample();
		ActivityStageExample.Criteria completedStageCriteria = completedStageExample.createCriteria();
		completedStageCriteria.andEndDateLessThan(nowDate);
		completedStageCriteria.andStatusNotEqualTo("completed");
		ActivityStage completedActivityStage = new ActivityStage();
		completedActivityStage.setStatus("completed");
		activityStageMapper.updateByExampleSelective(completedActivityStage, completedStageExample);

		// 根据时间修改为活动进行中
		ActivityInfoExample exampleDoing = new ActivityInfoExample();
		ActivityInfoExample.Criteria criteriaDoing = exampleDoing.createCriteria();
		criteriaDoing.andStartDateLessThanOrEqualTo(nowDate);
		criteriaDoing.andEndDateGreaterThanOrEqualTo(nowDate);
		criteriaDoing.andStatusNotEqualTo("doing");
		ActivityInfo doingUpdate = new ActivityInfo();
		doingUpdate.setStatus("doing");
		activityInfoMapper.updateByExampleSelective(doingUpdate, exampleDoing);

		// 根据时间修改活动阶段状态为正在进行
		ActivityStageExample doingStageExample = new ActivityStageExample();
		ActivityStageExample.Criteria doingStageCriteria = doingStageExample.createCriteria();
		doingStageCriteria.andStartDateLessThanOrEqualTo(nowDate);
		doingStageCriteria.andEndDateGreaterThanOrEqualTo(nowDate);
		doingStageCriteria.andStatusNotEqualTo("doing");
		ActivityStage doingActivityStage = new ActivityStage();
		doingActivityStage.setStatus("doing");
		activityStageMapper.updateByExampleSelective(doingActivityStage, doingStageExample);
	}

	@Override
	public ActivityInfo findById(long id) {
		ActivityInfo result = activityInfoMapper.selectByPrimaryKey(id);
		return result;
	}

	@Override
	public List<ActivityStage> findAllActivityStageByActivityId(long activityId) {
		ActivityStageExample example = new ActivityStageExample();
		ActivityStageExample.Criteria astCriteria = example.createCriteria();
		astCriteria.andActivityIdEqualTo(activityId);
		example.setOrderByClause("seq asc");
		List<ActivityStage> activityStages = activityStageMapper.selectByExample(example);
		return activityStages;
	}

	@Override
	public List<ActivityStageUser> findAllWinUserByActivityIdAndStageId(long activityStageId, String accountName) {
		ActivityStageUserExample example = new ActivityStageUserExample();
		ActivityStageUserExample.Criteria criteria = example.createCriteria();
		criteria.andActivityStageIdEqualTo(activityStageId);
		criteria.andIsWinnerEqualTo("y");

		ActivityStageUserExample.Criteria criteriaOr = example.or();
		criteriaOr.andActivityStageIdEqualTo(activityStageId);
		criteriaOr.andIsLuckyEqualTo("y");

		if (null != accountName && !accountName.isEmpty()) {
			criteria.andAccountNameEqualTo(accountName);
			criteriaOr.andAccountNameEqualTo(accountName);
		}

		List<ActivityStageUser> activityStageUsers = activityStageUserMapper.selectByExample(example);
		return activityStageUsers;
	}

	@Override
	public List<ActivityStageUser> findAllUserByActivityIdAndStageId(long activityStageId, String accountName) {
		ActivityStageUserExample example = new ActivityStageUserExample();
		ActivityStageUserExample.Criteria criteria = example.createCriteria();
		criteria.andActivityStageIdEqualTo(activityStageId);

		if (null != accountName && !accountName.isEmpty()) {
			criteria.andAccountNameEqualTo(accountName);
		}

		List<ActivityStageUser> activityStageUsers = activityStageUserMapper.selectByExample(example);
		return activityStageUsers;
	}

	@Override
	public synchronized void transfer2WinAndLuckyUser() {
		Date nowDate = new Date();
		Date endDate = new Date(nowDate.getTime() - 2 * 60 * 60 * 1000); // 两小时后发币
		ActivityStageExample activityStageExample = new ActivityStageExample();
		ActivityStageExample.Criteria activityStageCriteria = activityStageExample.createCriteria();
		activityStageCriteria.andStatusEqualTo("completed");
		activityStageCriteria.andIsPaidEqualTo("n");
//		activityStageCriteria.andEndDateLessThanOrEqualTo(endDate);
		List<ActivityStage> payActivityStageList = activityStageMapper.selectByExample(activityStageExample);
		for (ActivityStage thisActivityStage : payActivityStageList) {
			ActivityStage updateActivityStage = new ActivityStage();
			updateActivityStage.setId(thisActivityStage.getId());
			updateActivityStage.setIsPaid("y");
			activityStageMapper.updateByPrimaryKeySelective(updateActivityStage);

			// 最后一次获取交易用户，确保拿全数据
			this.getCurrentBuyUsers(thisActivityStage);

			// 找出优胜奖人员
			this.calculateWinner(thisActivityStage);

			// 挑选幸运者
			this.selectLuckyUser(thisActivityStage.getId());

			ActivityStageUserExample example = new ActivityStageUserExample();
			ActivityStageUserExample.Criteria criteria = example.createCriteria();
			criteria.andActivityStageIdEqualTo(thisActivityStage.getId());
			criteria.andStatusEqualTo("new");
			criteria.andIsWinnerEqualTo("y");

			ActivityStageUserExample.Criteria criteriaOr = example.or();
			criteriaOr.andActivityStageIdEqualTo(thisActivityStage.getId());
			criteriaOr.andStatusEqualTo("new");
			criteriaOr.andIsLuckyEqualTo("y");

			long countN = activityStageUserMapper.countByExample(example);
			if (countN == 0) {
				continue;
			}
			List<ActivityStageUser> activityStageUsers = activityStageUserMapper.selectByExample(example);
			for (ActivityStageUser thisActivityStageUser : activityStageUsers) {
				this.transfer2WinAndLuckyUserPerOne(thisActivityStageUser);
			}
		}
	}

	public void calculateWinner(ActivityStage activityStage) {
		Date nowDate = new Date();
		Page<ActivityStageUser> pageResult = PageHelper.startPage(1, activityStage.getCommonCount().intValue());
		ActivityStageUserExample example = new ActivityStageUserExample();
		ActivityStageUserExample.Criteria criteria = example.createCriteria();
		criteria.andActivityStageIdEqualTo(activityStage.getId());
		criteria.andStatusEqualTo("new");
		example.setOrderByClause("trade_date asc,id asc");
		activityStageUserMapper.selectByExample(example);
		List<ActivityStageUser> result = pageResult.getResult();
		for (ActivityStageUser thisActivityStageUser : result) {
			ActivityStageUser updateActivityStageUser = new ActivityStageUser();
			updateActivityStageUser.setId(thisActivityStageUser.getId());
			updateActivityStageUser.setIsWinner("y");
			updateActivityStageUser.setUpdateDate(nowDate);
			activityStageUserMapper.updateByPrimaryKeySelective(updateActivityStageUser);
		}
	}

	public void selectLuckyUser(long activityStageId) {
		Date nowDate = new Date();
		Page<ActivityStageUser> pageResult = PageHelper.startPage(1, 3);
		ActivityStageUserExample example = new ActivityStageUserExample();
		ActivityStageUserExample.Criteria criteria = example.createCriteria();
		criteria.andActivityStageIdEqualTo(activityStageId);
		criteria.andStatusEqualTo("new");
		example.setOrderByClause("RAND()");
		activityStageUserMapper.selectByExample(example);

		List<ActivityStageUser> result = pageResult.getResult();
		for (ActivityStageUser thisActivityStageUser : result) {
			ActivityStageUser updateActivityStageUser = new ActivityStageUser();
			updateActivityStageUser.setId(thisActivityStageUser.getId());
			updateActivityStageUser.setIsLucky("y");
			updateActivityStageUser.setUpdateDate(nowDate);
			activityStageUserMapper.updateByPrimaryKeySelective(updateActivityStageUser);
		}

	}

	public void transfer2WinAndLuckyUserPerOne(ActivityStageUser thisActivityStageUser) {
		Date nowDate = new Date();
		ActivityStage activityStage = activityStageMapper
				.selectByPrimaryKey(thisActivityStageUser.getActivityStageId());
		if (!activityStage.getStatus().equalsIgnoreCase("completed")) {
			return;
		}

		if (thisActivityStageUser.getIsWinner().equalsIgnoreCase("y")) {
			int maxQty = activityStage.getCommonMaxQty().intValue();
			int minQty = activityStage.getCommonMinQty().intValue();

			Random rand = new Random();
			int quantity = rand.nextInt(maxQty - minQty + 1) + minQty;
			String accountName = thisActivityStageUser.getAccountName();
			String memo = "ET交易所优胜奖励已经到账啦~交易所地址：http://etdac.io/  还有超级大奖等你来拿";

			ActivityStageUser updateActivityStageUser = new ActivityStageUser();
			updateActivityStageUser.setId(thisActivityStageUser.getId());
			updateActivityStageUser.setStatus("completed");
			updateActivityStageUser.setWinQty(BigDecimal.valueOf(quantity));
			updateActivityStageUser.setUpdateDate(nowDate);
			activityStageUserMapper.updateByPrimaryKeySelective(updateActivityStageUser);

			try {
				this.transfer(accountName, activityStage.getTokenContract(),
						quantity + " " + activityStage.getTokenName(), activityStage.getPrecisionNumber(), memo);
			} catch (Exception e) {

			}
		}

		if (thisActivityStageUser.getIsLucky().equalsIgnoreCase("y")) {
			int luckyCoinQty = activityStage.getLuckyCoinQty().intValue();
			int luckyCount = activityStage.getLuckyCount().intValue();

			String luckyMethod = activityStage.getLuckyMethod();
			if (luckyMethod.equalsIgnoreCase("share")) {
				BigDecimal quantity = BigDecimal.valueOf(luckyCoinQty).divide(BigDecimal.valueOf(luckyCount), 2,
						BigDecimal.ROUND_HALF_UP);
				String accountName = thisActivityStageUser.getAccountName();
				String memo = "恭喜您，被ET交易所幸运大奖砸中啦~还有更多精彩活动，尽在 http://etdac.io/ ";

				ActivityStageUser updateActivityStageUser = new ActivityStageUser();
				updateActivityStageUser.setId(thisActivityStageUser.getId());
				updateActivityStageUser.setStatus("completed");
				updateActivityStageUser.setLuckyQty(quantity);
				updateActivityStageUser.setUpdateDate(nowDate);
				activityStageUserMapper.updateByPrimaryKeySelective(updateActivityStageUser);

				try {
					this.transfer(accountName, activityStage.getTokenContract(),
							quantity + " " + activityStage.getTokenName(), activityStage.getPrecisionNumber(),
							memo);
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * 转账
	 * 
	 * @param accountName
	 * @param contractAccount
	 * @param quantity
	 * @param memo
	 * @return
	 */
	public String transfer(String accountName, String contractAccount, String quantity, int precisionNumber,
			String memo) {
		String[] quantityArray = quantity.split(" ");

		BigDecimal qty = new BigDecimal(quantityArray[0].trim());
		qty = qty.setScale(precisionNumber, BigDecimal.ROUND_HALF_DOWN);
		String qtyStr = qty + " " + quantityArray[1].toString();

		String url = EOS_SERVER_API + "transfer";
		JSONObject jo = new JSONObject();
		jo.put("to", accountName);
		jo.put("contractAccount", contractAccount);
		jo.put("quantity", qtyStr);
		jo.put("memo", memo);

		try {
			String result = HttpClientUtils.doPostJson(url, jo.toJSONString());
			System.out.println("url================= : " + url);
			System.out.println("json data: " + jo.toJSONString());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
