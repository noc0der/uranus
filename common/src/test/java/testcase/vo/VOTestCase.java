package testcase.vo;

import java.lang.reflect.Field;

import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.helper.EntityWrapperHelper;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.helper.VOHelper;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSONObject;

/**
 * 针对vo进行测试
 * 
 * @author to0ld
 *
 */
public class VOTestCase {
	static int LOOP = 100000000;// 10^9
	@Test
	public void performanceForRead(){
		try {
			AccountVO vo = new AccountVO();
			long start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				String tmp = vo.code_account;// getter
			}
			long end = System.currentTimeMillis();
			System.out.println("getter/setter:" + (end - start));// 性能基线

			Field f = vo.getClass().getDeclaredField("code_account");
			f.setAccessible(true);// 加上确实快一些
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				Object value = f.get(vo);// getter
			}
			end = System.currentTimeMillis();
			System.out.println("ref:" + (end - start));

			//
			Class[] paramTypes = new Class[] {};
			Object[] args = new Object[] {};
			FastClass fc = FastClass.create(AccountVO.class);
			FastMethod read = fc.getMethod("getName", paramTypes);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				read.invoke(vo, args);// getter
			}
			end = System.currentTimeMillis();
			System.out.println("cglib:" + (end - start));

			// mybatis reflection
			MetaObject object = SystemMetaObject.forObject(vo);
			//
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				Object tmp = object.getValue("code_account");//getter
			}
			end = System.currentTimeMillis();
			System.out.println("mybatis ref:" + (end - start));

			//asm style
			IValueObjectWrapper<AccountVO> wrapper = EntityWrapperHelper.wrapper(AccountVO.class);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				String code_account = (String)wrapper.get(vo,"code_account");//getter
			}
			end = System.currentTimeMillis();
			System.out.println("asm style:" + (end - start));
			
			//json object
			JSONObject jo = new JSONObject();
			jo.put("code_account", "new_value");
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				Object obj = jo.get("code_account");
			}
			end = System.currentTimeMillis();
			System.out.println("jsonobject style(get):" + (end - start));
			//
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				String obj = jo.getString("code_account");
			}
			end = System.currentTimeMillis();
			System.out.println("jsonobject style(getString):" + (end - start));
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
	@Test
	public void performanceForWrite() {
		try {
			AccountVO vo = new AccountVO();
			long start = System.currentTimeMillis();
			for (int i = 0, len = 0; i < LOOP; i++) {
				vo.code_account = "test";// setter
				VOHelper.cast(String.class, vo.code_account);//因为asm中有赋值的保护，所以，把其他的也补偿一个，以作为对比
			}
			long end = System.currentTimeMillis();
			System.out.println("getter/setter:" + (end - start));// 性能基线

			Field f = vo.getClass().getDeclaredField("code_account");
			f.setAccessible(true);// 加上确实快一些
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				f.set(vo, "test");// setter
				VOHelper.cast(String.class, vo.code_account);//因为asm中有赋值的保护，所以，把其他的也补偿一个，以作为对比
			}
			end = System.currentTimeMillis();
			System.out.println("ref:" + (end - start));

			//
			Class[] paramTypes_write = new Class[] { String.class };
			Object[] args_write = new Object[] { "test" };
			FastClass fc = FastClass.create(AccountVO.class);
			FastMethod write = fc.getMethod("setName", paramTypes_write);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				write.invoke(vo, args_write);
				VOHelper.cast(String.class, vo.code_account);//因为asm中有赋值的保护，所以，把其他的也补偿一个，以作为对比
			}
			end = System.currentTimeMillis();
			System.out.println("cglib:" + (end - start));

			// mybatis reflection
			MetaObject object = SystemMetaObject.forObject(vo);
			//
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				String v = VOHelper.cast(String.class, "test_new");//因为asm中有赋值的保护，所以，把其他的也补偿一个，以作为对比
				object.setValue("code_account", v);//setter
			}
			end = System.currentTimeMillis();
			System.out.println("mybatis ref:" + (end - start));

			//asm style
			IValueObjectWrapper<AccountVO> wrapper = EntityWrapperHelper.wrapper(AccountVO.class);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				wrapper.set(vo, "code_account", "test_new");//setter
			}
			end = System.currentTimeMillis();
			System.out.println("asm style:" + (end - start));
			
			//json object
			JSONObject jo = new JSONObject();
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				jo.put("code_account","test_new");
			}
			end = System.currentTimeMillis();
			System.out.println("jsonobject style:" + (end - start));
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
	/**
	 * 经过测试可以发现，asm的读很快，写的话，因为有类型转化的问题
	 */
	@Test
	public void performance() {
		try {
			AccountVO vo = new AccountVO();
			long start = System.currentTimeMillis();
			for (int i = 0, len = 0; i < LOOP; i++) {
//				vo.code_account = "test";// setter
				String tmp = vo.code_account;// getter
			}
			long end = System.currentTimeMillis();
			System.out.println("getter/setter:" + (end - start));// 性能基线

			Field f = vo.getClass().getDeclaredField("code_account");
			f.setAccessible(true);// 加上确实快一些
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				Object value = f.get(vo);// getter
//				f.set(vo, "test");// setter
			}
			end = System.currentTimeMillis();
			System.out.println("ref:" + (end - start));

			//
			Class[] paramTypes = new Class[] {};
			Class[] paramTypes_write = new Class[] { String.class };
			Object[] args = new Object[] {};
			Object[] args_write = new Object[] { "test" };
			FastClass fc = FastClass.create(AccountVO.class);
			FastMethod read = fc.getMethod("getName", paramTypes);
			FastMethod write = fc.getMethod("setName", paramTypes_write);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				read.invoke(vo, args);// getter
//				write.invoke(vo, args_write);
			}
			end = System.currentTimeMillis();
			System.out.println("cglib:" + (end - start));

			// mybatis reflection
			MetaObject object = SystemMetaObject.forObject(vo);
			//
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				Object tmp = object.getValue("code_account");//getter
//				object.setValue("code_account", "test_new");//setter
			}
			end = System.currentTimeMillis();
			System.out.println("mybatis ref:" + (end - start));

			//asm style
			IValueObjectWrapper<AccountVO> wrapper = EntityWrapperHelper.wrapper(AccountVO.class);
			start = System.currentTimeMillis();
			for (int i = 0; i < LOOP; i++) {
				String code_account = (String)wrapper.get(vo,"code_account");//getter
//				wrapper.set(vo, "code_account", "test_new");//setter
			}
			end = System.currentTimeMillis();
			System.out.println("asm style:" + (end - start));
			//FIXME 需要增加pojo与json object之间读取数据的差异对比
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
}
