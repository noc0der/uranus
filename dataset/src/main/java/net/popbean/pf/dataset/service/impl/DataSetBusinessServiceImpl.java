package net.popbean.pf.dataset.service.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import net.popbean.pf.business.service.impl.AbstractBusinessService;
import net.popbean.pf.dataset.helper.DataSetHelper;
import net.popbean.pf.dataset.service.CustomDataSetBizService;
import net.popbean.pf.dataset.service.DataSetBusinessService;
import net.popbean.pf.dataset.vo.DataSetFieldModel;
import net.popbean.pf.dataset.vo.DataSetModel;
import net.popbean.pf.dataset.vo.Scope;
import net.popbean.pf.dataset.vo.SourceType;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.helper.TreeHelper;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.security.vo.SecuritySession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
@Service("service/pf/dataset")
public class DataSetBusinessServiceImpl extends AbstractBusinessService implements DataSetBusinessService {
	@Autowired
	private ApplicationContext appctx;
	//
	@Override
	public JSONObject findModel(String ds_code, JSONObject param, Boolean hasData, Boolean hasBuildTree, Paging paging, SecuritySession client) throws BusinessError {
		try {
			JSONObject ret = new JSONObject();
			DataSetModel model = findModel(ds_code, client);
			ret.put("model", model);
			// 将model转化成前端能识别的数据
			if(hasData){
				List<JSONObject> range_list = fetchPagingRangeList(model, param, true, paging, client);
				ret.put("data", range_list);				
			}
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public List<JSONObject> fetchPagingRangeList(String ds_code, JSONObject param, Boolean hasBuildTree, Paging paging, SecuritySession client) throws BusinessError {
		try {
			DataSetModel model = findModel(ds_code, client);
			return fetchPagingRangeList(model, param, hasBuildTree, paging, client);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	@Override
	public DataSetModel findModel(String ds_unique, SecuritySession client) throws BusinessError {
		try {
			//
			JSONObject p = new JSONObject();
			p.put("ds_unique",ds_unique);
			StringBuilder sql = new StringBuilder(" select a.* from pb_pf_ds a where (pk_ds=${ds_unique} or code_ds=${ds_unique}) ");
			DataSetModel inst = _commondao.find(sql, p, DataSetModel.class, "没有找到指定的数据集ds_unique="+ds_unique);
			p.put("pk_ds", inst.pk_ds);
			sql = new StringBuilder(" select a.* from pb_pf_ds_field a where a.pk_ds=${pk_ds} order by inum");
			List<DataSetFieldModel> list = _commondao.query(sql, p, DataSetFieldModel.class);
			if(CollectionUtils.isEmpty(list)){//支持全盘接收的模式，省事
//				throw new BuzException("该数据集不完整，无法使用，请与运维人员联系");
			}else{
				inst.field_list = list;
				//将pk_field补上
				for(DataSetFieldModel field:list){
					if(field.ispk){
						inst.pk_field = field.code_field;
					}
					if(Scope.Data.equals(field.scope) && StringUtils.isBlank(inst.show_field)){
						inst.show_field = field.code_field;
					}
				}
			}
			return inst;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	
	private List<JSONObject> fetchPagingRangeList(DataSetModel model,JSONObject param,Boolean hasBuildTree,Paging paging,SecuritySession client)throws BusinessError{
		try {
			if (model == null) {// 其实findModel里面会抛出异常的，做个冗余的保护，而已
				ErrorBuilder.createSys().msg("传入的数据集模型为空").execute();
			}

			String ds_code = model.code_ds;
			param = JOHelper.cleanEmptyStr(param);
			// 第零步：合并client与param
			param.put("code_ds", ds_code);
			param = DataSetHelper.mergeEnv(param, client);
			//
			//
			List<DataSetFieldModel> field_list = model.field_list;
			if(!CollectionUtils.isEmpty(field_list)){
				for (DataSetFieldModel v : field_list) {
					// FIXME: 同时是属性和参数呢？
					if (v.ispk) {// 搞出主键来
						model.pk_field = v.code_field;
					}
				}				
			}
			//
//			
			List<JSONObject> list = null;
			// 第二步解析model，用规则来处理数据
			if (SourceType.Spring.equals(model.src_type)) {// spring
				// FIXME 需要优先考虑解决
				String exec_exp = model.exec_exp;
				if (!StringUtils.isBlank(exec_exp)) {
					CustomDataSetBizService cdbs = appctx.getBean(exec_exp, CustomDataSetBizService.class);// FIXME
					list = cdbs.fetch(model, param, paging, client);
				}
			} else if (SourceType.Restful.equals(model.src_type)) {// restful

				list = processFromHttp(param, paging, model);

			} else if (SourceType.Sql.equals(model.src_type)) {// sql
				// FIXME 需要优先考虑解决
				String sql = model.exec_exp;
				// 去掉参数点前部分
				param = fixKey(param);
				for (String key : param.keySet()) {
					int pos = key.indexOf(".");
					if (pos != -1) {// 要是有ab.这种变态，让丫死去
						param.put(key.substring(pos + 1), param.getString(key));
					}
				}
				list = _commondao.paging(new StringBuilder(sql), param, paging);
				// FIXME 需要根据model来进行分级处理
			}
			list = convertData(model, list);//结构转化
			if (hasBuildTree) {// 如果有必要就构建tree
				String pk_key = model.pk_field;
				if(StringUtils.isBlank(pk_key)){
					Paging pg = Paging.buildByResult(list);
					list = TreeHelper.buildTree(list, pk_key.toUpperCase(), "SERIESCODE");
					if(list.size()>0){
						list.get(0).put("totalCount",pg.totalCount);
						list.get(0).put("pageSize",pg.pageSize);
						list.get(0).put("totalPageCount",pg.totalPageCount);
						list.get(0).put("pageNo",pg.currentPageNo);
					}
				}
			}
			return list;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	private List<JSONObject> processFromHttp(JSONObject param, Paging paging, DataSetModel model) throws BusinessError {
		String content = "";
		try {
			// http
			String pk_key = model.pk_field;
			List<DataSetFieldModel> field_list = model.field_list;
			String exec_exp = model.exec_exp;
			
			
			if (!exec_exp.startsWith("http")) {// 确保有头
				exec_exp = "http://" + exec_exp;
			}
			//读取数据，你丫的不会上POST METHOD吧
			if(exec_exp.indexOf("?")== -1){
				exec_exp+="?";
			}
			for (String key : param.keySet()) {
				if(!exec_exp.endsWith("?")){
					exec_exp+="&";
				}
				exec_exp+=key+"="+encode(param.get(key));
			}
			content = HttpHelper.getContent(exec_exp);
			if(StringUtils.isBlank(content)){
				return new ArrayList<>();
			}
			if(CollectionUtils.isEmpty(field_list)){//假定是按照约定过来的{data:[]}
				JSONObject tmp = JSON.parseObject(content);
				return JOHelper.ja2list(tmp.getJSONArray("data"));
			}
			//
			String[] parts = null;
			int min_part = 9;//指定一个绝对大数
			int max_part = 0;
			for (DataSetFieldModel field : field_list) {
				String field_code_src = StringUtils.isBlank(field.code_field)?field.code_vendor:field.code_field;//JOHelper.getString(field, "FIELD_CODE_SRC", "FIELD_CODE");
				if (StringUtils.isBlank(field_code_src)) {
					ErrorBuilder.createSys().msg("模型设置错误，没有找到field_code_src or field_code属性的设置").execute();
				}
				parts = field_code_src.split("\\.");
				if (max_part < parts.length) {
					max_part = parts.length;
				}
				if (min_part > parts.length) {
					min_part = parts.length;
				}
			}
			if ((max_part - min_part) > 2) {
				ErrorBuilder.createSys().msg("不支持超过2层的数据结构").execute();
			}
			List<JSONObject> tmp = null;
			if (min_part == 1) {
				tmp = JSON.parseArray(content, JSONObject.class);//JO.parseArray(content, VO.class);
			} else if (min_part == 2) {
				JSONObject data = JSON.parseObject(content);
				tmp = JOHelper.ja2list(data.getJSONArray(parts[0]));
			} else if(min_part == 3){//应该用delta = max_part-min_part来伸缩
				JSONObject data = JSON.parseObject(content);
				data = data.getJSONObject(parts[0]);
				tmp = JOHelper.ja2list(data.getJSONArray(parts[1]));
			}
			List<JSONObject> list = new ArrayList<>();
			int start = 0;
			int end = tmp.size();
			if (paging != null) {
				start = (paging.currentPageNo - 1) * paging.pageSize;
				end = paging.currentPageNo * paging.pageSize;
			}
			for (int i = start; i < end; i++) {
				list.add(tmp.get(i));
			}
			return list;
		} catch (Exception e) {
			processError("content:"+content, e);
		}
		return null;
	}
	private  String encode(Object s) throws Exception{
		String v = null;
		if (s == null) {
			v = "";
		} else if (s.getClass().isPrimitive()) {
			v = String.valueOf(s);
		} else if (s instanceof String) {
			v = (String) s;
		} else {
			v = JSON.toJSONString(s);
		}
		return URLEncoder.encode(v, "UTF-8");
	}

}
