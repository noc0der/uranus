package net.popbean.pf.log.op.service;

import com.alibaba.fastjson.JSONObject;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.security.vo.SecuritySession;
/**
 * 业务日志
 * @author to0ld
 *
 */
public interface OpLogService {
	/**
	 * 
	 * @param op_cate
	 * @param jo
	 * @param session
	 * @throws BusinessError
	 */
	public void log(String op_cate,JSONObject jo,SecuritySession session)throws BusinessError;
}
