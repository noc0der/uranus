package testcase.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidTestCase {
	@Test
	public void valid(){
		try {
			List<String> list = new ArrayList<String>();
			list.add("select 1 from tab_1 where 1=1 limit 1");
			list.add("SELECT 1 from tab_1 where 1=1 limit 1");
			String sql = "select 1 from tab_1 where 1=1 limit 1";
			boolean flag = CollectionUtils.contains(list.iterator(), sql);
			Assert.assertTrue(flag,"soso");
		} catch (Exception e) {
			Assert.fail("valid collections util", e);
		}
	}
}
