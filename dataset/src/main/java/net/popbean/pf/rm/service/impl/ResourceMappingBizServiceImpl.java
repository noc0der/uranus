package net.popbean.pf.rm.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.popbean.pf.business.service.impl.AbstractBusinessService;
import net.popbean.pf.dataset.service.DataSetBusinessService;
import net.popbean.pf.dataset.vo.DataSetModel;
import net.popbean.pf.entity.helper.FieldHelper;
import net.popbean.pf.entity.helper.JO;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.persistence.helper.DaoHelper;
import net.popbean.pf.rm.helper.ResourceMappingHelper;
import net.popbean.pf.rm.service.ResourceMappingBusinessService;
import net.popbean.pf.rm.vo.ResourceMappingModel;
import net.popbean.pf.security.vo.SecuritySession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

@Service("service/pf/resourcemapping")
public class ResourceMappingBizServiceImpl extends AbstractBusinessService implements ResourceMappingBusinessService {
//	@Autowired
//	@Qualifier("pf.datapod.service")
//	protected DataPodBizService dpService;
	@Autowired
	@Qualifier("service/pf/dataset")
	protected DataSetBusinessService dsService;
	/**
	 * 查找选中和没被选中的数据，存在一个VO,格式为{chosen: chosen_list, unchosen: unchosen_list}
	 * @param rm_code 资源映射编码
	 * @param pk_subject 主体主键
	 * @param client 环境参数
	 */
	@Override
	public JSONObject fetchRangesForMapping(String rm_code, String pk_subject, SecuritySession client)throws BusinessError {
		try {
			//FIXMME 先查询得到配置
			ResourceMappingModel rm_inst = findBaseInfo(rm_code);
			
			//FIXME 暂时不考虑istat=0，封存的情况
			//需要识别一下是排除还是咋
			String rlt_md_code = rm_inst.code_relation;
			StringBuilder sql = new StringBuilder(" select a.* from "+rlt_md_code+" a where ref_subject=${ref_subject} ");
			List<JSONObject> rlt_list = _commondao.query(sql, JO.gen("PK_SUBJECT",pk_subject));//得到授权数据
			String pk_resource_code = rm_inst.ref_resource;//受控资源主键
			JSONObject ds_inst = dsService.findModel(pk_resource_code, new JSONObject(), true,true,null,client);
			List<JSONObject> range_list = JOHelper.ja2list(ds_inst.getJSONArray("data"));
			//FXIME 需要注意的是，要把memo给加上，要不然满足不了财务的要求：不同的部门看到的同一费用科目，备注不同
			String pk_key = ds_inst.getString("PK_FIELD");
			List<JSONObject> chosenList = VOHelper.in(range_list, rlt_list, pk_key, "PK_RESOURCE");
				//FIXME 获得resource的数据集；得到授权范围，做交集
			List<JSONObject> unchosenList = VOHelper.notIn(range_list, rlt_list, pk_key, "PK_RESOURCE");
			return JO.gen("chosen", chosenList, "unchosen", unchosenList);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	/**
	 * 查找可选的数据(仅仅用于配置，不能用于外部的业务调用)
	 * @param isChoiced 针对已经选中的情况
	 * @return
	 * @throws BuzException
	 */
	@Override
	public List<JSONObject> fetchRangeListForMapping(String rm_code,String pk_subject,Boolean isChoiced,JSONObject param,SecuritySession client)throws BusinessError{
		try {
			//FIXMME 先查询得到配置
			ResourceMappingModel rm_inst = findBaseInfo(rm_code);
			
			//FIXME 暂时不考虑istat=0，封存的情况
			//需要识别一下是排除还是咋
			String rlt_md_code = rm_inst.code_relation;
			StringBuilder sql = new StringBuilder(" select a.* from "+rlt_md_code+" a where ref_subject=${ref_subject} ");
			List<JSONObject> rlt_list = _commondao.query(sql, JO.gen("PK_SUBJECT",pk_subject));//得到授权数据
			String pk_resource_code = rm_inst.ref_resource;//受控资源主键
			JSONObject ds_inst = dsService.findModel(pk_resource_code, param, true,true,null,client);
			DataSetModel ds_model = (DataSetModel)ds_inst.get("model");
			List<JSONObject> range_list = JOHelper.ja2list(ds_inst.getJSONArray("data"));
			//FXIME 需要注意的是，要把memo给加上，要不然满足不了财务的要求：不同的部门看到的同一费用科目，备注不同
			String pk_key = ds_model.pk_field;
			List<JSONObject> list = new ArrayList<JSONObject>();
			if(isChoiced){//已经选择的情况
				//FIXME 获得resource的数据集；得到授权的范围，做差集
//				list = intersect(range_list, rlt_list, pk_key,"PK_RESOURCE");
				list = VOHelper.in(range_list, rlt_list, pk_key, "PK_RESOURCE");
			}else{//已经选中的，类似交集
				//FIXME 获得resource的数据集；得到授权范围，做交集
//				list = sub(range_list,rlt_list,pk_key);
				list = VOHelper.notIn(range_list, rlt_list, pk_key, "PK_RESOURCE");
			}
			if(!list.isEmpty() && param != null && VOHelper.has("pageNo", param) && VOHelper.has("pageSize", param)) {
				Paging paging = Paging.build(param);
				int totalCount = list.size();
				int mod = totalCount%paging.pageSize;
				int totalPageCount = totalCount/paging.pageSize + (mod > 0 ? 1: 0);
				int offset = (paging.currentPageNo - 1) * paging.pageSize;
				int length = totalCount - offset < paging.pageSize ? mod : paging.pageSize;
				list = list.subList(offset, offset+length);
				JSONObject head = list.get(0);
				head.put("pageNo", paging.currentPageNo);
				head.put("totalPageCount",totalPageCount);
				head.put("totalCount",totalCount);
				head.put("pageSize", paging.pageSize);
			}
			return list;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	
	/**
	 * 根据可选的条件来判断是否有配套的授权，叠加后给出符合条件的结果
	 * @param rm_unique
	 * @param param
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	@Override
	public List<JSONObject> fetch(List<String> rm_unique,JSONObject param,SecuritySession client)throws BusinessError{
		try {
			//首先应该来一个判空吧
			if(CollectionUtils.isEmpty(rm_unique)){
				ErrorBuilder.createBusiness().msg("传入的rm_unique为空，没法弄").execute();
			}
			List<JSONObject> ret = null;
			for(String rm:rm_unique){
				ret = fetchResourceList(rm,param,ret,client);
			}
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 
	 * @param rm_unique
	 * @param param
	 * @param list 需要与结果集进行匹配的数据
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	private List<JSONObject> fetchResourceList(String rm_unique,JSONObject param,List<JSONObject> list,SecuritySession client)throws BusinessError{
		try {
			//根据rm_unique得到rm_inst
			StringBuilder sql = new StringBuilder(" select a.* ");
			sql.append(" from mt_pf_rm a ");
			sql.append(" where 1=1 and (a.rm_code=${RM_UNIQUE} or a.pk_rm=${RM_UNIQUE}) ");
			ResourceMappingModel rm_inst = _commondao.find(sql, JO.gen("RM_UNIQUE",rm_unique),ResourceMappingModel.class,null);
			if(rm_inst == null){//没有找到配套的rm，就原路返回
				return list;
			}
			//根据rm_inst得到pk_subject model,pk_resource model
//			String pk_subject_code = rm_inst.getString("PK_SUBJECT_CODE");
//			VO subject_model = dsService.findModel(pk_subject_code, client);
			
			//根据pk_subject model中的pk_field看看，param有没有key
			param = JOHelper.cleanEmptyStr(param);
			param = fixKey(param);
			
			Map<Object, Object> values = new HashMap<Object, Object>();
			for(String key:param.keySet()){//先把重复的，不是主键的滤掉
				Object value = param.get(key);
				if(value == null || key.toUpperCase().indexOf("PK_") == -1){
					continue;
				}
				values.put(value, value);
			}
			List<String> pk_list = new ArrayList<String>();
			for(Object key:values.keySet()){
				pk_list.add(key.toString());
			}
			List<VO> ret =fetchResourceList(rm_inst,pk_list,param,client);
			if(list == null){
				//说明是第一次或者不需要合并
			}else{
				String pk_resource_code = rm_inst.getString("PK_RESOURCE_CODE");
				JSONObject resource_model = dsService.findModel(pk_resource_code,null,false,false,null, client);
				DataSetModel resource_ds_model = (DataSetModel)resource_model.get("model");
				String pk_field = resource_ds_model.pk_field;//FIXME 为空就得抛错
//				ret = intersect(ret,list,pk_field,pk_field);
				ret = VOHelper.in(ret,list,pk_field,pk_field);
			}
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	private JSONObject fixKey(JSONObject param){
//		for(String key:param.keySet()){
//			int pos = key.indexOf(".");
//			if(pos!=-1){//要是有ab.这种变态，让丫死去
//				param.put(key.substring(pos+1),param.get(key));	
//			}
//		}
//		return param;
		JSONObject ret = new JSONObject();
		Iterator<String> keys = param.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			int pos = key.indexOf(".");
			ret.put(key.substring(pos + 1), param.get(key));
//			if (pos != -1) {// 要是有ab.这种变态，让丫死去
//				
//			}else{
//				ret.put(key, param.get(key));
//			}
		}
		return ret;
	}
	/**
	 * 得到主体的值域列表
	 * @param rm_unique
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	@Override
	public List<JSONObject> fetchSubjectList(String rm_unique,JSONObject param,Paging paging,SecuritySession client)throws BusinessError{
		try {
			//得到主体的编码
			StringBuilder sql = new StringBuilder("select a.* from pb_pf_rm where 1=1 and (code_rm=${RM_UNIQUE} or pk_rm=${RM_UNIQUE}) ");
			JSONObject rm_inst = _commondao.find(sql, JO.gen("RM_UNIQUE",rm_unique),"没有找到rm_unique="+rm_unique+"的数据");
			//得到值域
			String pk_subject_code = rm_inst.getString("PK_SUBJECT_CODE");
			JSONObject model = dsService.findModel(pk_subject_code, param, true,true, paging, client);
			List<JSONObject> ret = JOHelper.ja2list(model.getJSONArray("result")); 
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 查询可见范围
	 * @param rm_unique 资源映射唯一标示
	 * @param pk_subject 主体
	 */
	@Override
	public List<JSONObject> fetchResourceList(String rm_unique, String pk_subject, SecuritySession client)throws BusinessError {
		try {
			//FIXMME 先查询得到配置
			StringBuilder sql = new StringBuilder(" select a.* from pb_pf_rm a ");
			sql.append(" where 1=1 and (a.code_rm=${RM_UNIQUE} or a.pk_rm=${RM_UNIQUE}) ");
			ResourceMappingModel rm_inst = _commondao.find(sql, JO.gen("RM_UNIQUE",rm_unique),ResourceMappingModel.class,"指定的映射配置，rm_unique="+rm_unique+"没有找到");
			//FIXME 暂时不考虑istat=0，封存的情况
			//需要识别一下是排除还是咋
			return fetchResourceList(rm_inst,Arrays.asList(pk_subject),new JSONObject(),client);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 根据资源映射模型，寻找符合条件的pk_subject的数据
	 * @param rm_inst
	 * @param pk_subject_list
	 * @param param
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	private List<JSONObject> fetchResourceList(ResourceMappingModel rm_inst, List<String> pk_subject_list, JSONObject param,SecuritySession client)throws BusinessError{
		try {
			String rlt_md_code = rm_inst.code_relation;
			StringBuilder sql = new StringBuilder(" select a.* from "+rlt_md_code+" a where 1=1 ");
			Integer itype = rm_inst.i_type;
			String pk_resource_code = rm_inst.ref_resource;
			JSONObject ds_inst = dsService.findModel(pk_resource_code, param,true,true,null, client);
			List<JSONObject> range_list = JOHelper.ja2list(ds_inst.getJSONArray("data"));
			
			if(CollectionUtils.isEmpty(pk_subject_list)){//如果不指定pk_subject
				if(itype == 0){//排除,返回全部
					return range_list;
				}else{//包含,返回空组
					return new ArrayList<JSONObject>();
				}
			}
			//
			sql.append(" and a.pk_subject ");
			sql.append(DaoHelper.Sql.in("PK_SUBJECT", pk_subject_list.size()));//FIXME 用exists是不是更好一些
			JSONObject p = new JSONObject();
			p = DaoHelper.Sql.in(p, pk_subject_list, "PK_SUBJECT");
			List<JSONObject> rlt_list = _commondao.query(sql, p);//得到授权数据
			
			//FXIME 需要注意的是，要把memo给加上，要不然满足不了财务的要求：不同的部门看到的同一费用科目，备注不同
			String pk_key = ds_inst.getString("PK_FIELD");
			List<JSONObject> list = new ArrayList<JSONObject>();
			if(itype == 0){//排除法:差集
				//FIXME 获得resource的数据集；得到授权的范围，做差集
//				list = sub(range_list,rlt_list,pk_key);
				list = VOHelper.notIn(range_list,rlt_list,pk_key,"PK_RESOURCE");
			}else{//包含法:交集
				//FIXME 获得resource的数据集；得到授权范围，做交集
//				list = intersect(range_list, rlt_list, pk_key,"PK_RESOURCE");
				list = VOHelper.in(range_list, rlt_list, pk_key,"PK_RESOURCE");
			}
			return list;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 获得映射的配置模型(主要是pk_subject,pk_resource)
	 * @param rm_code
	 * @param client
	 * @return
	 * @throws BuzException
	 */
	@Override
	public ResourceMappingModel findModel(String rm_code,SecuritySession client)throws BusinessError{
		try {
			JSONObject param = JO.gen("code_rm",rm_code);
			
			StringBuilder sql = new StringBuilder(" select a.* from pb_pf_rm a where code_rm=${code_rm} ");
			ResourceMappingModel rm_inst = _commondao.find(sql, param, ResourceMappingModel.class, "没有找到rm_code="+rm_code+"的资源映射配置");
			
			String pk_subject = rm_inst.ref_subject;
			String pk_resource = rm_inst.ref_resource;
			JSONObject subject_model = dpService.findModelByDataSet(pk_subject, new JSONObject(),true, false,true,null, client);
			JSONObject resource_model = dpService.findModelByDataSet(pk_resource, new JSONObject(), true,false,true,null, client);
			rm_inst.subject_ds_model = ;
			rm_inst.resource_ds_model = ;
			rm_inst.put("PK_SUBJECT_MODEL",subject_model);
			rm_inst.put("PK_RESOURCE_MODEL",resource_model);
			return rm_inst;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	private ResourceMappingModel findBaseInfo(String rm_unique)throws BusinessError{
		try {
			StringBuilder sql = new StringBuilder(" select a.code_relation,a.ref_resource from pb_pf_rm a where a.code_rm=${code_rm} ");
			ResourceMappingModel rm_inst = _commondao.find(sql, JO.gen("code_rm",rm_unique),ResourceMappingModel.class,"没有找到rm_code="+rm_unique+"的资源映射");
			return rm_inst;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	/**
	 * 增量添加
	 * @param rm_code
	 * @param pk_subject
	 * @param pk_resource_list
	 * @param client
	 * @throws BuzException
	 */
	@Override
	public void grant(String rm_code,String pk_subject,List<String> pk_resource_list,SecuritySession client)throws BusinessError{
		try {
			ResourceMappingModel rm_inst = findBaseInfo(rm_code);
			String rlt_md_code = rm_inst.code_relation;
			//清除一下已有的关系
			StringBuilder sql = new StringBuilder("select pk_subject,pk_resource from "+rlt_md_code+" where pk_subject=${PK_SUBJECT} and pk_resource ");
			sql.append(DaoHelper.Sql.in("PK_RESOURCE", pk_resource_list.size()));
			JSONObject p = DaoHelper.Sql.in(JO.gen(), pk_resource_list, "PK_RESOURCE");
			List<JSONObject> sub_list = _commondao.query(sql, p);
			//
			Map<String,String> bus = new HashMap<>();
			for(JSONObject sub:sub_list){
				sub.put(sub.getString("PK_RESOURCE"), sub.getString("PK_RESOURCE"));
			}
			//
			//
			List<JSONObject> data = new ArrayList<>();
			for(String v:pk_resource_list){
				JSONObject vo = new JSONObject();
				if(!bus.containsKey(v)){//已经存在的，就不改了
					vo.put("PK_SUBJECT",pk_subject);
					vo.put("PK_RESOURCE",v);//其实为空也是不行的
					data.add(vo);					
				}
			}
			EntityModel tm = ResourceMappingHelper.buildRelationEntityModel(rlt_md_code);
			FieldModel pk_resource = FieldHelper.ref("ref_resource","资源主键");
			FieldModel pk_subject_field = FieldHelper.ref("ref_subject","主体主键");
			_commondao.batchInsertJO(tm, data,new FieldModel[]{pk_subject_field,pk_resource});//要想省事，可以用replace，pk_subject:pk_resource
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * 增量清除
	 * @param rm_code
	 * @param pk_subject
	 * @param pk_resource_list
	 * @param client
	 * @throws BuzException
	 */
	@Override
	public void remove(String rm_code,String pk_subject,List<String> pk_resource_list,SecuritySession client)throws BusinessError{
		try {//FIXME 没有任何保护，呵呵
			ResourceMappingModel rm_inst = findBaseInfo(rm_code);
			String rlt_md_code = rm_inst.code_relation;
			//
			List<JSONObject> list = new ArrayList<>();
			for(String pk_resource: pk_resource_list){
				JSONObject vo = JO.gen("ref_subject",pk_subject);
				vo.put("PK_RESOURCE",pk_resource);
				list.add(vo);
			}
			StringBuilder sql = new StringBuilder("delete from "+rlt_md_code+" where ref_subject=${ref_subject} and ref_resource=${ref_resource}");
			_commondao.batch(sql, list);
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * 全量映射
	 * @param rm_code
	 * @param ref_subject 
	 * @param pk_resource_list
	 */
	@Override
	public void mapping(String rm_code,String ref_subject,List<String> pk_resource_list,SecuritySession client)throws BusinessError{
		try {
			ResourceMappingModel rm_inst = findBaseInfo(rm_code);
			String rlt_md_code = rm_inst.code_relation;
			StringBuilder sql = new StringBuilder(" delete from  " + rlt_md_code + " where ref_subject = ${ref_subject}");
			_commondao.executeChange(sql, JO.gen("ref_subject", ref_subject));
			//
			List<JSONObject> data = new ArrayList<>();
			for(String v:pk_resource_list){
				JSONObject vo = new JSONObject();
				vo.put("PK_SUBJECT",ref_subject);
				vo.put("PK_RESOURCE",v);//其实为空也是不行的
				data.add(vo);
			}
			EntityModel tm = ResourceMappingHelper.buildRelationEntityModel(rlt_md_code);
			FieldModel pk_resource = FieldHelper.ref("ref_resource","资源主键");
			FieldModel pk_subject_field = FieldHelper.ref("ref_subject","主体主键");
			_commondao.batchInsertJO(tm,data,new FieldModel[]{pk_subject_field,pk_resource});
		} catch (Exception e) {
			processError(e);
		}
	}
	/**
	 * @deprecated 作为一个扩展，没有必要放在这里
	 */
	@Override
	public JSONObject syncing(String rm_code, String pk_subject, List<JSONObject> pk_subjects, SecuritySession client) throws BusinessError {
		List<JSONObject> results = null;
		String rlt_md_code = null;
		try {
			// 找到对应的表，以及数据
			ResourceMappingModel rmmodel = findBaseInfo(rm_code);
			rlt_md_code = rmmodel.code_relation;
			StringBuilder sql = new StringBuilder("select pk_resource, memo from " + rlt_md_code + " where ref_subject = ${ref_subject}");
			results = _commondao.query(sql, JO.gen("ref_subject", pk_subject));
		} catch (Exception e) {
			processError(e);
		}
		if(CollectionUtils.isEmpty(results)){
			return JO.gen("msg", "原始为空，不需要复制");
		}
		JSONObject msg = new JSONObject();
		List<String> succ = new ArrayList<String>();
		List<String> fail = new ArrayList<String>();
		for (JSONObject new_subject : pk_subjects) {
			// 得到主键与显示值
			String pk_value = new_subject.getString("pk_field");
			String show_value = new_subject.getString("show_field");
			if (pk_value.equals(pk_subject)) {
				continue;
			}

			try {
				// 一个个插入
				mapping(rlt_md_code, pk_value, results);
				succ.add(show_value);
			} catch (Exception e) {
				e.printStackTrace();
				fail.add(show_value);
			}
		}

		// 返回信息，会展示msg部分
		String message = "成功" + succ.size() + "个，失败" + fail.size() + "个";
		if (fail.size() > 0) {
			message += ": " + toString(fail);
		}
		
		msg.put("msg", message);
		msg.put("succ", succ);
		msg.put("fail", fail);
		return msg;
	}

	/**
	 * 直接把数据插入到相应的表中
	 * @param rlt_md_code
	 * @param pk_subject
	 * @param results
	 * @throws Exception
	 */
	private void mapping(String rlt_md_code, String pk_subject, List<JSONObject> results) throws Exception {
		StringBuilder select = new StringBuilder();
		select.append("select pk_resource from ");
		select.append(rlt_md_code);
		select.append(" where pk_subject = ${PK_SUBJECT}");
		HashMap<String, String> current = new HashMap<String, String>();
		// 取出当前的资源，放进hash表，这样只是不想删除再插入
		for (JSONObject result : _commondao.query(select, JO.gen("PK_SUBJECT", pk_subject))) {
			current.put(result.getString("PK_RESOURCE"), null);
		}

		// 得到一份新的需要插入的数据
		List<JSONObject> newdata = new ArrayList<JSONObject>();
		for (JSONObject result : results) {
			if (!current.containsKey(result.getString("PK_RESOURCE"))) {
				JSONObject item = ObjectUtils.clone(result);
				item.put("PK_SUBJECT", pk_subject);
				newdata.add(item);
			}
		}

		if (newdata.size() == 0) {
			return;
		}

		// 和新增的一样，插入数据
		EntityModel tm = ResourceMappingHelper.buildRelationEntityModel(rlt_md_code);
		FieldModel pk_resource_field = FieldHelper.ref("ref_resource", "资源主键");
		FieldModel pk_subject_field = FieldHelper.ref("ref_subject", "主体主键");
		_commondao.batchInsertJO(tm, newdata, new FieldModel[] { pk_subject_field, pk_resource_field });
	}

	/**
	 * 把list转为字符串，类似python里的<code>", ".join(list)</code>
	 * @param list
	 * @return
	 */
	private static String toString(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder msg = new StringBuilder();
		for (String s : list) {
			msg.append(s);
			msg.append(", ");
		}
		return msg.substring(0, msg.length() - 2);
	}
}