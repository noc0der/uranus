package net.popbean.pf.dataset.vo;

import java.math.BigDecimal;

import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 
 * @author to0ld
 *
 */
public class EnumRangeVO extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8328762475814960817L;
	public String pk_ds;//ref->pk_ds:pk_ds_name
	public String pk_ds_range;
	public String code_range;
	public String name_range;
	public String code_value;
	public BigDecimal money_value;
	public BigDecimal money_value_max;
	public BigDecimal money_value_min;
	public String memo_range;
}
