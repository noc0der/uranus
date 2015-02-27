package testcase.vo;

import com.alibaba.fastjson.util.TypeUtils;

/**
 * 尝试进行包装的处理
 * @author to0ld
 *
 */
public class AccountWrapper {
	/**
	 * 
	 * @param target
	 * @param key
	 * @param value
	 */
	public void set(AccountVO target,String key,Object value){
		if("pk_account".equals(key)){
			target.pk_account = TypeUtils.castToString(value);
		}else if("code_account".equals(key)){
			target.code_account = TypeUtils.castToString(value);
		}
	}
	public Object get(AccountVO target,String key){
		if("pk_account".equals(key)){
			return target.pk_account;
		}else if("code_account".equals(key)){
			return target.code_account;
		}
		return null;
	}
}
