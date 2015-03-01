package net.popbean.pf.entity.model;

import java.io.Serializable;
import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.model.helper.EntityModelHelper;

/**
 * 
 * 用于描述实体
 * 
 * @author to0ld
 *
 */
@Entity(code="pb_pf_entity")
public class EntityModel implements Serializable, Cloneable,IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4365726099154277092L;
	@Field(domain=Domain.PK)
	public String pk_entity;
	@Field(domain=Domain.Code)
	public String code;//FIXME 对于分表的情况而言，还是要处理一下的(暂时不支持分表的情况吧)
	@Field(domain=Domain.Code)
	public String name;
	@Field(domain=Domain.Memo)
	public String memo;
	public EntityType type = EntityType.Normal;
	//
	public String clazz;//其实我不想留这个字段，没有它用code一样可以，因为表名是唯一的
	//
	public List<FieldModel> field_list;
	//FIXME 有无必要去映射到db中?
	/**
	 * 
	 * @return
	 */
	public FieldModel findPK(){
		return EntityModelHelper.findPK(this.field_list);
	}
}
