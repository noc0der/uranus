package net.popbean.pf.security.vo;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 组织机构
 * @author to0ld
 *
 */
@Entity(code="pb_bd_org",name="组织信息")
public class OrgVO extends AbstractValueObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9198231455239532834L;
	//
	public String pk_org;
	@Field(code="ref_org_cate",name="部门类型",domain=Domain.Ref)
	public String ref_org_cate;
	@Field(code="ref_company",name="所属企业",domain=Domain.Ref)
	public String ref_company;
	@Field(code="ref_account_owenr",name="组织负责人",domain=Domain.Ref)
	public String ref_account_owner;
	public String code_org;
	public String name_org;
	public String memo_org;
	public Integer i_deep;//节点深度
}
