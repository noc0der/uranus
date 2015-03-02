package net.popbean.pf.mvc.bind.support;

import org.apache.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonObjectArgumentResolver implements HandlerMethodArgumentResolver{
	protected static Logger log = Logger.getLogger("SERVICE");// 这个将来要换成别的logger
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		log.debug(parameter.getParameterName() + " : " + parameter.getParameterName()   
                + " \nParameterType: " + parameter.getParameterType() + " \nMethod: " + parameter.getMethod().getName());
		if (parameter.getParameterType().equals(JSONObject.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
			throws Exception {
		String value = webRequest.getParameter(parameter.getParameterName());
		JSONObject vo = JSON.parseObject(value);
		return vo;
	}
}
