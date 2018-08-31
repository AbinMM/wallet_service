package it.etoken.component.eosblock.service;

import java.util.List;

import com.mongodb.BasicDBObject;

public interface EOSActionService {
	public void save(BasicDBObject eOSAction);

	public void remove(String id);

	public void update(String id, String key, String value);

	public List<BasicDBObject> list4Page(int page, int pageSize);
}
