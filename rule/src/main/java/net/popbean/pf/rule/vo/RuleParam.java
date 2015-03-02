package net.popbean.pf.rule.vo;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 规则参数
 * @author to0ld
 *
 */
public class RuleParam extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7093324741650749171L;
	//
	@Field(domain=Domain.PK)
	public String pk_rule_param;//建议采用company:code_rule:code_param 
}
