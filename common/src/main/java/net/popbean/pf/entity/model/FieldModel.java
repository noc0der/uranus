package net.popbean.pf.entity.model;

import java.io.Serializable;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.RelationType;
/**
 * 
 * @author to0ld
 *
 */
public class FieldModel implements Serializable, Cloneable,IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6539336547071774284L;
	//
	public String pk_entity;
	public String pk_field;
	public Domain type;
	public String code;
	public String name;
	public boolean required;
	public boolean ispk;
	public int length;
	public int precision;
	public String defaultValue;
	//
	public String code_relation_entity;//引用表编码
	public String pk_relation_entity;//引用表唯一标示
	public RelationType rt = RelationType.None;
}
