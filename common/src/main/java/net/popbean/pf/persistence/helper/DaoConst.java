package net.popbean.pf.persistence.helper;

import java.io.Serializable;
import java.util.List;

import net.popbean.pf.entity.helper.JOHelper;

import com.alibaba.fastjson.JSONObject;

public class DaoConst {
	public static class Paging implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3691890889880957315L;
		public Integer currentPageNo = 1;//当前页数
		public Integer pageSize = 20;//页数
		public Integer totalCount;//总数
		public Integer totalPageCount;//总页数
		public Paging(){
			super();
		}
		public Paging(Integer currentPageNo){
			this(currentPageNo,20);
		}
		public Paging(Integer currentPageNo,Integer pageSize){
			super();
			this.currentPageNo = currentPageNo;
			this.pageSize = pageSize;
		}
		/**
		 * 从vo中提取分页信息
		 * @param param
		 * @return
		 */
		public static Paging build(JSONObject param){//应该谨慎使用
			Paging ret = new Paging();
			ret.pageSize = JOHelper.getIntValue(param, "pageSize",20);
			ret.currentPageNo = JOHelper.getIntValue(param, "pageNo", 1);
			if (ret.currentPageNo < 1) {
				ret.currentPageNo = 1;
			}
			ret.totalCount = JOHelper.getIntValue(param, "totalCount",0);
			ret.totalPageCount = JOHelper.getIntValue(param, "totalPageCount",1);
			return ret;
		}
		public static Paging buildByParam(JSONObject param){
			Paging ret = new Paging();
			ret.pageSize = param.getInteger("pageSize");
			ret.currentPageNo = param.getInteger("pageNo");
			//只要任意一项为空，就搞成空
			if(!JOHelper.has("pageSize", param) || !JOHelper.has("pageNo", param)){
				return null;
			}
			return ret;
		}
		/**
		 * 
		 * @param result
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static Paging buildByResult(Object result){
			JSONObject tmp = null;
			if (result instanceof JSONObject) {
//				Paging page = ((VO) result).getVO("paging");// 假定是：{paging:{...},data:[...]}结构
				tmp = ((JSONObject) result).getJSONObject("paging");
			} else if (result instanceof List && ((List<?>) result).size() > 0) {
				try {
					tmp = ((List<JSONObject>) result).get(0);
				} catch (ClassCastException e) {
				}
			}
			if (tmp != null) {
				return build(tmp);
			} else {
				return null;
			}
		}
	}
}
