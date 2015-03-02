package net.popbean.pf.rule.vo;

import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 
 * @author to0ld
 *
 */
public class RuleDetail extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3461489074210806393L;
	//
	public String pk_rule_deail;
	public String pk_cond;
	public String cond_exp;
	public String exec_exp;
	public Double inum;
	public String memo;
}
