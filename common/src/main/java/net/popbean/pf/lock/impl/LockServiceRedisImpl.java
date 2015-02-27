package net.popbean.pf.lock.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.lock.LockService;
/**
 * 如果有问题就采用redlock的实现redisson
 * @author to0ld
 *
 */
@Service("service/pf/lock/redis")
public class LockServiceRedisImpl implements LockService {
	@Value("${is.dev}")
	protected boolean isDev = true;//是否为开发环境，应该分为prod/dev/test三个stage
	@Autowired
	@Qualifier("redisTemplate")
	protected RedisTemplate<String, Integer> operations;
	public static final String COLL_LOCK = "pb_lock";
	//
	@Override
	public void lock(String business_type, String lock_id) throws BusinessError {
		try {
			if(isDev){//开发状态就不用了吧
				return ;
			}
			//修改一下做法，改用
			long current_value = operations.opsForValue().increment(business_type+":"+lock_id, 1);
			if(current_value >1){
				ErrorBuilder.createBusiness().msg("正在处理，请稍后再试").execute();
			}
		} catch (Exception e) {
			ErrorBuilder.createBusiness().cause(e).execute();
		}
	}

	@Override
	public void unlock(String business_type, String lock_id) throws BusinessError {
		try {
			if(isDev){//开发状态就不用了吧
				return ;
			}
			//应该要针对返回结果进行判断，如果seed超标了，还是要抛出异常的
			operations.delete(business_type+":"+lock_id);
		} catch (Exception e) {
			ErrorBuilder.process(e);
		}
	}
}
