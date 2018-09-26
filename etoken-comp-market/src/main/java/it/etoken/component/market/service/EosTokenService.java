package it.etoken.component.market.service;

import java.util.List;
import java.util.Map;

import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.market.entity.Coins;
import it.etoken.base.model.market.vo.CoinTicker;

public interface EosTokenService {
	
	
	/**
	 * 查詢币
	 * @param id
	 * @return
	 */
	public List<CoinTicker> getTicker()throws MLException;
	
	/**
	 * 
	 * @return
	 * @throws MLException
	 */
	public Map getLine(String coin,String type)throws MLException;

	public void ticker(Coins coins)throws MLException;
	

}
