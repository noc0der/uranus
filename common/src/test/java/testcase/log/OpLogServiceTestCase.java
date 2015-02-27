package testcase.log;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpLogServiceTestCase {
	@Test
	public void test(){
		try {
			System.out.println(DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM"));
		} catch (Exception e) {
			Assert.fail("test format", e);
		}
	}
}
