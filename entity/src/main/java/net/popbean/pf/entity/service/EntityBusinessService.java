package net.popbean.pf.entity.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.exception.BusinessError;

/**
 * 实体服务
 * @author to0ld
 *
 */
public interface EntityBusinessService<V> {
	/**
	 * 依据实体编码获得entity model
	 * @param entity_code
	 * @return
	 * @throws BusinessError
	 */
	public EntityModel findModel(String entity_code)throws BusinessError;
	/**
	 * 
	 * @param vo
	 * @param withChild
	 * @return
	 * @throws BusinessError
	 */
	public <T extends IValueObject> V saveData(T vo,boolean withChild)throws BusinessError;
	/**
	 * 
	 * @param vo
	 * @return
	 * @throws BusinessError
	 */
	public <T extends IValueObject> int deleteData(T vo,boolean withChild)throws BusinessError;
	/**
	 * 
	 * @param vo
	 * @param withChild 是否带有子集
	 * @return
	 * @throws BusinessError
	 */
	public <T extends IValueObject> T findData(T vo,boolean withChild)throws BusinessError;
	/**
	 * 适用于单表查询的情况
	 * @param condition
	 * @param clazz
	 * @return
	 * @throws BusinessError
	 */
	public <T extends IValueObject> List<T> fetchData(JSONObject condition,Class<T> clazz)throws BusinessError;
}
