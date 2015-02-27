package net.popbean.pf.entity.service;

import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.exception.BusinessError;
/**
 * 实体结构处理
 * @author to0ld
 *
 */

import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;
/**
 * 实体结构处理
 * @author to0ld
 *
 */
public interface EntityStructBusinessService {
	/**
	 * 
	 * @return
	 * @throws BusinessError
	 */
	public List<JSONObject> fetchSchema() throws BusinessError;
	/**
	 * 
	 * @param scheme
	 * @throws BusinessError
	 */
	public void dropSchema(String schema) throws BusinessError;
	/**
	 * 
	 * @param entity_code
	 * @param col_code
	 * @return
	 * @throws Exception
	 */
	public boolean exists(String entity_code, String col_code) throws BusinessError;
	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws BusinessError
	 */
	public List<String> diffDbStructByEntityModel(Class<? extends IValueObject> clazz) throws BusinessError;
	/**
	 * 
	 * @param entity_code
	 * @return
	 * @throws BusinessError
	 */
	public EntityModel buildEntityModelByDbStruct(String entity_code) throws BusinessError;
	/**
	 * 
	 * @param list
	 * @param session
	 * @throws BusinessError
	 */
	public void syncDbStructByEntityModel(List<EntityModel> list, SecuritySession session) throws BusinessError;
	/**
	 * 将entity model更新到数据库中
	 * @param model
	 * @param session
	 * @throws BusinessError
	 */
	public void syncEntityModel(List<EntityModel> model, SecuritySession session) throws BusinessError;
	/**
	 * 将整个体系内的表都建立了
	 * @param clazz
	 * @param session
	 * @throws BusinessError
	 */
	public void syncDbStruct(Class<? extends IValueObject> clazz, SecuritySession session) throws BusinessError;
	/**
	 * 
	 * @param session
	 * @throws BusinessError
	 */
	public void syncDbStruct(SecuritySession session)throws BusinessError;
	/**
	 * 
	 * @param session
	 * @return
	 * @throws BusinessError
	 */
	public List<String> diffDbStructByEntityModel(SecuritySession session) throws BusinessError;
	/**
	 * 
	 * @param scheme
	 * @param table
	 * @param column
	 * @param isForce
	 * @param session
	 * @throws BusinessError
	 */
	public void cleanDbStruct(String scheme, String table, String column, Boolean isForce,SecuritySession session) throws BusinessError;
	/**
	 * 
	 * @param clazz_list
	 * @param isSyncDb
	 * @param client
	 * @throws BusinessError
	 */
	void impByEntityClass(List<String> clazz_list, Boolean isSyncDb, SecuritySession client) throws BusinessError;
	/**
	 * 
	 * @param entity_unique_list
	 * @param client
	 * @throws BusinessError
	@Deprecated
	void syncDbStructByEntityMeta(List<String> entity_unique_list, SecuritySession client) throws BusinessError;
		 */
	/**
	 * 
	 * @param clazz_list
	 * @param client
	 * @throws BusinessError
	 */
	void syncDbStructByClazz(List<String> clazz_list, SecuritySession client) throws BusinessError;
}
