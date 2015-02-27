package testcase.entity;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Field;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSON;
/**
 * 有以下几个维度需要验证
 * - pojo to jsonobject
 * - pojo to db
 *  - pojo to mongo
 *  - pojo to redis
 * @author to0ld
 *
 */
public class EnumTestCase {
	@Test
	public void pojo2jsonobject(){
		try {
			//根据枚举值的字符串值获得枚举类型值
			//构建一个有enum的pojo
			TestVO inst = new TestVO();
			inst.pk_test = "pk_test_value_1";
			inst.domain = Domain.Ref;
			String content = JSON.toJSONString(inst);
			//如果字符串中包含了ref就算对吧
			Assert.assertTrue(content.indexOf("Ref")!=-1,"其实java的enum挺奇葩的");
			TestVO inst1 = JSON.parseObject(content,TestVO.class);
			Assert.assertTrue(Domain.Ref.equals(inst1.domain),"就是string的转化");
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
	public static class TestVO implements IValueObject{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3554408395180618172L;
		@Field(domain=Domain.PK,code="pk_test")
		public String pk_test;
		@Field(domain=Domain.Code)
		public Domain domain;
		
	}
}
