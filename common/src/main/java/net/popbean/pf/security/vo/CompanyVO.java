package net.popbean.pf.security.vo;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Field;
/**
 * 多租户的企业信息
 * @author to0ld
 *
 */
public class CompanyVO implements IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4168320084899460389L;
	//
	@Field(name="企业主键",domain=Domain.PK)
	public String company_id;
	@Field(name="企业编码",domain=Domain.Code)
	public String company_code;
	@Field(name="企业名称",domain=Domain.Code)
	public String company_name;
}
