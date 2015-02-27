package net.popbean.pf.lock.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.lock.LockService;
/**
 * 
 * @author to0ld
 *
 */
@Service("service/pf/lock/mongo")
public class LockServiceMongoImpl implements LockService {
	@Value("${is.dev}")
	protected boolean isDev = true;//是否为开发环境，应该分为prod/dev/test三个stage
	@Autowired
	@Qualifier("mongoTemplate")
	protected MongoTemplate operations;
	public static final String COLL_LOCK = "popbean_lock";
	//
	@Override
	public void lock(String business_type, String lock_id) throws BusinessError {
		try {
			if(isDev){//开发状态就不用了吧
				return ;
			}
			//修改一下做法，改用
			Criteria c = Criteria.where("business").is(business_type).and("lock_id").is(lock_id);//使用bill_code+sn_len+yyyymmdd进行查询定位
			Update update = new Update().inc("seed", 1);//自增
			FindAndModifyOptions opt = FindAndModifyOptions.options().returnNew(true).upsert(true);
			JSONObject rs = operations.findAndModify(Query.query(c), update, opt,JSONObject.class,COLL_LOCK);
			Integer current_value = rs.getInteger("seed");//新的单据号值
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
			operations.remove(Query.query(Criteria.where("business").is(business_type).and("lock_id").is(lock_id)), COLL_LOCK);
		} catch (Exception e) {
			ErrorBuilder.process(e);
		}
	}
}
