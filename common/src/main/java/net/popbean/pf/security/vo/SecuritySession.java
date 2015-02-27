package net.popbean.pf.security.vo;

import java.io.Serializable;
/**
 * 用于记录用户登陆的信息
 * @author to0ld
 *
 */
public class SecuritySession implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5920848722158597456L;
	//
	public String account_code;//code_account vs account_code
	public String account_name;
	public String account_id;
	public String ip;//登陆ip
	//
	public CompanyVO company;
}
