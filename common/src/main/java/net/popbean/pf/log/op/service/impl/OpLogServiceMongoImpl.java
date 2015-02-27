package net.popbean.pf.log.op.service.impl;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.log.helper.LogConst;
import net.popbean.pf.log.op.service.OpLogService;
import net.popbean.pf.security.vo.SecuritySession;
@Service("service/pf/log/op")
public class OpLogServiceMongoImpl implements OpLogService {
	//
	@Autowired
	@Qualifier("mongoTemplate")
	protected MongoTemplate operations;
	public static final String COLL_OPLOG = "pb_op_log";
	//
	@Override
	public void log(String op_cate, JSONObject jo, SecuritySession session) throws BusinessError {
		try {
			JSONObject t = (JSONObject)jo.clone();
			t.put("op_cate", op_cate);//FIXME 变量命名，先后顺序不太好弄啊规则都是反着的
			//
			Object remote_addr = MDC.get(LogConst.REMOTE_ADDR);
			if(remote_addr != null){
				t.put("remote_addr",remote_addr);
			}
			Object x_req = MDC.get(LogConst.X_REQ_ID);
			if(x_req != null){
				t.put(LogConst.X_REQ_ID,x_req);
			}
			if(session!=null){//并入登陆信息
				if(session.company != null){
					t.put("pk_company", session.company.company_id);//有可能会出错
					t.put("code_company", session.company.company_code);
					t.put("name_company", session.company.company_name);
				}
				t.put("code_account", session.account_code);
				t.put("name_account", session.account_name);
				t.put("pk_account", session.account_id);
				if(StringUtils.isBlank(session.ip)){//如果为空就去取来自拦截器的
					t.put("ip",MDC.get(LogConst.REMOTE_ADDR));
				}else{
					t.put("ip", session.ip);
				}
				t.put("ts_crt", new Timestamp(System.currentTimeMillis()));
			}
			operations.save(t,getCollCode());//查询的时候，你就费点劲
		} catch (Exception e) {
			ErrorBuilder.process(e);
		}
	}
	private String getCollCode(){
		String yyyyMM = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMM");
		return COLL_OPLOG+"_"+yyyyMM;
	}
}
