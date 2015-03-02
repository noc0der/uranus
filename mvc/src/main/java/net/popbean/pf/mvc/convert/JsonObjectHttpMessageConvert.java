package net.popbean.pf.mvc.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.List;

import net.popbean.pf.entity.helper.JO;
import net.popbean.pf.log.helper.LogHelper;
import net.popbean.pf.mvc.vo.BusinessErrorModel;
import net.popbean.pf.persistence.helper.DaoConst.Paging;

import org.apache.log4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * 用于确保返回的数据，都是符合前端要求的 命名有些偏颇
 * @author to0ld
 */
public class JsonObjectHttpMessageConvert extends FastJsonHttpMessageConverter implements GenericHttpMessageConverter<Object> {

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		//为了支持跨域
//		outputMessage.getHeaders().add("Access-Control-Allow-Origin", "*");
//		outputMessage.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//		outputMessage.getHeaders().add("Access-Control-Max-Age", "3600");
//		outputMessage.getHeaders().add("Access-Control-Allow-Headers", "x-requested-with");
		//
		List<String> ref = outputMessage.getHeaders().get("Referer");
//		Object ip = LogHelper.getFromMDC("RemoteAddr");
		Object ip = MDC.get("ip");
		//如果是{data:[]|{},page:{}}格式
		//如果是{}格式
		//如果是[]格式
		//return:{data:[];page:{}}
		if (object != null) {
			JSONObject ret = new JSONObject();
			Paging page = Paging.buildByResult(object);
			if (page != null) {
				ret.put("page", page);
			}
			ret.put("data", object);
			ret.put("status", 1);
			if (object instanceof BusinessErrorModel) {
				ret.put("status", 0);
			}
			Object request_url = MDC.get("req_url");//FIXME 还是定义一个const吧
			ret.put("action",request_url);
			ret.put("ref",ref);
			if(ip!=null){
				ret.put("ip",ip);	
			}
			object = ret;// 为了满足前端组的需要
		}else{//满足前端关于没内容不行的要求
			object = JO.gen("status", 1,"ref",ref);
			if(ip!=null){
				((JSONObject)object).put("ip",ip);
			}
		}
		//-------
		super.writeInternal(object,outputMessage);
	}

	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		// 用于处理@RequestBody这种，详见GenericHttpMessageConverter
		return JSONObject.class.equals(type);
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		// 用于处理@RequestBody这种，详见GenericHttpMessageConverter
		String charset;
		HttpHeaders headers = inputMessage.getHeaders();
		long contentLength = headers.getContentLength();
		try {
			charset = headers.getContentType().getCharSet().name();
		} catch (RuntimeException e) {
			charset = "UTF-8";
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(contentLength >= 0 ? (int) contentLength : StreamUtils.BUFFER_SIZE);
		StreamUtils.copy(inputMessage.getBody(), bos);
		String body = new String(bos.toByteArray(), charset);
		// 只有对Content-Type: application/x-www-form-urlencoded才转义，可是你真是个form，也不能被转成VO吧...
		if (MediaType.APPLICATION_FORM_URLENCODED.includes(headers.getContentType())) {
			body = URLDecoder.decode(body);
		}
		return JSON.parse(body);
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		// 对于读任何东西，FastJsonHttpMessageConverter都会转成JSON，不是所期望的
//		return false;
		return JSONObject.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
	}
	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		if (JSONObject.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz)) {
			return true;
		}
		return super.canWrite(clazz, mediaType);
	}
}
