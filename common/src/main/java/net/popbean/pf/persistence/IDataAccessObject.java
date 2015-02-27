package net.popbean.pf.persistence;

import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.model.FieldModel;

import com.alibaba.fastjson.JSONObject;
/**
 * 感觉data access object这个吧，还是可以按照CRUD分一下的
 * @author to0ld
 *
 * @param <V>
 */
public interface IDataAccessObject<V> {
	/**
	 * 
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public V save(IValueObject vo) throws Exception;
	/**
	 * delete from xx where pk_key=?
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int delete(IValueObject vo) throws Exception;
	/**
	 * 
	 * @param sql
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public List<JSONObject> query(StringBuilder sql, JSONObject vo) throws Exception;
	/**
	 * select * from xx where pk_key=?
	 * @param vo
	 * @param tech_msg
	 * @return
	 * @throws Exception
	 */
	public <T extends IValueObject> T find(T vo,String tech_msg) throws Exception;
	/**
	 * 
	 * @param sql
	 * @param param
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public List<JSONObject> paging(StringBuilder sql, JSONObject param,int pageNo, int pageSize) throws Exception;

	/**
	 * 不光是insert|update|delete语句，只要不返回的应该都可以
	 * @param sql
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public int[] batch(StringBuilder sql, List<JSONObject> list) throws Exception;
	/**
	 * 之所以分开是因为 其中有genID的处理,支持insert|update的处理
	 * @param list
	 * @param guardFields
	 * @return
	 * @throws Exception
	 */
	public <T extends IValueObject> int[] batchInsert(List<T> list,FieldModel[] guardFields) throws Exception;
	/**
	 * 查到数据就是true，没找到就是false 
	 * @param sql
	 * @param vo
	 * @param invalid_msg
	 * @return
	 * @throws Exception
	 */
	public boolean assertTrue(StringBuilder sql, JSONObject vo,String invalid_msg) throws Exception ;
	/**
	 * 
	 * @param sql
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public int executeChange(StringBuilder sql, JSONObject vo) throws Exception;
}