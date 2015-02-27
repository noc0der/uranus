package net.popbean.pf.exception;

import java.rmi.RemoteException;
/**
 * 业务约束；系统异常；
 * @author to0ld
 *
 */
public class BusinessError extends RemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1642671712443301700L;
	//
	public static final String MSG_NORMAL = "系统异常，请与运维人员联系";//FIXME 其实这么写是很不负责任的
	//
	private ErrorCate _cate = ErrorCate.SYS;//默认为系统异常
	public String msg;
	//
	public BusinessError(){
		super();
	}
	public BusinessError(ErrorCate cate){
		super();
		_cate = cate;
	}
	public BusinessError(ErrorCate cate,Throwable cause){
		super(cause.getMessage(),cause);
		_cate = cate;
	}
}
