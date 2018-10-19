package it.etoken.component.user.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.etoken.base.model.user.entity.Sysconf;
import it.etoken.base.model.user.entity.SysconfExample;
import it.etoken.component.user.dao.mapper.SysconfMapper;
import it.etoken.component.user.service.SysconfService;

@Component
@Transactional
public class SysconfServiceImpl implements SysconfService {
	private final static Logger logger = LoggerFactory.getLogger(SysconfServiceImpl.class);
	@Autowired
	SysconfMapper sysconfMapper;

	@Override
	public List<Sysconf> findAll() {
		SysconfExample example = new SysconfExample();
		List<Sysconf> list = sysconfMapper.selectByExample(example);
		return list;
	}

	@Override
	public int updateByName(Sysconf sysconf) {
		String name = sysconf.getName();
		SysconfExample example = new SysconfExample();
		SysconfExample.Criteria criteria = example.createCriteria();
		criteria.andNameEqualTo(name);
		int result = sysconfMapper.updateByExampleSelective(sysconf, example);
		return result;
	}
}
