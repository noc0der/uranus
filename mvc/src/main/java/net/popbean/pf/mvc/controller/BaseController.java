package net.popbean.pf.mvc.controller;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorCate;
import net.popbean.pf.mvc.vo.BusinessErrorModel;

import org.apache.log4j.Logger;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 采用@ControllerAdvice可能会更好一些
 * @author to0ld
 *
 */
public class BaseController {
	protected static Logger log = Logger.getLogger("SERVICE");
	/**
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(BusinessError.class)
	@ResponseBody
	public BusinessErrorModel handleMyException(BusinessError ex) {
		BusinessErrorModel be = build(ex);
		log.error(JSON.toJSONString(be));
		return be;
	}
	protected BusinessErrorModel build(BusinessError ex){
		BusinessErrorModel be = new BusinessErrorModel();
		be.buz_error_msg = ex.msg;
		be.tech_error_msg = ex.getMessage()+"\n";
		StackTraceElement[] stes = null;
		if (ex.getCause() != null) {
			be.tech_error_msg += ex.getCause().getMessage();// 还是cause来吧
			stes = ex.getCause().getStackTrace();
		} else {
			stes = ex.getStackTrace();
		}
		List<String> ste_list = new ArrayList<String>();
		String clazz_name = ex.getClass().getName();
		if(ex.getCause()!=null){
			clazz_name += "->"+ex.getCause().getClass().getName();
		}
		ste_list.add(clazz_name);
		for (StackTraceElement ste : stes) {
			String msg = ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getLineNumber() + ")";
			ste_list.add(msg);
		}
		be.ste_list = ste_list;
		be.status = 0;
		return be;
	}
	/**
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public BusinessErrorModel handleThrowable(Throwable ex) {
		BusinessError b_ex = null;
		if(!(ex instanceof BusinessError)){
			b_ex = new BusinessError(ErrorCate.SYS, ex);
		}else{
			b_ex = (BusinessError)ex; 
		}
		return handleMyException(b_ex);
	}
	@InitBinder
	public void initBinder(WebDataBinder dataBinder){
		dataBinder.registerCustomEditor(JSONObject.class, new PropertyEditorSupport() {
			JSONObject value;
	        @Override
	        public JSONObject getValue() {
	            return value;
	        }

	        @Override
	        public void setAsText(String text) throws IllegalArgumentException {
	        	value = JSON.parseObject(text);
	        }
	    });
	}
}
