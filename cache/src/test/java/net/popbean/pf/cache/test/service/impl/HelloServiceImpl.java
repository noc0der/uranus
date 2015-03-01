package net.popbean.pf.cache.test.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import net.popbean.pf.business.service.impl.AbstractBusinessService;
import net.popbean.pf.cache.test.service.HelloService;
import net.popbean.pf.cache.test.vo.HelloVO;
import net.popbean.pf.exception.ErrorBuilder;
/**
 * 示范cache的使用
 * - Cacheable:存储数据
 * - CachePut:更新缓存数据
 * - CacheEvict:让cache失效
 * @author to0ld
 *
 */
@Service("service/test/hello")
public class HelloServiceImpl extends AbstractBusinessService implements HelloService {
	private int _count =0;
	@Override
	public int count() {
		return _count;
	}

	@Override
	@Cacheable(value="service/hello",key="'say'")
	public String say() {
		_count++;
		return "say hello("+_count+")";
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	@Override
	@CachePut(value="service/hello",key="'say'")
	public String put(Integer value){
		this._count = value;
		return "say hello("+value+")";
	}
	@Override
	@CacheEvict(value="service/hello",key="'say'")
	public void clean(){
		_count = 0;
	}

	@Override
	@Cacheable(value="service/hello",key="#pk")
	public HelloVO find(String pk)throws Exception {
		try {
			HelloVO vo = new HelloVO();
			vo.pk_hello = pk;
			HelloVO ret = _commondao.find(vo, null);
			return ret;
		} catch (Exception e) {
			ErrorBuilder.createSys().cause(e).execute();
		}
		return null;
	}
}
