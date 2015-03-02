package net.popbean.pf.rule.vo;

import java.util.List;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.impl.AbstractValueObject;

/**
 * 规则的数据模型
 * 
 * @author to0ld
 *
 */
@Entity(code="pb_pf_rule",name="业务规则")
public class RuleModel extends AbstractValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7887680396883210204L;
	@Field(domain=Domain.PK)
	String pk_rule ;
	@Field(domain=Domain.Code)
	String code_rule;
	@Field(domain=Domain.Memo)
	String memo_rule;
	
	//
	List<RuleDetail> details;
	List<RuleParam> params;
}
