package it.etoken.component.user.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.etoken.base.common.exception.MLCommonException;
import it.etoken.base.common.exception.MLException;
import it.etoken.base.model.user.entity.EostRecord;
import it.etoken.base.model.user.entity.User;
import it.etoken.component.user.dao.mapper.EostRecordMapper;
import it.etoken.component.user.dao.mapper.UserMapper;
import it.etoken.component.user.service.EostRecordService;

@Component
@Transactional
public class EostRecordServiceImpl implements EostRecordService{

	private final static Logger logger = LoggerFactory.getLogger(EostRecordServiceImpl.class);
	
	@Autowired
	private EostRecordMapper eostRecordMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public List<EostRecord> findByUid(String uid) {
		try {
			List<EostRecord> list=eostRecordMapper.findEosRecord(Long.parseLong(uid));
			return list;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
	}

	@Override
	public void saveEostRecord(EostRecord eostRecord) {
		try {
			User user = userMapper.findById(eostRecord.getUid());
			if(user.getEost()==0 || user.getEost()<0) {
				throw new MLException(MLCommonException.NOEOST, "没有奖励可以领取。");
			}
			eostRecordMapper.insertNew(eostRecord);
			userMapper.updateEost(eostRecord.getUid(), 0.0);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new MLException(MLCommonException.system_err);
		}
		
	}
}
