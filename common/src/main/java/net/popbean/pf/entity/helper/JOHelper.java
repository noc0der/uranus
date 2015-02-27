package net.popbean.pf.entity.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;

public class JOHelper {
	/**
	 * 
	 * @param jo
	 * @return
	 */
	public static boolean isEmpty(JSONObject jo){
		if(jo == null){
			return true;
		}
		if(jo.keySet().size() >0){
			return false;
		}
		return true;
	}
	/**
	 * 
	 * @param jo
	 * @param key
	 * @param def
	 * @return
	 */
	public static Integer getIntValue(JSONObject jo,String key,int def){
		Integer value = jo.getInteger(key);
		if(value == null){
			return def;
		}
		return value;
	}
	public static Integer selectInt(JSONObject jo,String...key){
		for(String k:key){
			Integer value = jo.getInteger(k);
			if(value!=null){
				return value;
			}
		}
		return null;
	}
	/**
	 * 
	 * @param jo
	 * @param key
	 * @return
	 */
	public static String selectString(JSONObject jo,String...key){
		for(String k:key){
			String value = jo.getString(k);
			if(value!=null){//长度为0的字符串，应该也算有值
				return value;
			}
		}
		return null;
	}
	public static Object selectObject(JSONObject jo,String...key){
		for(String k:key){
			Object value = jo.get(k);
			if(value!=null){//长度为0的字符串，应该也算有值
				return value;
			}
		}
		return null;
	}
	public static Object getObjectByIgnoreCase(JSONObject jo,String key){
		return selectObject(jo, key,key.toLowerCase(),key.toUpperCase());
	}
	/**
	 * 将bean转成vo结构
	 * @param javaObject
	 * @return
	 */
    public static final Object toVO(Object javaObject) {
        return toVO(javaObject, ParserConfig.getGlobalInstance());
    }
    @SuppressWarnings("unchecked")
    public static final Object toVO(Object javaObject, ParserConfig mapping) {
        if (javaObject == null) {
            return null;
        }

        if (javaObject instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) javaObject;

            JSONObject json = new JSONObject(map.size());

            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object key = entry.getKey();
                String jsonKey = TypeUtils.castToString(key);
                Object jsonValue = toVO(entry.getValue());
                json.put(jsonKey, jsonValue);
            }

            return json;
        }

        if (javaObject instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) javaObject;

            List<Object> array = new ArrayList<Object>(collection.size());

            for (Object item : collection) {
                Object jsonValue = toVO(item);
                array.add(jsonValue);
            }

            return array;
        }
        if (javaObject instanceof JSON) {
            return (JSON) javaObject;
        }
        Class<?> clazz = javaObject.getClass();

        if (clazz.isEnum()) {
            return ((Enum<?>) javaObject).name();
        }

        if (clazz.isArray()) {
            int len = Array.getLength(javaObject);

            List<Object> array = new ArrayList<Object>(len);

            for (int i = 0; i < len; ++i) {
                Object item = Array.get(javaObject, i);
                Object jsonValue = toVO(item);
                array.add(jsonValue);
            }

            return array;
        }

        if (mapping.isPrimitive(clazz)) {
            return javaObject;
        }

        try {
            List<FieldInfo> getters = TypeUtils.computeGetters(clazz, null);

            JSONObject json = new JSONObject(getters.size());

            for (FieldInfo field : getters) {
                Object value = field.get(javaObject);
                Object jsonValue = toVO(value);

                json.put(field.getName(), jsonValue);
            }

            return json;
        } catch (Exception e) {
            throw new JSONException("toJSON error", e);
        }
    }
    /**
     * 
     * @param key
     * @param vo
     * @return
     */
	public static boolean has(String key,JSONObject vo){
		if(isEmpty(vo)){
			return false;
		}
		if(StringUtils.isBlank(key)){
			return false;
		}
		if(vo.get(key)!=null){
			return true;
		}
		return false;
	}
	public static boolean has(FieldModel field,JSONObject vo){//将来可能会做更严格的校验
		if(field == null){
			return false;//这个也不太好哈
		}
		return has(field.code,vo);
	}
	public static boolean hasIgnoreCase(String key,JSONObject vo){
		if(isEmpty(vo)){
			return false;
		}
		if(StringUtils.isBlank(key)){
			return false;
		}
		if(vo.get(key)!=null || vo.get(key.toLowerCase())!=null || vo.get(key.toUpperCase())!=null){
			return true;
		}
		return false;
	}
	/**
	 * 利用asm来进行vo to jsonobject转化
	 * 没有采用反射，是因为反射太慢了
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public static JSONObject vo2jo(IValueObject vo)throws Exception{
		if(vo == null){
			return null;
		}
		EntityModel model = EntityModelHelper.build(vo);
		IValueObjectWrapper wrapper = EntityWrapperHelper.wrapper(vo.getClass());

		JSONObject ret = new JSONObject();
		for(FieldModel f:model.field_list){
			String key = f.code;
			ret.put(key, wrapper.get(vo, key));
		}
		return ret;
	}
	/**
	 * 排除空串，解决查询的瞎拼写字符串的问题
	 * @param jo
	 * @return
	 */
	public static JSONObject cleanEmptyStr(JSONObject jo){
		if(isEmpty(jo)){
			return new JSONObject();
		}
		Iterator<Entry<String, Object>> inter = jo.entrySet().iterator();
		while(inter.hasNext()){
			Entry<String,Object> next = inter.next();
			Object v = jo.get(next.getKey());
			if(v == null || StringUtils.isBlank(v.toString()) || v.toString().equals("[]")){
				inter.remove();
			}
		}
		return jo;
	}
	public static boolean equalsStringValue(JSONObject vo,String key,Object value){
		if(vo == null || StringUtils.isBlank(key)){
			return false;
		}
		if(vo.get(key) == null){
			if(value == null){
				return true;
			}else{
				return false;
			}
		}else{//vo$(key) is not null
			if(value == null && vo.getString(key).equals("")){//值为空的情况
				return true;
			}else if(value !=null && vo.getString(key).equals(value.toString())){
				return true;
			}
			return false;
		}
	}
	/**
	 * 
	 * @param ja
	 * @return
	 */
	public static List<JSONObject> ja2list(JSONArray ja){
		List<JSONObject> ret = new ArrayList<>();
		for(int i=0,len=ja.size();i<len;i++){
			ret.add(ja.getJSONObject(i));
		}
		return ret;
	}
}
