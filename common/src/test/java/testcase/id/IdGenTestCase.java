package testcase.id;

import net.popbean.pf.id.service.IDGenService;
import net.popbean.pf.id.service.impl.IDGenServiceUUIDImpl;

import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IdGenTestCase {
	@Test
	public void performance(){//spring idgen要比自己的idgen更慢
		try {
			int loop = 10000000;
			IdGenerator idgen = new AlternativeJdkIdGenerator();
			long start = System.currentTimeMillis();
			for(int i=0,len=loop;i<len;i++){
//				UUID uuid = idgen.generateId();
//				String tmp = uuid.toString().replaceAll("-", "");
				String tmp = idgen.generateId().toString();
				tmp = tmp.replaceAll("-", "");
			}
			long end = System.currentTimeMillis();
			System.out.println("spring id gen:"+(end-start));
			//
			IDGenService<String> inst = new IDGenServiceUUIDImpl();
			start = System.currentTimeMillis();
			for(int i=0,len=loop;i<len;i++){
				String tmp = inst.gen(null);
			}
			end = System.currentTimeMillis();
			System.out.println("custom id gen:"+(end-start));
		} catch (Exception e) {
			Assert.fail("performance", e);
		}
	}
}
