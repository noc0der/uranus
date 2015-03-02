package net.popbean.pf.security.vo;

import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 功能权限数据结构描述
 * 为了省事，直接用folder了
 * @author to0ld
 */
@Entity(code="pb_bd_perm",name="功能权限注册表")
public class PermVO extends AbstractValueObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7435167071250932466L;
	public String pk_perm;
	public String code_perm;
	public String name_perm;
	public Integer i_type;//0:app;3:folder;5:node;7:action
	public String code_badge;//BADGE键值
	public Integer i_num;//序号
	public String memo_uri;//资源指向
	public String memo_perm;
	public String ref_app;//
	public Integer i_def;//默认节点
	public String icon;//图标
	public String ref_perm_folder;//所属目录主键
	//
}
