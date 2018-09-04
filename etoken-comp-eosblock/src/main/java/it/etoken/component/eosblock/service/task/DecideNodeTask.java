package it.etoken.component.eosblock.service.task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.common.utils.HttpClientUtils;
import it.etoken.base.model.eosblock.entity.EosNode;
import it.etoken.cache.service.CacheService;
import it.etoken.component.eosblock.service.EosNodeService;

@Component
public class DecideNodeTask {

	@Autowired
	EosNodeService eosNodeService;
	
	@Autowired
	CacheService cacheService;

	// 获取当前内存价格表的数据
	@Scheduled(cron = "0 */10 * * * ?")
	public void getRamPrice() {
		try {
			System.out.println("开始获取节点信息...");
			List<EosNode> eosNodeList = eosNodeService.findAll();
			List<JSONObject> infoList = new ArrayList<JSONObject>();
			for(EosNode thisEosNode : eosNodeList) {
				try {
					String resultStr = HttpClientUtils.doPostJson(thisEosNode.getUrl() + "/v1/chain/get_info", "");
					if (null == resultStr || resultStr.isEmpty()) {
						continue;
					}
					
					JSONObject info = JSONObject.parseObject(resultStr);
					info.put("url", thisEosNode.getUrl());
					infoList.add(info);
				}catch(Exception ee) {
					
				}
				
			}
			Collections.sort(infoList, new Comparator<JSONObject>(){
	            public int compare(JSONObject p1, JSONObject p2) {
	            	BigDecimal x1 = p1.getBigDecimal("head_block_num");
	            	BigDecimal x2 = p2.getBigDecimal("head_block_num");
	                if(x1.compareTo(x2) < 0){
	                    return 1;
	                }
	                if(x1.compareTo(x2) == 0){
	                    return 0;
	                }
	                return -1;
	            }
	        });
			
			cacheService.set("node_list", infoList);
			System.out.println(infoList);
			System.out.println("节点信息仲裁完成");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MLException(MLCommonException.system_err);
		}

	}
}
