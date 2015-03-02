package net.popbean.pf.rule.service.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.popbean.pf.business.service.impl.AbstractBusinessService;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.rule.service.RuleBusinessService;
import net.popbean.pf.rule.service.RuleExpBizService;
import net.popbean.pf.rule.vo.RuleDetail;
import net.popbean.pf.security.vo.SecuritySession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
@Service("service/pf/rule")
public class RuleBusinessServiceImpl extends AbstractBusinessService implements RuleBusinessService {
	@Autowired
	private ApplicationContext appctx;
	//
	@Override
	public JSONObject eval(String rule_code, JSONObject context, SecuritySession client) throws BusinessError {
		JSONObject vo = new JSONObject();
		vo.put("condition",false);
		try {
			List<RuleDetail> rule_detail_list = fetchRuleDetailList(rule_code);
			//获得条件进行判断，是否能执行方法体
			return eval(rule_detail_list,context,client);
		} catch (Exception e) {
			processError(e);
		}
		return vo;
	}
	@Override
	public JSONObject eval(String code_rule, List<JSONObject> ctx_list, SecuritySession client) throws BusinessError {
		JSONObject vo = new JSONObject();
		List<RuleDetail> rule_detail_list = fetchRuleDetailList(code_rule);
		List<JSONObject> list = new ArrayList<JSONObject>(); 
		for(JSONObject ctx:ctx_list){
			JSONObject v = eval(rule_detail_list,ctx,client);
			list.add(v);
		}
		vo.put("data",list);
		return vo;
	}
	/**
	 * 
	 * @param ctx
	 * @param exp
	 * @param ret
	 * @return
	 * @throws Exception
	 */
	private  <T> T eval(JSONObject ctx,String exp,Class<T> ret)throws Exception{
		StandardEvaluationContext eval_ctx = new StandardEvaluationContext(ctx);
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp_inst = parser.parseExpression(exp);
		T tmp = exp_inst.getValue(eval_ctx,ret);		
		return tmp;
	}
	/**
	 * 根据编码找细则
	 * @param rule_code
	 * @return
	 * @throws BusinessError
	 */
	private List<RuleDetail> fetchRuleDetailList(String rule_code)throws BusinessError{
		try {
			//FIXME 需要启用Cache在获取model的时候会更快捷
			StringBuilder sql = new StringBuilder("select b.*,c.range_code,c.range_name,c.cvalue,c.memo ");
			sql.append(" from pb_pf_rule a left join pb_pf_rule_detail b on (a.pk_rule=b.pk_rule) ");
			sql.append(" left join pb_pf_ds_range c on (b.pk_cond = pk_ds_range) ");
			sql.append(" where a.code_rule=${code_rule} ");
			sql.append(" order by b.inum ");
			JSONObject param = new JSONObject();
			param.put("code_rule",rule_code);
			List<RuleDetail> rule_detail_list = _commondao.query(sql, param, RuleDetail.class);
			if(CollectionUtils.isEmpty(rule_detail_list)){
				ErrorBuilder.createBusiness().msg("没有找到code_rule="+rule_code+"的数据").execute();
			}
			return rule_detail_list;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}
	private JSONObject eval(List<RuleDetail> rule_detail_list,JSONObject context,SecuritySession client)throws BusinessError{
		JSONObject vo = new JSONObject();
		vo.put("condition",false);
		String exec_exp = null;
		String cond_exp = "";
		RuleDetail current_detail = null;
		try {
			if(CollectionUtils.isEmpty(rule_detail_list)){
				ErrorBuilder.createSys().msg("规则的细则为空，无法进行后续操作").execute();
			}
			int p = 0;
			for(RuleDetail detail:rule_detail_list){
				current_detail = detail;
				cond_exp = "";//确认清空
				if(!StringUtils.isBlank(detail.pk_cond)){//如果存在引用的，则优先使用引用的
					cond_exp = detail.memo;
					p++;
				}
				if(!StringUtils.isBlank(detail.cond_exp)){
					if(p!=0 && cond_exp.length()>0){
						cond_exp += " && ";
					}
					cond_exp += " ("+detail.cond_exp+")";
					p++;
				}
				boolean flag = true;
				if(!StringUtils.isBlank(cond_exp)){
					flag = eval(context,cond_exp,Boolean.class);
				}
				if(!flag){//不符合执行条件。。。算啥？
					continue;//要是大家都不满意我就throw exception
				}
				vo.put("condition",true);//确保是能找到数据的
				//FIXME 暂时不考虑参数不合格不合法的情况
				//寻找方法体，进行执行
				exec_exp = detail.exec_exp;//还是通俗一点，整bean.method吧，省得一地的类
				if(exec_exp.startsWith("spring://")){
					exec_exp = exec_exp.replaceAll("spring://", "");
					int pos = exec_exp.lastIndexOf("#");
					if(pos == -1){
						RuleExpBizService exp = (RuleExpBizService)appctx.getBean(exec_exp);
						Object ret = exp.evalBool(context, client);
						vo.put("data",ret);
					}else {//带参数的搞法
						Object bean = appctx.getBean(exec_exp.substring(0,pos));
						Method m = bean.getClass().getMethod(exec_exp.substring(pos+1), JSONObject.class,SecuritySession.class);
						Object ret = m.invoke(bean, new Object[]{context,client});
						vo.put("data",ret);
					}
					return vo;
				}else if(exec_exp.startsWith("sql://")){
					exec_exp = exec_exp.replaceAll("sql://", "");
					Object ret =  !_commondao.assertTrue(new StringBuilder(exec_exp), context,null);
					vo.put("data",ret);
					return vo;
				}else if(exec_exp.startsWith("expression://")){
					//FIXME 暂时先不引进
					exec_exp = exec_exp.replace("expression://", "");
					String rs = eval(context, exec_exp, String.class);
					//目前支持
					vo.put("data",rs);
				}else if(exec_exp.startsWith("return://")){
					exec_exp = exec_exp.replace("return://", "");
					//目前支持
					vo.put("data",exec_exp);
				}
				vo.put("cond_exp",cond_exp);
				vo.put("inum", detail.inum);
				return vo;
			}
			//获得条件进行判断，是否能执行方法体
		} catch (Exception e) {
			ErrorBuilder.createSys().cause(e).msg(JSON.toJSONString(current_detail)+"\n"+cond_exp+":"+JSON.toJSONString(context)).execute();
		}
		return vo;
	}
}
