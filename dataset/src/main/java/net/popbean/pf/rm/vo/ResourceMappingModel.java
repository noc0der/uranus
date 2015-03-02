package net.popbean.pf.rm.vo;

import net.popbean.pf.dataset.vo.DataSetModel;
import net.popbean.pf.entity.impl.AbstractValueObject;

public class ResourceMappingModel extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3607238787796118266L;
	public String pk_rm;
	public String code_rm;
	public String code_relation;//映射存储所在表
	public String memo_rm;//备注
	public String code_app;//所属应用
	public Integer i_type;//0:排除;3:包含
	public String ref_subject;//ref->pk_ds:name
	public String ref_resource;//
	//
	public DataSetModel subject_ds_model;
	public DataSetModel resource_ds_model;
}
