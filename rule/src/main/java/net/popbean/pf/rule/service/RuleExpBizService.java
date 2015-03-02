package net.popbean.pf.rule.service;

import java.util.List;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

public interface RuleExpBizService {
	/**
	 * 
	 * @param ctx
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	public boolean evalBool(JSONObject ctx, SecuritySession client) throws BusinessError;

	public JSONObject evalObj(JSONObject ctx, SecuritySession client) throws BusinessError;

	public List<JSONObject> evalList(JSONObject ctx, SecuritySession client) throws BusinessError;
}
