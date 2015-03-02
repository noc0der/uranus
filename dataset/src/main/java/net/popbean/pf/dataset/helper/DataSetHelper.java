package net.popbean.pf.dataset.helper;

import net.popbean.pf.security.vo.SecuritySession;

import com.alibaba.fastjson.JSONObject;

public class DataSetHelper {
	public static JSONObject mergeEnv(JSONObject param ,SecuritySession client){
		if(client == null){
			return param;
		}
		param.put("code_account",client.account_code);
		param.put("pk_account",client.account_id);
//		if(!AssertHelper.isNullorEmptyList(client.dept_list)){
//			param.put("LOGIN_PK_DEPT",client.dept_list.get(0).get("PK_DEPT"));	
//		}
		if(client.company != null){
			param.put("pk_company",client.company.company_id);	
		}
//		if(VOHelper.has("FILTER", param)){
//			param.put("FILTER","%"+param.get("FILTER")+"%");
//		}
		return param;
	}
}
