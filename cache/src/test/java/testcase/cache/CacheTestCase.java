package testcase.cache;

import net.popbean.pf.business.service.CommonBusinessService;
import net.popbean.pf.cache.test.service.HelloService;
import net.popbean.pf.cache.test.vo.HelloVO;
import net.popbean.pf.entity.helper.JO;
import net.popbean.pf.entity.service.EntityStructBusinessService;
import net.popbean.pf.security.vo.SecuritySession;
import net.popbean.pf.testcase.TestHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * -[] mongo vs redis
 * -[] available
 * @author to0ld
 *
 */
@ContextConfiguration(locations={"classpath:/spring/app.test.xml"})
public class CacheTestCase extends AbstractTestNGSpringContextTests{
	//
	@Autowired
	@Qualifier("service/pf/entity/struct")
	EntityStructBusinessService esService;
	@Autowired
	@Qualifier("service/pf/common")
	CommonBusinessService commonService;
	//
	@Autowired
	@Qualifier("service/test/hello")
	HelloService helloService;
	//
	@Test
	public void available(){
		try {
			//留意一下hello service我只是做了一个示范
			//获取初始值，比较(初始应该是0)
			helloService.clean();//清理历史数据
			
			int count = helloService.count();
			Assert.assertEquals(count, 0);
			
			//第一次读取，比较
			String ret = helloService.say();
			count = helloService.count();
			Assert.assertEquals(ret, "say hello("+count+")");
			//重复读取，比较
			ret = helloService.say();
			Assert.assertEquals(ret, "say hello("+count+")");//跟上一次的数据进行对比
			
			//cache put
			count = 777;
			helloService.put(count);
			//
			ret = helloService.say();
			Assert.assertEquals(ret, "say hello("+count+")");
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e), e);
		}
	}
	@Test
	public void performance(){
		try {
			//留意一下hello service我只是做了一个示范
			//获取初始值，比较(初始应该是0)
			helloService.clean();//清理历史数据
			//10000次大概是1269毫秒
			int loop = 10000;
			String ret = null;
			long start = System.currentTimeMillis();
			for(int i=0;i<loop;i++){
				ret = helloService.say();	
			}
			long end = System.currentTimeMillis();
			System.out.println("get:"+(end-start));
			int count = helloService.count();
			Assert.assertEquals(ret, "say hello("+count+")");
			
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e), e);
		}
	}
	@Test
	public void perforcanceByDB(){
		SecuritySession session = null;
		try {
			//建表
			session = mockLogin();
			//建表
			esService.syncDbStruct(HelloVO.class, session);
			HelloVO inst = new HelloVO();
			//插入
			String pk_value = commonService.save(inst, null);
			Assert.assertNotNull(pk_value);
			//获取
			int loop = 10000;
			long start = System.currentTimeMillis();
			for(int i=0;i<loop;i++){
				HelloVO ret = helloService.find(pk_value);	
			}
			long end = System.currentTimeMillis();
			System.out.println("from cache:"+(end-start)+"-->");
			//
			StringBuilder sql = new StringBuilder("select * from pb_hello where pk_hello=${pk_hello}");
			start = System.currentTimeMillis();
			for(int i=0;i<loop;i++){
				HelloVO inst1 = commonService.find(sql, JO.gen("pk_hello",pk_value), HelloVO.class, session);
			}
			end = System.currentTimeMillis();
			System.out.println("from db:"+(end-start)+"-->");
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e), e);
		}finally{
			try {
				StringBuilder drop_sql = new StringBuilder("drop table pb_hello");
				commonService.executeChange(drop_sql, session);				
			} catch (Exception e2) {
				Assert.fail(TestHelper.getErrorMsg(e2), e2);
			}
		}
	}
	private SecuritySession mockLogin(){
		SecuritySession session = new SecuritySession();
		return session;
	}
}
