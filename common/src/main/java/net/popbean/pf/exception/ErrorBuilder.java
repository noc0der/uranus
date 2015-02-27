package net.popbean.pf.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * 用于处理business error的相关事务
 * 稳定后倒是可以放到error中
 * @author to0ld
 *
 */
public class ErrorBuilder {
	public ErrorCate cate;
	public String msg;//用于扔到前台，但给技术看的信息
	public Throwable cause;
	//
	public static ErrorBuilder createSys(){
		return create(ErrorCate.SYS);
	}
	public static ErrorBuilder createBusiness(){
		return create(ErrorCate.BUSINESS);
	}
	/**
	 * 简化处理，直接调用
	 * @param t
	 */
	public static void process(Throwable t)throws BusinessError{
		createBusiness().cause(t).execute();
	}
	/**
	 * 
	 * @param cate
	 * @return
	 */
	private static ErrorBuilder create(ErrorCate cate){
		ErrorBuilder ret = new ErrorBuilder();
		ret.cate = cate;
		return ret;
	}
	public ErrorBuilder msg(String value){
		this.msg = value;
		return this;
	}
	public ErrorBuilder cause(Throwable cause){
		this.cause = cause;
		return this;
	}
	public void execute()throws BusinessError{
		BusinessError error = build();
		throw error;
	}
	/**
	 * 构建异常
	 * @return
	 */
	public BusinessError build(){
		BusinessError error = null;
		if(this.cause == null){
			error = new BusinessError(this.cate);	
		}else{
			if(this.cause instanceof BusinessError){
				error = (BusinessError)this.cause;
			}else{
				error = new BusinessError(this.cate,this.cause);	
			}
			
		}
		if(!StringUtils.isBlank(this.msg)){
			if(StringUtils.isBlank(error.msg)){
				error.msg = this.msg+"\n";	
			}else{
				error.msg = this.msg+"\n"+error.msg; 
			}
		}
		return error;
	}
}
