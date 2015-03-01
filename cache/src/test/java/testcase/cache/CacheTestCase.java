package testcase.cache;

import net.popbean.pf.cache.test.service.HelloService;
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
}
