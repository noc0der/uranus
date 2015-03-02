package net.popbean.pf.mvc.bind.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.popbean.pf.security.vo.SecuritySession;

import org.apache.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 直接使用session处理用户登陆变量的信息
 * 
 * @author to0ld
 */
public class SessionArgumentResolver implements HandlerMethodArgumentResolver {
	protected static Logger log = Logger.getLogger("SERVICE");// 这个将来要换成别的logger
	public static final String TOKEN = "ui_session_token";
	//
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (SecuritySession.class.isAssignableFrom(parameter.getParameterType())) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory)throws Exception {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		SecuritySession env = getUISession(servletRequest);
		if (env != null) {
			String ip = webRequest.getHeader("x-forwarded-for");
			if (ip == null) {
				ip = servletRequest.getRemoteAddr();
			}
			env.ip = ip;
		}
		return env;
	}
	public static SecuritySession getUISession(HttpServletRequest request) {//FIXME 叫AccountEnv? AccountSession?
		HttpSession session = request.getSession(false);
		if (session != null) {
			return (SecuritySession) session.getAttribute(TOKEN);
		} else {
			return null;
		}
	}
}
