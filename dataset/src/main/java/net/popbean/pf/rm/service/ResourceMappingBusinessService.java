package net.popbean.pf.rm.service;

import java.util.List;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.rm.vo.ResourceMappingModel;
import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

/**
 * 资源映射服务(用于授权)
 * 
 * @author to0ld
 * 
 */
public interface ResourceMappingBusinessService {
	/**
	 * 
	 * @param code_rm
	 * @param pk_subject
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	List<JSONObject> fetchResourceList(String code_rm, String pk_subject, SecuritySession client) throws BusinessError;

	/**
	 * 
	 * @param code_rm
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	ResourceMappingModel findModel(String code_rm, SecuritySession client) throws BusinessError;

	/**
	 * 
	 * @param rm_code
	 * @param pk_subject
	 * @param client
	 * @return {chosen: chosen_list, unchosen: unchosen_list}
	 */
	JSONObject fetchRangesForMapping(String rm_code, String pk_subject, SecuritySession client) throws BusinessError;
	/**
	 * 
	 * @param rm_code
	 * @param pk_subject
	 * @param isChoiced
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	List<JSONObject> fetchRangeListForMapping(String rm_code, String pk_subject, Boolean isChoiced, JSONObject param, SecuritySession client) throws BusinessError;

	void mapping(String rm_code, String pk_subject, List<String> pk_resource_list, SecuritySession client) throws BusinessError;


	/**
	 * 多个数据权限设定的交际(咱就不用搞关系运算了吧)
	 * @param rm_unique 必须是resource相同的一组
	 * @param param
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	List<JSONObject> fetch(List<String> rm_unique, JSONObject param, SecuritySession client) throws BusinessError;

	/**
	 * 同步，或者说复制数据权限
	 * <br>
	 * 把rm_code中主体为pk_subject的数据，复制到其它主体
	 * @param rm_code 资源主体
	 * @param pk_subject 主体
	 * @param pk_subjects 多个其它主体
	 * @param client 用户身份
	 * @return
	 * @throws BuzException
	 */
	JSONObject syncing(String rm_code, String pk_subject, List<JSONObject> pk_subjects, SecuritySession client) throws BusinessError;
	/**
	 * 
	 * @param rm_unique
	 * @param param
	 * @param paging
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	List<JSONObject> fetchSubjectList(String rm_unique, JSONObject param, Paging paging, SecuritySession client) throws BusinessError;

	void grant(String rm_code, String pk_subject, List<String> pk_resource_list, SecuritySession client) throws BusinessError;

	void remove(String rm_code, String pk_subject, List<String> pk_resource_list, SecuritySession client) throws BusinessError;
}
