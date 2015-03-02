package net.popbean.pf.dataset.vo;

import java.util.List;

import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 数据集的数据模型
 * @author to0ld
 *
 */
@Entity(code="pb_pf_ds")
public class DataSetModel extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5816415774814995076L;
	//
	
	public String pk_ds;
	public String code_ds;
	public String name_ds;
	public String code_app;//所属应用
	public SourceType src_type;
	public String exec_exp;
	public String memo_ds;
	public String pk_field;//为了增强虚设不需要存数据库中
	public String show_field;//用于显示
	public List<DataSetFieldModel> field_list;
}
