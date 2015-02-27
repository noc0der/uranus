package net.popbean.pf.persistence.service.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import net.popbean.pf.persistence.service.IDBConnectionProviderService;

import org.springframework.jdbc.datasource.DataSourceUtils;

public class DBConnectionProviderServiceImpl implements IDBConnectionProviderService {
	
	protected DataSource _ds;
	public void setDataSource(DataSource dataSource){
	    this._ds = dataSource;
	}
	@Override
	public Connection getConnection() throws Exception {
		return DataSourceUtils.getConnection(_ds);
	}

	@Override
	public void closeConnection() throws Exception {
		closeConnection(getConnection());
	}

	@Override
	public void closeConnection(Connection conn) throws Exception {
		DataSourceUtils.releaseConnection(conn, _ds);
	}

}
