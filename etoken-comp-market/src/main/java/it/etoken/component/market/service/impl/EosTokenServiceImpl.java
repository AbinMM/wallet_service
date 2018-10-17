package it.etoken.component.market.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.http.HttpClientService;
import it.etoken.base.common.result.MLResultObject;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.base.model.market.vo.CoinTicker;
import it.etoken.cache.service.CacheService;
import it.etoken.component.market.service.CoinsService;
import it.etoken.component.market.service.EosTokenService;
import it.etoken.componet.eosblock.facade.ETExchangePriceFacadeAPI;

@Component
public class EosTokenServiceImpl  implements EosTokenService{

	private final static Logger logger = LoggerFactory.getLogger(EosTokenServiceImpl.class);
	
	final String [] lines = new String[]{"300","3600","21600","86400"}; //second
	
	DecimalFormat formatter =new DecimalFormat("#.0000");
	
	DecimalFormat formatter1 =new DecimalFormat("#");
	
	DecimalFormat formatter2 =new DecimalFormat("#.00");
	
	final String server="https://data.gateio.io/api2/1/ticker/";
	
	@Autowired
	HttpClientService httpClientService;
	
	@Autowired
	CacheService cacheService;
	
	@Autowired
	CoinsService coinsService;
	
	@Value("${eostoken.exchange}")
	String eosTokenExchange;
	
	
	@Reference(version = "1.0.0", timeout = 60000, retries = 3)
	ETExchangePriceFacadeAPI eTExchangePriceFacadeAPI;
	

	@Override
	@Async
	public void ticker(Coins coins) throws MLException {
		try{
			String symble = coins.getSymble();
			String[] symbleArray = symble.split("-");
			if(symbleArray.length<2) {
				return ;
			}
			exchangeRate(symbleArray[1]);
			BigDecimal bdRate = cacheService.get("CNY_"+symbleArray[1],BigDecimal.class);
			double rate = bdRate.doubleValue();
			
			BigDecimal bdRateUSD = cacheService.get("USDT_"+symbleArray[1],BigDecimal.class);
			double rateUSD = bdRateUSD.doubleValue();
			String code =cacheService.get("code" +coins.getCode().toUpperCase(),String.class );
			MLResultObject<JSONObject> result=eTExchangePriceFacadeAPI.getTodayKInfo(code);
			JSONObject obj=result.getResult();
			System.out.println(null == obj ? "" : obj.toString());
			Double last_rmb=Double.parseDouble(formatter2.format(obj.getBigDecimal("min").doubleValue()* rate));
			Double last=Double.parseDouble(formatter.format(obj.getBigDecimal("min").doubleValue()* rateUSD));
			//兼容gate.io的格式。
			JSONObject jo=new JSONObject();
			jo.put("quoteVolume", obj.getBigDecimal("volum"));
			jo.put("baseVolume",obj.getBigDecimal("volum").doubleValue());
			jo.put("percentChange", obj.getBigDecimal("increase"));
			jo.put("high24hr", obj.getBigDecimal("max").doubleValue()* rateUSD);
			jo.put("low24hr", obj.getBigDecimal("min").doubleValue()* rateUSD);
			jo.put("last", last);
			jo.put("last_rmb", last_rmb);
			cacheService.set("ticker_"+coins.getCode(),jo);	
			//保存k线
			this.line(coins);
		}catch (Exception e) {
			logger.error("ticker",e);
		}
	}

	
	
	private void exchangeRate(String code) throws MLException  {
		try{
			String result = httpClientService.doGet(server+code+"_USDT");
			JSONObject ro = JSON.parseObject(result);
			BigDecimal price_usd  = ro.getBigDecimal("last");
			
			BigDecimal cnyUsdt= cacheService.get("CNY_USDT", BigDecimal.class);
			BigDecimal price = price_usd.multiply(cnyUsdt);
			
			cacheService.set("USDT_"+code,price_usd);
			cacheService.set("CNY_"+code,price);
		}catch (Exception e) {
			cacheService.set("USDT_EOS",5.39);
			cacheService.set("CNY_EOS",36);
			logger.error("exchange",e);
		}
	}

	public List<CoinTicker> getTicker() throws MLException {
		List<CoinTicker> tikes = new ArrayList<CoinTicker>();
		try {
			Page<Coins> coins = coinsService.findAllBy4MarketByExchange(eosTokenExchange);
			for (Coins c : coins.getResult()) {
				String symble = c.getSymble().toLowerCase();
				String[] symbleArray = symble.split("-");
				if(symbleArray.length<2) {
					continue;
				}
				exchangeRate(symbleArray[1]);
				BigDecimal bdRate = cacheService.get("CNY_"+symbleArray[1],BigDecimal.class);
				double rate = bdRate.doubleValue();
				
				BigDecimal bdRateUSD = cacheService.get("USDT_"+symbleArray[1],BigDecimal.class);
				double rateUSD = bdRateUSD.doubleValue();
				CoinTicker t = new CoinTicker();
				t.setId(c.getId());
				t.setCode(c.getCode());
				t.setImg(c.getImg());
				t.setName(c.getName());	
			    String code =cacheService.get("code" +c.getCode().toUpperCase(),String.class );
				MLResultObject<JSONObject> result=eTExchangePriceFacadeAPI.getTodayKInfo(code);
				JSONObject obj=result.getResult();
				
				if(obj != null) {
					t.setStart(Double.parseDouble(formatter.format(obj.getBigDecimal("open").doubleValue()* rate)));
					t.setMax(Double.parseDouble(formatter.format(obj.getBigDecimal("max").doubleValue()* rate)));
					t.setMin(Double.parseDouble(formatter.format(obj.getBigDecimal("min").doubleValue()* rate)));
					t.setIncrease(Double.parseDouble(formatter2.format(obj.getBigDecimal("increase").doubleValue())));
					t.setTxs(Double.parseDouble(formatter1.format(obj.getBigDecimal("volum").doubleValue())));
					t.setUsd(Double.parseDouble(formatter.format(obj.getBigDecimal("min").doubleValue()* rateUSD)));//美元
					t.setPrice(Double.parseDouble(formatter.format(obj.getBigDecimal("min").doubleValue()* rate)));//成交价
					t.setValue(Double.parseDouble(formatter.format(c.getMarke() * t.getUsd())));//市值
				}
				tikes.add(t);
			}
			return tikes;
		} catch (Exception e) {
			logger.error("getTicker", e);
		}
		return tikes;
	}
	
	

	public Map getLine(String code,String type) throws MLException {
		try{
			Map data = cacheService.get("eostoken_line_"+code.toLowerCase()+"_"+type,Map.class);
			return data;
		}catch (Exception e) {
			logger.error(e.toString());
		}
		return null;
	}

	
	

	@SuppressWarnings("unchecked")
	private void line(Coins coins) throws MLException {
		try{	
			for(int i=0;i<lines.length;i++){
				String type = lines[i];
				Map<String,List> data = new HashMap<>();
				List<Double> txs = new ArrayList<>();
				List<Double> ps = new ArrayList<>();
				List<String> x = new ArrayList<>();
				data.put("txs", txs);//交易量
				data.put("ps", ps);//价格
				data.put("x", x);//时间
				List list = cacheService.get("et_price_second_" + coins.getCode().toUpperCase() + "_" + type, List.class);
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
					txs.add(jo.getDouble("trading_volum"));
					Long xx = jo.getLong("record_date")+8*60*60*1000;
					SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
					x.add(sf.format(xx));
				}
	
				cacheService.set("eostoken_line_"+coins.getCode().toLowerCase().toLowerCase()+"_"+i, data);
			}
			
		}catch (Exception e) {
			logger.error("line",e);
		}
	}
}
