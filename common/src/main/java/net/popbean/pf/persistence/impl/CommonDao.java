package net.popbean.pf.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import net.popbean.pf.id.service.IDGenService;
import net.popbean.pf.persistence.service.IDBConnectionProviderService;
/**
 * 
 * @author to0ld
 *
 */
@Repository("dao/pf/common")
public class CommonDao extends BaseDao<String> {
	/**
	 * 
	 */
	@Override
	@Autowired
	@Qualifier("service/pf/dbprovider")
	public void setDbProvider(IDBConnectionProviderService value) {
		_dbConnProviderService = value;
	}

	@Override
	@Autowired
	@Qualifier("service/pf/id/uuid")
	public void setIdGen(IDGenService<String> value) {
		_idGen = value;
	}
}
