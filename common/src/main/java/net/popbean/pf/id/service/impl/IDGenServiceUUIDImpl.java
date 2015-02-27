package net.popbean.pf.id.service.impl;

import java.util.List;

import net.popbean.pf.exception.BusinessError;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service("service/pf/id/uuid")
public class IDGenServiceUUIDImpl extends AbstractIDGenServiceUUIDImpl {
	//之所以没采用spring的idgen，是因为，那个更慢
	private String sep = "";

	protected String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuffer buf = new StringBuffer("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	protected String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuffer buf = new StringBuffer("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	@Override
	public String gen(JSONObject param) throws BusinessError {
		return new StringBuffer(36).append(format(getIP())).append(sep).append(format(getJVM())).append(sep).append(format(getHiTime())).append(sep)
				.append(format(getLoTime())).append(sep).append(format(getCount())).toString();
	}

	@Override
	public List<String> batch(JSONObject param, Integer len) throws BusinessError {
		throw new UnsupportedOperationException("还没实现");
	}
}
