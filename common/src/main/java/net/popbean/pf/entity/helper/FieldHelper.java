package net.popbean.pf.entity.helper;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.model.FieldModel;

public class FieldHelper {
	/**
	 * 
	 * @param code
	 * @param name
	 * @return
	 */
	public static FieldModel pk(String code, String name) {
		FieldModel ret = new FieldModel();
		ret.code = code;
		ret.name = name;
		ret.ispk = true;
		return ret;
	}
	public static FieldModel ref(String code, String name) {
		FieldModel ret = new FieldModel();
		ret.code = code;
		ret.name = name;
		ret.type = Domain.Ref;
		return ret;
	}
	public static FieldModel stat(String code, String name) {
		FieldModel ret = new FieldModel();
		ret.code = code;
		ret.name = name;
		ret.type = Domain.Stat;
		return ret;
	}
	public static FieldModel memo(String code, String name) {
		FieldModel ret = new FieldModel();
		ret.code = code;
		ret.name = name;
		ret.type = Domain.Memo;
		return ret;
	}
}
