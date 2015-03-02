package net.popbean.pf.security.vo;

import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.impl.AbstractValueObject;

/**
 * 应用信息
 * @author to0ld
 *
 */
@Entity(code = "pb_bd_app", name = "应用信息")
public class AppVO extends AbstractValueObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2692355483300031372L;
	public String pk_app;
	public String code_app;
	public String name_app;
	public String memo_app;
	public String ref_company_crt;
	public String code_secret;
}
