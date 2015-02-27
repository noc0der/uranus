package net.popbean.pf.entity.struct;

import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;

/**
 * 用于满足不同的db之间的转化
 * 
 * @author to0ld
 *
 */
public interface EntityStruct {

	public String create(EntityModel model)throws Exception;

	public String alter(String table_code, FieldModel newField, FieldModel oldField) throws Exception;

	public String convertField(FieldModel field) throws Exception;
}
