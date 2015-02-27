package net.popbean.pf.business.service.impl;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.lock.LockService;
import net.popbean.pf.log.op.service.OpLogService;
import net.popbean.pf.persistence.impl.CommonDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
/**
 * 抽象的业务服务基类
 * @author to0ld
 *
 */
public abstract class AbstractBusinessService {
	@Autowired(required=true)
	@Qualifier("dao/pf/common")
	protected CommonDao _commondao;
	//FIXME 
	@Value("${is.dev}")
	protected boolean isDev = true;//是否为开发环境，应该分为prod/dev/test三个stage
	//
	@Autowired(required=true)
	@Qualifier("service/pf/lock/redis")
	protected LockService lockService;
	//
	@Autowired(required=true)
	@Qualifier("service/pf/log/op")
	protected OpLogService oplogService;
	//
	protected static Logger log = Logger.getLogger("SERVICE");
	//
	public <T extends IValueObject> T find(T vo,String tech_msg)throws BusinessError{
		try {
			T ret = _commondao.find(vo,tech_msg);
			return ret;
		} catch (Exception e) {//在这里怎么识别是业务异常还是系统异常呢,如果传进来的已经是businesserror就不管了
			ErrorBuilder.createBusiness().cause(e).execute();
		}
		return null;
	}
	/**
	 * 
	 * @param t
	 * @throws BusinessError
	 */
	public void processError(Throwable t)throws BusinessError{
		ErrorBuilder.createBusiness().cause(t).execute();
	}
	/**
	 * 
	 * @param msg
	 * @param t
	 * @throws BusinessError
	 */
	public void processError(String msg,Throwable t)throws BusinessError{
		ErrorBuilder.createBusiness().cause(t).msg(msg).execute();
	}
}
