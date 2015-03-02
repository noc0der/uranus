package net.popbean.pf.security.service;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.security.vo.SecuritySession;

public interface AuthenticateBusinessService {
	/**
	 * 
	 * @param account_code
	 * @param account_pwd
	 * @return
	 * @throws BusinessError
	 */
	public SecuritySession buildSession(String account_code) throws BusinessError;
	/**
	 * 
	 * @param account_code
	 * @param account_pwd
	 * @return
	 * @throws BusinessError
	 */
	public boolean auth(String account_code, String account_pwd) throws BusinessError;
}
