package net.popbean.pf.mvc.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.AbstractView;
/**
 * 暂时先放到session中，redis使用成熟后，放到redis的延时队列中
 * 可以考虑做一个统一的TokenInterceptor，允许每次访问创建，或每个登陆期间有效
 * 对handler进行缓存,这样能更快一些
 * @author to0ld
 *
 */
public class CSRFInterceptor extends HandlerInterceptorAdapter {


	public static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";
	@Value("${is.dev}")
	protected boolean isDev = true;//是否为开发环境，应该分为prod/dev/test三个stage

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (isDev) {
			return true;
		}
		//FIXME 可以定义一个AccessToken annotation如果该handler方法上定义有这个变量，那就生成
		// 目前仅针对POST请求
		if ("POST".equals(request.getMethod())) {
			// FIXME 应该使用微信认证，但项目在war包下，没有引入，暂时使用是否带微信认证的头来判断
			// 通过微信认证，允许(这是意外的小插曲，先这么放吧)
			String signature = request.getParameter("signature");  
	        String timestamp = request.getParameter("timestamp");  
	        String nonce = request.getParameter("nonce");
	        if (signature != null && timestamp != null && nonce != null){
	        	return true;
	        }
			// 比较csrftoken(目前暂时放到session中)
			String csrftoken = findToken(request);
			//
			if (!csrftoken.equals(request.getHeader(X_CSRF_TOKEN))) {
				invalidRequest("非法请求");
			}
		}
		return true;
	}
	/**
	 * 提取存储在后端的token数据
	 * 暂时从session中提取
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private String findToken(HttpServletRequest request)throws Exception{//someService.find();someService.save() -> tokenService
		HttpSession session = request.getSession(false);
		if (session == null) {
			invalidRequest("非法会话请求");
		}
		String csrftoken = (String) session.getAttribute(X_CSRF_TOKEN);
		if(StringUtils.isBlank(csrftoken)){
			invalidRequest("非法会话请求");
		}
		return csrftoken;
	}
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (isDev) {
			return;
		}

		// angularjs使用cookie: https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
		// 目前仅对模板引擎生效
		if (modelAndView != null) {//
			HttpSession session = request.getSession();
			String csrftoken;
			if (session.getAttribute(X_CSRF_TOKEN) == null) {
				csrftoken = genCSRFToken(session);
				session.setAttribute(X_CSRF_TOKEN, csrftoken);
			} else {
				csrftoken = (String) session.getAttribute(X_CSRF_TOKEN);
			}
			modelAndView.addObject(X_CSRF_TOKEN, csrftoken);
		}
	}

	private static final String CSRF_SECRET = "6195130a1c24bb5a4aa109a0813bff51";
	public static final String genCSRFToken(HttpSession session) {
		return HmacUtils.hmacSha1Hex(CSRF_SECRET, session.getId());
	}

	private static void invalidRequest(final String message) throws ModelAndViewDefiningException {
		throw new ModelAndViewDefiningException(new ModelAndView(new AbstractView() {
			@Override
			protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.addHeader("Content-Type",  "text/plain;charset=UTF-8");
				response.getWriter().write(message);
				response.flushBuffer();
			}
		}));
	}
}
