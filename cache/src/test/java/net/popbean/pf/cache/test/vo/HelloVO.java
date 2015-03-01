package net.popbean.pf.cache.test.vo;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
/**
 * 用于验证cache的实体
 * @author to0ld
 *
 */
@Entity(code="pb_hello")
public class HelloVO implements IValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8235894953811866614L;
	@Field(domain=Domain.PK)
	public String pk_hello;
}
