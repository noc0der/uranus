package testcase.asm.method;

import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.helper.EntityWrapperHelper;

import org.objectweb.asm.util.ASMifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import testcase.vo.AccountVO;

/**
 * 以accountvo为原型
 * 测试wrapper生成；缓存；使用
 * @author to0ld
 *
 */
public class EntityWrapperTestCase {
	@Test
	public void usage(){
		try {
			//[v]1-定义account(not entity model,just pojo like jpa v2)
			//1.1- testcase.vo.AccountVO
			AccountVO inst = new AccountVO();
			inst.code_account = "test";
			IValueObjectWrapper<AccountVO> wrapper = EntityWrapperHelper.wrapper(AccountVO.class);
			String code_account = (String)wrapper.get(inst,"code_account");//FIXME 感觉非常不好用的样子啊
			Assert.assertEquals(code_account, "test","soso");
			wrapper.set(inst, "code_account", "new_bee");
			Assert.assertEquals(inst.code_account, "new_bee");
			//2-定义wrapper（implements ivalueobjectwrapper)
			//[v] 2.1-IValueObjectWrapper
			//[x] 2.2- fake AccountxVOWrapper
			//3-定义vohelper.wrapper(account.class)
			//4-示范使用一个account instance<--  --> json object
			//4.1- wrapper.get(inst,key) --> sql struct那里可能会用到
			//4.2- wrapper.set(inst,key,value) -> resultset to pojo
			//4.3- performance (getter and setter)
		} catch (Exception e) {
			Assert.fail("usage", e);
		}
	}
	@Test
	public void gen(){//得到wrapper的样本
		try {
			ASMifier.main(new String[]{AccountVO.class.getName()});
		} catch (Exception e) {
			Assert.fail("gen class", e);
		}
	}
}
