package net.popbean.pf.entity.struct.impl;

import org.apache.commons.lang3.StringUtils;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.struct.EntityStruct;

public class MysqlEntityStructImpl implements EntityStruct {

	@Override
	public String create(EntityModel model)throws Exception {
		String tableCode = model.code;
		String ret = " create table " + tableCode + "(";
		//
		int pos = 0;
		for (FieldModel field : model.field_list) {
			if (pos != 0) {
				ret += ",";
			}
			ret += convertField(field) + "\n";
			pos++;
		}
		// 处理主键
		FieldModel pk = model.findPK();
		if (pk != null && (Domain.PK.equals(pk.type))) {// 如果是临时表，无主键，那就用reffield吧
			ret += " ,constraint PK_" + tableCode + " primary key (" + pk.code + ") ";
		}
		ret += ")";
		return ret;
	}

	/**
	 * @param newField entitymodel中的结构
	 * @param oldField 当前数据库中的结构
	 */
	@Override
	public String alter(String table_code, FieldModel newField, FieldModel oldField) throws Exception {
		String ret = "";
		if (newField == null && oldField == null) {// 都是空，就不玩了
			return null;
		}
		if (newField == null) {// 为drop的场景
			ret += "ALTER TABLE " + table_code + " DROP " + oldField.code + "";
			return ret;
		}
		if (oldField == null) {// add的场景
			ret += "ALTER TABLE " + table_code + " ADD (" + convertField(newField) + ")";
			return ret;
		}
		if (!newField.code.toLowerCase().equals(oldField.code.toLowerCase())) {// 这个估计很难被执行到,因为新老之间没有对应关系
			ret += "ALTER TABLE " + table_code + " CHANGE " + oldField.code + " " + convertField(newField);
			return ret;
		}
		return null;
	}
	@Override
	public String convertField(FieldModel field) throws Exception {
		String ret = field.code + " ";
		if (Domain.Code.equals(field.type) || Domain.Memo.equals(field.type)) {
//			ret += " varchar(" + field.length + ")";
			ret += " varchar(" + Domain.Code.getLength() + ")";
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default '" + field.defaultValue + "'";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		} else if (Domain.PK.equals(field.type)) {
//			ret += " varchar(" + field.length + ")";
			ret += " varchar(" + Domain.PK.getLength() + ")";
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default '" + field.defaultValue + "'";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		}else if(Domain.Ref.equals(field.type)){
			ret += " varchar(" + Domain.Ref.getLength() + ")";
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default '" + field.defaultValue + "'";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		} else if (Domain.Date.equals(field.type)) {
			ret += " date ";
			if (field.required) {
				ret += " not null ";
			}
		} else if (Domain.Money.equals(field.type)) {
			ret += " DECIMAL(" + Domain.Money.getLength() + "," + field.precision + ")";
			
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default " + field.defaultValue + "";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		} else if (Domain.Stat.equals(field.type)) {
			ret += " SMALLINT ";
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default " + field.defaultValue + "";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		} else if (Domain.TimeStamp.equals(field.type)) {
			ret += " TIMESTAMP ";
			if (field.required) {
				ret += " not null ";
			}else{
				ret += "  null ";
			}
			if (field.defaultValue != null) {//FIXME 强行给人无视了？
//				ret += " deault 0 ";//暂时不支持对timestamp设置初始值
			} else {
				if(field.code.equalsIgnoreCase("LM_TS")){//当乐观锁用的好吧
					ret += " default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ";
				}
			}
			
		} else if (Domain.Int.equals(field.type)) {
			ret += " INTEGER ";
			if (!StringUtils.isBlank(field.defaultValue)) {
				ret += " default " + field.defaultValue + "";// 需要考虑的是，如果字符还得单引号
			}
			if (field.required) {
				ret += " not null ";
			}
		}
		return ret;
	}
}
