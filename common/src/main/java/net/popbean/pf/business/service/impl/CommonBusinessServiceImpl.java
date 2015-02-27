package net.popbean.pf.business.service.impl;

import java.util.List;

import net.popbean.pf.business.service.CommonBusinessService;
import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.security.vo.SecuritySession;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于执行sql语句使用
 * @author to0ld
 *
 */
@Service("service/pf/common")
public class CommonBusinessServiceImpl extends AbstractBusinessService implements CommonBusinessService{
	/**
	 * 
	 */
	@Override
	public List<JSONObject> query(StringBuilder sql, JSONObject param, SecuritySession client) throws BusinessError {
		try {
			List<JSONObject> ret = _commondao.query(sql, param);
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public void executeChange(StringBuilder sql,JSONObject param,SecuritySession client)throws BusinessError{
		try {
			_commondao.executeChange(sql, param);
		} catch (Exception e) {
			processError(e);
		}
	}
	@Override
	public void executeChange(StringBuilder sql,List<JSONObject> param_list,SecuritySession client)throws BusinessError{
		try {
			_commondao.batch(sql, param_list);
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * 批量执行(事务容器)
	 * @param sql_list
	 * @param param_list
	 * @param client
	 * @throws BusinessError
	 */
	@Override
	public void executeChange(List<StringBuilder> sql_list,List<JSONObject> param_list,SecuritySession client)throws BusinessError{
		try {
			for(int i=0,len=sql_list.size();i<len;i++){
				if(!CollectionUtils.isEmpty(param_list)){
					_commondao.executeChange(sql_list.get(i), param_list.get(i));	
				}else{
					_commondao.executeChange(sql_list.get(i), null);
				}
				
			}
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * 将执行结果放在其中返回
	 */
	@Override
	public JSONObject query(List<StringBuilder> sql_list, List<JSONObject> param_list, SecuritySession client) throws BusinessError {
		try {
			JSONObject ret = new JSONObject();
			for(int i=0,len=sql_list.size();i<len;i++){
				List<JSONObject> list = _commondao.query(sql_list.get(i), param_list.get(i));
				ret.put(i+"", list);
			}
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public <T extends IValueObject> T find(StringBuilder sql,JSONObject param,Class<T> clazz,SecuritySession client)throws BusinessError{
		try {
			T ret = _commondao.find(sql, param, clazz, null);
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public JSONObject find(StringBuilder sql,JSONObject param,String tech_msg,SecuritySession client)throws BusinessError{
		try {
			JSONObject ret = _commondao.find(sql, param,tech_msg);
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 
	 */
	@Override
	public void batchInsert(List<IValueObject> list, FieldModel[] guardFields) throws BusinessError {
		try {
			_commondao.batchInsert(list,guardFields);
		} catch (Exception e) {
			processError(e);
		}
	}
	@Override
	public void batchReplace(List<IValueObject> list) throws BusinessError {
		try {
			_commondao.batchReplace(list);
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * @deprecated 没有使用保护
	 */
	@Override
	public String save( IValueObject data, FieldModel[] guardFields) throws BusinessError {
		try {
			return _commondao.save(data);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 
	 */
	@Override
	public List<JSONObject> paging(StringBuilder sql, JSONObject param, Paging paging, SecuritySession client) throws BusinessError {
		try {
			return _commondao.paging(sql, param, paging);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public void batchUpdate(List<IValueObject> list) throws BusinessError {
		try {
			_commondao.batchUpdate(list);
		} catch (Exception e) {
			processError(e);
		}		
	}
	@Override
	public void executeChange(StringBuilder sql, SecuritySession client) throws BusinessError {
		try {
			_commondao.executeChange(sql);
		} catch (Exception e) {
			processError(e);
		}
	}
}