package net.popbean.pf.security.service;

import net.popbean.pf.exception.BusinessError;

/**
 * 用于接入各种鉴权实现
 * 
 * @author to0ld
 *
 */
public interface CustomAuthenticationBusinessService {
	/**
	 * 
	 * @param acc_code
	 * @param acc_pwd
	 * @throws BuzException
	 */
	void login(String acc_code, String acc_pwd) throws BusinessError;
}
