package net.popbean.pf.business.service;

import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于给纯执行的sql的用
 * 
 * @author to0ld
 * 
 */
public interface CommonBusinessService {
	/**
	 * 适用于只执行一个查询语句的
	 * 
	 * @param sql
	 * @param param
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	public List<JSONObject> query(StringBuilder sql, JSONObject param, SecuritySession client) throws BusinessError;

	/**
	 * 
	 * @param sql_list
	 * @param param_list
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	public JSONObject query(List<StringBuilder> sql_list, List<JSONObject> param_list, SecuritySession client) throws BusinessError;

	public List<JSONObject> paging(StringBuilder sql, JSONObject param, Paging paging, SecuritySession client) throws BusinessError;

	public <T extends IValueObject> T find(StringBuilder sql,JSONObject param,Class<T> clazz,SecuritySession client)throws BusinessError;

	public void executeChange(StringBuilder sql, SecuritySession client) throws BusinessError;

	public void executeChange(StringBuilder sql, JSONObject param, SecuritySession client) throws BusinessError;

	public void executeChange(List<StringBuilder> sql_list, List<JSONObject> param_list, SecuritySession client) throws BusinessError;

	public void executeChange(StringBuilder sql, List<JSONObject> param_list, SecuritySession client) throws BusinessError;

	public void batchInsert(List<IValueObject> list, FieldModel[] guardFields) throws BusinessError;

	public String save(IValueObject data, FieldModel[] guardFields) throws BusinessError;

	public void batchUpdate(List<IValueObject> list) throws BusinessError;

	public JSONObject find(StringBuilder sql, JSONObject param, String tech_msg, SecuritySession client) throws BusinessError;

	public void batchReplace(List<IValueObject> list) throws BusinessError;
}
