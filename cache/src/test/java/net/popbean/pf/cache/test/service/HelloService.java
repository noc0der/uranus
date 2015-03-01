package net.popbean.pf.cache.test.service;

import net.popbean.pf.cache.test.vo.HelloVO;

public interface HelloService {
	public int count();

	public String say();

	public void clean();

	public String put(Integer value);

	public HelloVO find(String pk)throws Exception;
}
