package testcase.entity;

import java.util.List;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.helper.ClassRefHelper;

import org.apache.commons.lang3.ClassUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import testcase.vo.AccountVO;

public class ClassRefTestCase {
	@Test
	public void isEntity(){
		try {
			Assert.assertTrue(ClassUtils.isAssignable(AccountVO.class,IValueObject.class), "account -> IvalueObject");
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
	/**
	 * 检测一下类扫描
	 * 其实更大的麻烦来自于web container环境或者osgi环境
	 */
	@Test
	public void scanEntity(){
		try {
			List<String> clazz_list = ClassRefHelper.scanEntity("net.popbean");
			Assert.assertNotNull(clazz_list,"不可能一个都没有啊");
		} catch (Exception e) {
			Assert.fail("scan", e);
		}
	}
	
}
