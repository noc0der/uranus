package net.popbean.pf.entity.helper;

import com.alibaba.fastjson.JSONObject;

public class JO {
	/**
	 * 
	 * @param args
	 * @return
	 */
	public static JSONObject gen(Object... args){
		JSONObject ret = new JSONObject();
		for(int i=0,len=args.length;i<len;i+=2){
			if(args[i+1] == null){
				ret.remove(args[i].toString());
			}else{
				ret.put(args[i].toString(), args[i+1]);
			}
		}
		return ret;
	}
}
