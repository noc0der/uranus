package net.popbean.pf.persistence.service;

import java.sql.Connection;

/**
 * 
 * @author tealc
 *
 */
public interface IDBConnectionProviderService {
	public Connection getConnection() throws Exception;

	public void closeConnection() throws Exception;

	public void closeConnection(Connection conn) throws Exception;
}
