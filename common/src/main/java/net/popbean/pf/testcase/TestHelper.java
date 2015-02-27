package net.popbean.pf.testcase;

import net.popbean.pf.exception.BusinessError;

public class TestHelper {
	/**
	 * 这么做，只是为了避免在war中带入testng的包，或许这样做没有意义
	 * @param t
	 * @return
	 */
	public static String getErrorMsg(Throwable t){
		if(t instanceof BusinessError){
			return ((BusinessError)t).msg;
		}else{
			return t.getMessage();
		}
	}
}
