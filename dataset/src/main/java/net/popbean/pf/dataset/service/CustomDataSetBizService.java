package net.popbean.pf.dataset.service;

import java.util.List;

import net.popbean.pf.dataset.vo.DataSetModel;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;
/**
 * 用于自定义的逻辑(应该被groovy取代)
 * @author to0ld
 *
 */
public interface CustomDataSetBizService {
	/**
	 * 
	 * @param model
	 * @param param
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	public List<JSONObject> fetch(DataSetModel model,JSONObject param,Paging paging,SecuritySession client)throws BusinessError;
}
