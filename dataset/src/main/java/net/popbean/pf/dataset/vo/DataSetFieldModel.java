package net.popbean.pf.dataset.vo;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.impl.AbstractValueObject;

public class DataSetFieldModel extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2754985279324149578L;
	//
	public String pk_ds;
	public String pk_ds_field;
	public Integer inum;//显示顺序
	public String code_field;//编码
	public String code_vendor;//转化后的编码
	public String name_field;//名称
	public String memo_field;
	public Domain domain;//怎么映射到数据库中啊，头疼
	public Scope scope = Scope.Data;//
	public String rangeset;//值域
	public String def_value;//默认值
	public Integer ireq ;
	public boolean ispk;//是否是唯一标示
}
