package net.popbean.pf.entity.model;

import java.io.Serializable;
import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.helper.EntityModelHelper;

import org.springframework.util.CollectionUtils;

/**
 * 
 * 用于描述实体
 * 
 * @author to0ld
 *
 */
public class EntityModel implements Serializable, Cloneable,IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4365726099154277092L;
	public String pk_entity;
	public String code;//FIXME 对于分表的情况而言，还是要处理一下的(暂时不支持分表的情况吧)
	public String name;
	public String memo;
	public EntityType type = EntityType.Normal;
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
