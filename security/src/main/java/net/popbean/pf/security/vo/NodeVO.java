package net.popbean.pf.security.vo;

import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.impl.AbstractValueObject;
/**
 * 节点工厂注册的节点
 * @author to0ld
 *
 */
@Entity(code="pb_pf_node",name="节点工厂")
public class NodeVO extends AbstractValueObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1799800618183581104L;
	public String pk_node;
	public String code_node;
	public String name_node;
	public String code_type;//节点类型
	public String code_bill;//单据编码
	public String code_app;//应用标示
	public String memo_node;//备注
	public String memo_script;//控制机脚本
	public String code_ctrl;//控制脚本链接
}
