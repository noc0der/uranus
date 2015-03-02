package net.popbean.pf.rule.service;

import java.util.List;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author to0ld
 *
 */
public interface RuleBusinessService {
	/**
	 * 
	 * @param rule_code
	 * @param context
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	public JSONObject eval(String rule_code, JSONObject context, SecuritySession client) throws BusinessError;
	/**
	 * 
	 * @param rule_code
	 * @param ctx_list
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	public JSONObject eval(String rule_code, List<JSONObject> ctx_list, SecuritySession client) throws BusinessError;
}
