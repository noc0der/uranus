package net.popbean.pf.dataset.service;

import java.util.List;

import net.popbean.pf.dataset.vo.DataSetModel;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

public interface DataSetBusinessService {
	/**
	 * 
	 * @param ds_code
	 * @param param
	 * @param hasData
	 * @param hasBuildTree
	 * @param paging
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	JSONObject findModel(String ds_code,JSONObject param,Boolean hasData,Boolean hasBuildTree,Paging paging, SecuritySession client) throws BusinessError;
	
	//JSONObject findModelWithData(String ds_code, JSONObject param,Boolean hasBuildTree,Paging paging, Session client) throws BusinessError;
	/**
	 * 
	 * @param ds_code
	 * @param param
	 * @param hasBuildTree
	 * @param paging
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	List<JSONObject> fetchPagingRangeList(String ds_code,JSONObject param,Boolean hasBuildTree,Paging paging, SecuritySession client) throws BusinessError;
	/**
	 * 
	 * @param ds_unique
	 * @param client
	 * @return
	 * @throws BusinessError
	 */
	DataSetModel findModel(String ds_unique, SecuritySession client) throws BusinessError;
}
