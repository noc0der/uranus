package net.popbean.pf.mvc.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 用于处理行为审计相关的内容
 * 提供user_agent，ip，访问标志
 * 以mdc的方式写入日志中
 * @author to0ld
 *
 */
public class TraceInterceptor extends HandlerInterceptorAdapter {
	public static final String IP = "ip";
	public static final String REQ_ID = "req_id";
	public static final String URI = "uri";
	public static final String USER_AGENT = "user_agent";
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		super.preHandle(request, response, handler);
		MDC.put(REQ_ID, UUID.randomUUID().toString());
		MDC.put(IP,getRemoteAddr(request));
		MDC.put(URI, request.getRequestURL().toString());
		String ua = request.getHeader("User-Agent");
		if(!StringUtils.isBlank(ua)){//如果没有ua，且无认证信息，应该认为是攻击
			MDC.put(USER_AGENT, request.getHeader("User-Agent"));//如果是从后台调用，不会有ua	
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
		MDC.remove(REQ_ID);
		MDC.remove(IP);
		MDC.remove(URI);
		MDC.remove(USER_AGENT);
	}
	/**
	 * 获取用户IP信息 如果是代理模式，就加上代理前的IP 因为X-Forwarded-For太容易伪造
	 * @param request
	 * @return
	 */
	public static String getRemoteAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)){
			ip+=",";
		}else{
			ip="";
		}
		ip += request.getRemoteAddr() + ":" + request.getRemotePort();
		return ip;
	}
}
