package testcase.vo;

import java.sql.Date;

import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.helper.VOHelper;


/**
 * 尝试进行包装的处理
 * @author to0ld
 *
 */
public class AccountVO1Wrapper implements IValueObjectWrapper<AccountVO>{
//	public static final String KEY_BUS = "pk_account#code_account#date_birth";
	/**
	 * 
	 * @param target
	 * @param key
	 * @param value
	 */
	public void set(AccountVO target,String key,Object value){//其实就是一个映射关系，避免反射，艹
		//为了防止空转，应该先进行一次判断
//		if(KEY_BUS.indexOf(key) == -1){//多一次判断，有可能会导致性能下降，如果index慢，可以用map哈
//			return ;
//		}
		//
		if("pk_account".equals(key)){
			target.pk_account = VOHelper.cast(String.class,value);
			return ;
		}
		if("code_account".equals(key)){
			target.code_account = VOHelper.cast(String.class,value);
			return ;
		}
		if("date_birth".equals(key)){
			target.date_birth = VOHelper.cast(Date.class,value);
			return ;
		}
	}
	public Object get(AccountVO target,String key){
		if("pk_account".equals(key)){
			return target.pk_account;
		}
		if("code_account".equals(key)){
			return target.code_account;
		}
		if("date_birth".equals(key)){
			return target.date_birth ;
		}
		return null;
	}
}
