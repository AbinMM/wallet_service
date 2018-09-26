package it.etoken.component.market.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.base.model.market.vo.CoinTicker;
import it.etoken.component.market.service.BigOneService;
import it.etoken.component.market.service.CoinsService;
import it.etoken.component.market.service.EosTokenService;
import it.etoken.component.market.service.GateioService;
import it.etoken.component.market.service.MarketService;
import it.etoken.component.market.service.OkexService;

@Component
public class MarketServiceImpl implements MarketService{
	
	@Autowired
	BigOneService bigOneService;
	
	@Autowired
	OkexService okexService;
	
	@Autowired
	GateioService gateioService;
	
	@Autowired
	EosTokenService eosTokenService;
	
	@Autowired
	CoinsService coinsService;
	
	@Value("${gate.exchange}")
	String gateExchange;
	
	@Value("${okex.exchange}")
	String okexExchange;
	
	@Value("${bigone.exchange}")
	String bigOneExchange;
	
	@Value("${eostoken.exchange}")
	String eosTokenExchange;
	

	

	@Override
	public void ticker(Coins coins) throws MLException {
		Coins coin=coinsService.findByName(coins.getName());
		if(coin.getExchange().equals(gateExchange)) {
			  gateioService.ticker(coins);
		}else if(coin.getExchange().equals(eosTokenExchange)) {
			  eosTokenService.ticker(coins);
		}else if(coin.getExchange().equals(bigOneExchange)) {
			  bigOneService.ticker(coins);
		}else if(coin.getExchange().equals(okexExchange)) {
			  okexService.ticker(coins);
		}
	}

	@Override
	public void exchange() throws MLException {
		gateioService.exchange();
//		bigOneService.exchange();
//		okexService.exchange();       	
	}

	@Override
	public List<CoinTicker> getTicker() throws MLException {
		List<CoinTicker> ticker=new ArrayList<CoinTicker>();
		List<CoinTicker> eostokenList=eosTokenService.getTicker();
		if(null!=eostokenList) {
			ticker.addAll(eostokenList);
		}
		List<CoinTicker> gateioList=gateioService.getTicker();
		if(null!=gateioList) {
			ticker.addAll(gateioList);
		}
		List<CoinTicker> bigOneList=bigOneService.getTicker();
		if(null!=bigOneList) {
			ticker.addAll(bigOneList);
		}
		List<CoinTicker> okexList=okexService.getTicker();
		if(null!=okexList) {
			ticker.addAll(okexList);
		}
		return ticker;
	}

	@Override
	public Map getLine(String coin, String type) throws MLException {
		Coins coins=coinsService.findByName(coin.toUpperCase());
		if(coins.getExchange().equals(gateExchange)) {
			  Map date=	gateioService.getLine(coin, type);
			  return date;
		}else if(coins.getExchange().equals(eosTokenExchange)) {
			  Map date= eosTokenService.getLine(coin, type);
			  return date;
		}else if(coins.getExchange().equals(bigOneExchange)) {
			  Map date= bigOneService.getLine(coin, type);
			  return date;  
		}else if(coins.getExchange().equals(okexExchange)) {
			  Map date= okexService.getLine(coin, type);
			  return date;
		}else {
			return null;
		}
	}

	@Override
	public void morningPrice(Coins c) throws MLException {
		gateioService.morningPrice(c);
		okexService.morningPrice(c);
		bigOneService.morningPrice(c);
	}

}
