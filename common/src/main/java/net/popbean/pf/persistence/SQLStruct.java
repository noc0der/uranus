package net.popbean.pf.persistence;

import java.util.ArrayList;
import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.helper.EntityWrapperHelper;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.helper.VOHelper;
import net.popbean.pf.exception.ErrorBuilder;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author to0ld
 *
 */
public class SQLStruct {
	//FIXME renamed SqlBuilder
	private static final long serialVersionUID = -4049787403014556858L;
	public boolean translated = false;//是否已经翻译
	public StringBuffer nataiveSql = new StringBuffer();//可执行的，preparedstmt的sql
	public List<String> cols = new ArrayList<String>();//变量的序列
	public List<Object> values = new ArrayList<Object>();//需要填充的变量值
	/**
	 * 将part添加到sql的尾部并将col写入参数列表
	 * 适用于直接构建translated=true的场景
	 * @param part
	 * @param col
	 */
	public SQLStruct appendPart(String part,String col){
		if(!StringUtils.isBlank(part)){
			nataiveSql.append(part);
		}
		if(!StringUtils.isBlank(col)){
			cols.add(col);
		}
		return this;
	}
	public SQLStruct appendPart(String part){
		return appendPart(part, null);
	}
	/**
	 * 
	 * @param vo
	 * @return
	 */
	public SQLStruct bind(JSONObject vo)throws Exception{
		if(JOHelper.isEmpty(vo)){//进行一下基本的保护
			return this;
		}
		List<Object> params = new ArrayList<Object>();
		for (String key : this.cols) {
			Object t = JOHelper.getObjectByIgnoreCase(vo, key);//依然有可能为空
			if(t == null){
				ErrorBuilder.createSys().msg("传入的数据中没有key="+key+"("+JSON.toJSONString(vo)+")").execute();//如果有必要就给出整个vo(toJsonString)
			}
			params.add(t);
		}
		this.values = params;
		return this;
	}
	public <T extends IValueObject> SQLStruct bind(T vo)throws Exception{
		if(vo == null){//进行一下基本的保护
			return this;
		}
		IValueObjectWrapper<T> wrapper = (IValueObjectWrapper<T>)EntityWrapperHelper.wrapper(vo.getClass());
		List<Object> params = new ArrayList<Object>();
		for (String key : this.cols) {
			Object t = wrapper.get(vo, key);
			if(t == null){
				t = wrapper.get(vo, key.toUpperCase());
				if(t == null){
					t = wrapper.get(vo, key.toLowerCase());
				}
			}
			params.add(t);
		}
		this.values = params;
		return this;
	}
	@SuppressWarnings("unchecked")
	public SQLStruct clone(){
		SQLStruct ret = new SQLStruct();
		ret.translated = this.translated;
		ret.cols = (ArrayList<String>)((ArrayList<String>)this.cols).clone();
		ret.values = (ArrayList<Object>)((ArrayList<Object>)this.values).clone();
		ret.nataiveSql = new StringBuffer(this.nataiveSql.toString());
		return ret;
	}
}
