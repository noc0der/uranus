package testcase.business;


import net.popbean.pf.business.service.CommonBusinessService;
import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.Field;
import net.popbean.pf.entity.helper.JO;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;
import net.popbean.pf.entity.service.EntityStructBusinessService;
import net.popbean.pf.entity.struct.impl.MysqlEntityStructImpl;
import net.popbean.pf.security.vo.SecuritySession;
import net.popbean.pf.testcase.TestHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 测试一下基础服务的可用性
 * - 建表
 * - CRUD
 * @author to0ld
 *
 */
@ContextConfiguration(locations={"classpath:/spring/app.test.xml"})
public class BusinessServiceTestCase extends AbstractTestNGSpringContextTests{
	@Autowired
	@Qualifier("service/pf/entity/struct")
	EntityStructBusinessService esService;
	@Autowired
	@Qualifier("service/pf/common")
	CommonBusinessService commonService;
	//
	/**
	 * 
	 * @return
	 */
	private SecuritySession mockLogin(){
		SecuritySession session = new SecuritySession();
		return session;
	}
	@Test
	public void test(){
		try {
			SecuritySession session = mockLogin();
			//建表
			esService.syncDbStruct(TestVO.class, session);
			//模拟数据
			TestVO inst = new TestVO();
			inst.test_code = "逗你玩还不行啊";
			//插入
			String pk_value = commonService.save(inst, null);
			Assert.assertNotNull(pk_value);
			//修改
			inst.pk_test = pk_value;
			inst.test_code = "new_value";
			commonService.save(inst, null);
			//查询
			StringBuilder sql = new StringBuilder("select * from pb_test where pk_test=${pk_test}");
			TestVO inst1 = commonService.find(sql, JO.gen("pk_test",pk_value), TestVO.class, session);
			Assert.assertNotNull(inst1);
			Assert.assertEquals(inst.test_code,inst1.test_code);
			//删除
			sql = new StringBuilder("delete from pb_test where pk_test=${pk_test}");
			commonService.executeChange(sql, JO.gen("pk_test",pk_value), session);
			//
			sql = new StringBuilder("select * from pb_test where pk_test=${pk_test}");
			inst1 = commonService.find(sql, JO.gen("pk_test",pk_value), TestVO.class, session);
			Assert.assertNull(inst1);
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e),e);
		}
	}
	@Test
	public void entityconvert(){
		try {
			//建表
			MysqlEntityStructImpl inst = new MysqlEntityStructImpl();
			EntityModel model = EntityModelHelper.build(TestVO.class);
			String create_sql = inst.create(model);
			System.out.println(create_sql);
			//修改表结构这个。。。以后再验证吧
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e),e);
		}
	}
	/**
	 * 
	 * @author to0ld
	 *
	 */
	@Entity(code="pb_test",name="就是测试呀")
	public static class TestVO implements IValueObject{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5903867680252760633L;
		@Field(domain=Domain.PK)
		public String pk_test;
		@Field(domain=Domain.Code)
		public String test_code;
	}
}
