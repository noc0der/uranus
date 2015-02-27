package net.popbean.pf.id.helper;

import org.apache.commons.lang3.StringUtils;

import net.popbean.pf.exception.BusinessError;

public class IdGenHelper {
	public static String genID(String entity_code,String field_code)throws BusinessError{
		//主键格式为：表名.字段名.补零
		StringBuilder ret = new StringBuilder(entity_code);

		if(!StringUtils.isBlank(field_code)){
			ret.append(":").append(field_code);//大家一致认为这个比较合适
		}
		return ret.toString();
	}
}
