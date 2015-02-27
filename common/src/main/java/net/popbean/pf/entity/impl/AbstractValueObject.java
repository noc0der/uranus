package net.popbean.pf.entity.impl;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Field;

public class AbstractValueObject implements IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3852105815084274103L;
	
	@Field(name="创建者",domain=Domain.Ref)
	public String pk_account_crt;
	@Field(name="所属企业",domain=Domain.Ref)
	public String pk_company_crt;
	@Field(name="创建者",domain=Domain.Stat,range="0:编辑@3:处理中@5:已完成")
	public Integer i_stat;
}
