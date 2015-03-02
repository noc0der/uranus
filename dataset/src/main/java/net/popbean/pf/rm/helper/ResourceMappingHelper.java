package net.popbean.pf.rm.helper;

import java.util.ArrayList;
import java.util.List;

import net.popbean.pf.entity.helper.FieldHelper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;


public class ResourceMappingHelper {
	/**
	 * 用于生成授权表
	 * 
	 * @param rlt_md_code
	 * @return
	 */
	public static EntityModel buildRelationEntityModel(String rlt_md_code) {
		EntityModel tm = new EntityModel();
		tm.code = rlt_md_code;
		tm.name = "资源授权表(" + rlt_md_code + ")";
		//
		List<FieldModel> field_list = new ArrayList<>();
		FieldModel pk_field = FieldHelper.pk("pk_rlt_rm", "主键");
		FieldModel ref_subject = FieldHelper.ref("ref_subject","主体参照");
		FieldModel ref_resource = FieldHelper.ref("ref_resource","资源参照");
		FieldModel i_stat = FieldHelper.stat("i_stat","状态");
		FieldModel memo = FieldHelper.memo("memo","备注");
		field_list.add(pk_field);
		field_list.add(ref_subject);
		field_list.add(ref_resource);
		field_list.add(i_stat);
		field_list.add(memo);
		//
		tm.field_list = field_list;
		return tm;
	}
}
