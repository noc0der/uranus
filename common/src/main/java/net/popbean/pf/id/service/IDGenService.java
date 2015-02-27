package net.popbean.pf.id.service;

import java.util.List;

import net.popbean.pf.exception.BusinessError;

import com.alibaba.fastjson.JSONObject;

public interface IDGenService<T> {
	/**
	 * 
	 * @param param
	 * @return
	 * @throws BusinessError
	 */
	public T gen(JSONObject param) throws BusinessError;
	/**
	 * 
	 * @param param
	 * @param len
	 * @return
	 * @throws BusinessError
	 */
	public List<T> batch(JSONObject param, Integer len) throws BusinessError;
}
