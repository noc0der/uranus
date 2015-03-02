package net.popbean.pf.security.vo;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 角色数据结构描述
 * @author to0ld
 *
 */
@Entity(code="pb_bd_role",name="角色信息")
public class RoleVO extends AbstractValueObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3918195315421352204L;
	@Field(name="角色主键",domain=Domain.PK)
	public String pk_role;
	public String code_role;
	public String name_role;
	public String ref_account_crt;
	public String ref_company_crt;
	public String memo_role;
	public String code_app;
	

}
