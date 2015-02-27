package net.popbean.pf.entity.helper;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.exception.ErrorBuilder;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

public class VOHelper {
	/**
	 * 将value转化为指定的类型
	 * @param clazz
	 * @param value
	 * @return
	 */
	public static <T> T  cast(Class<T> clazz,Object value){
		if(clazz.equals(String.class)){
			return (T)value;
		}else if(clazz.equals(Double.class)){
			return (T)TypeUtils.castToDouble(value);
		}else if(clazz.equals(Date.class)){
			return (T)TypeUtils.castToDate(value);
		}else if(clazz.equals(Timestamp.class)){
			return (T)TypeUtils.castToTimestamp(value);
		}
		return (T)value;
	}
	/**
	 * 
	 * @param model
	 * @param vo
	 * @throws Exception
	 */
	public static void validate(EntityModel model,IValueObject vo)throws Exception{
		if(model == null){
			throw new Exception("传入参数错误(tm空值)");
		}
		if(vo == null){
			throw new Exception("传入参数错误(vo空值)");
		}
		JSONObject jo = JOHelper.vo2jo(vo);
		validate(model, jo);
		//
		return ;
	}
	public static void validate(EntityModel model,JSONObject jo)throws Exception{
		if(model == null){
			throw new Exception("传入参数错误(tm空值)");
		}
		if(JOHelper.isEmpty(jo)){
			throw new Exception("传入参数错误(jo空值)");
		}
		int pos = 0;
		List<FieldModel> list = model.field_list;
		
		FieldModel pk_field = model.findPK();
		//
		for(FieldModel f:list){
			if(f == null){
				ErrorBuilder.createSys().msg(model.code+"中的第"+pos+"位数据为空(field.size="+model.field_list.size()+")").execute();
			}
			//不是主键 并且 必填 并且 空值
			if(pk_field!=null && !f.code.equals(pk_field.code)){//不是主键
				//非空 但值为空
				if(f.required && StringUtils.isBlank(jo.getString(f.code))){
					if(f.defaultValue == null){
						ErrorBuilder.createBusiness().msg("必填项["+model.code+"."+f.name+"("+f.code+")]为空").execute();
					}else{
						jo.put(f.code, f.defaultValue);//确保填补默认值		
					}
				}
			}
			if(f.type == Domain.Code || f.type == Domain.Memo || f.type == Domain.PK){
				int maxLen = f.length;//((StringField)f).getMaxLen();
				String v = jo.getString(f.code);
				if(v!=null && v.length()>maxLen && f.code.lastIndexOf("_")==-1){//如果是ID_这样的，就不做任何处理
					throw new Exception("["+f.code+"]的值：["+v+"]超长");
				}				
			}
			//类型转化
			if(JOHelper.has(f, jo)){
				jo.put(f.code,jo.get(f));
			}
			pos++;
		}
		//
		return ;
	}
	public static boolean has(FieldModel field,IValueObject vo)throws Exception{
		IValueObjectWrapper wrapper = EntityWrapperHelper.wrapper(vo.getClass());//有缓存的，别怕
		Object value = wrapper.get(vo, field.code);
		if(value != null){
			return true;
		}
		return false;
	}
}
