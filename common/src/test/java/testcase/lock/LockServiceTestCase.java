package testcase.lock;


import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.lock.LockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations={"classpath:/spring/app.test.xml"})
public class LockServiceTestCase extends AbstractTestNGSpringContextTests {
	@Autowired
	@Qualifier("service/pf/lock/redis")
	LockService lockService;
	@Test
	public void repeatLock(){
		try {
			lockService.unlock("pb", "id_1");//不管之前锁了多少次，这一把解锁搞定所有历史问题
			lockService.lock("pb", "id_1");
			//正在处理，请稍后再试
		} catch (Exception e) {
			Assert.fail("try lock fail", e);
		}
		try {
			lockService.lock("pb", "id_1");
			//正在处理，请稍后再试
		} catch (Exception e) {
			if(e instanceof BusinessError){
				Assert.assertEquals(((BusinessError)e).msg, "正在处理，请稍后再试","要是能重复锁，也挺悲剧的，这就是为啥我不喜欢redisson的原因");
			}else{
				Assert.fail("repeat lock fail", e);	
			}
		}
	}
	@Test
	public void performance(){//大概是1万1秒，10万7.8秒左右的水平，高于sql的吞吐水平
		try {
			int loop = 100000;
			long start = System.currentTimeMillis();
			for(int i=0,len=loop;i<len;i++){
				lockService.lock("pb", "id_2");
				lockService.unlock("pb", "id_2");				
			}
			long end = System.currentTimeMillis();
			System.out.println("loop="+loop+":"+(end-start));
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}
}
