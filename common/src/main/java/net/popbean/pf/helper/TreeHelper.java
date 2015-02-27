package net.popbean.pf.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.popbean.pf.entity.helper.FieldConst;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.log.prof.helper.ProfLogHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
public class TreeHelper {
	protected static Logger slog = Logger.getLogger("SERVICE");//这个将来要换成别的logger
	/**
	 * vo 与target的关系界定
	 * @param vo
	 * @param target
	 */
	private static int decition(JSONObject vo,JSONObject target,String pk_key,String seriescode){
		String series = vo.getString(seriescode);
		String pk = vo.getString(pk_key);
		String series_target = target.getString(seriescode);
		String pk_target = target.getString(pk_key);
		String[] list = null;
		if(series_target!=null){
			list = series_target.split("/");
			if(list[list.length-1].equals(pk)){//target的father是vo
				return 0; 
			}
		}
		if(series!=null){
			list = series.split("/");
			if(list[list.length-1].equals(pk_target)){//targetchild是vo
				return 1;
			}
		}
		return -1;
	}
	/**
	 * 构建tree
	 * @param list
	 * @param pk_key
	 * @param seriescode
	 * @return
	 * @throws Exception
	 */
	public static List<JSONObject> buildTree(List<JSONObject> list,String pk_key,final String seriescode)throws Exception{
		if(StringUtils.isBlank(seriescode)){
			return list;
		}
		JSONObject[] array = new JSONObject[]{};
		array = list.toArray(array);
		Arrays.sort(array, new Comparator<JSONObject>() {//调整成父-->子的结构
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String sc_v1 = o1.getString(seriescode);
				String sc_v2 = o2.getString(seriescode);
				if(StringUtils.isBlank(sc_v2)){
					return 0;
				}
				if(StringUtils.isBlank(sc_v1) && !StringUtils.isBlank(sc_v2)){
					return 1;
				}
				return sc_v1.compareToIgnoreCase(sc_v2);
			}
		});
		//
		//根据seriescode进行排序？
		//利用tree path的特性去找
		//构建一个map，记录key与位置的关系
		Map<String,Integer> map = new HashMap<String, Integer>();
		int pos = 0;
		String key = null;
		for(JSONObject v:array){
			key = v.getString(pk_key);
			map.put(key, pos++);
		}
		//
		pos = 0;
		String sc_v = null;
		JSONObject current = null;
		for(int i=array.length-1;i>=0;i--){
			current = array[i];
			if(current == null){
				continue;
			}
			sc_v = current.getString(seriescode);
			if(StringUtils.isBlank(sc_v)){//说明它本身就是一级节点，无需考虑了
				continue;
			}
			String[] sc_array = sc_v.split("/");//得到级次码的数组
			for(int ii=sc_array.length-1;ii>=0;ii--){
				String parent = sc_array[ii];
				Integer parent_pos = map.get(parent);
				if(parent_pos!=null && parent_pos!=-1){
					JSONObject parent_node = array[parent_pos];
					if(parent_node != null){
						List<JSONObject> child = JOHelper.ja2list(parent_node.getJSONArray("children"));
						if(child == null){
							child = new ArrayList<JSONObject>();
						}
						child.add(array[i]);
						parent_node.put("children",child);
						array[i] = null;//已经被收
						array[parent_pos] = parent_node;
					}
					break;//一旦被收拾，跳出循环
				}
			}
		}
		//清除没有值的数据
		List<JSONObject> ret = new ArrayList<JSONObject>();
		for(int i=array.length-1;i>=0;i--){
			if(array[i]!=null){
				ret.add(array[i]);
			}
		}
		return ret;
	}
	/**
	 * 利用级次码，将一个数组构建成一个树
	 * 需要时按照seriescode进行排序的
	 * @param list
	 * @param pk_key
	 * @param seriescode
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static List<JSONObject> build(List<JSONObject> list,String pk_key,String seriescode)throws Exception{
		ProfLogHelper.begin();
		boolean isChange = false;
		int father_pos = -1;//父节点的位置
		for(int i=0,len=list.size();i<len;){
			JSONObject inst = list.get(i);
			isChange = false;			
			for(int ii=(i+1);ii<len;){
				int rlt = decition(inst,list.get(ii),pk_key,seriescode);
				if(rlt == 1){//is father(即使找到父节点也要继续吃掉所有的子节点,当下只是记录位置)
					father_pos = ii;
					isChange = true;
					ii++;
					continue;
				}else if(rlt == 0){//is child
					List<JSONObject> child = JOHelper.ja2list(inst.getJSONArray("children"));
					if(child == null){
						child = new ArrayList<JSONObject>();
					}
					child.add(list.get(ii));
					inst.put("children",child);
					list.remove(ii);
					len = list.size();
				}else{//none
					ii++;
				}
			}
			if(isChange){
				List<JSONObject> child = JOHelper.ja2list(inst.getJSONArray("children"));
				if(child == null){
					child = new ArrayList<JSONObject>();
				}
				child.add(inst);
				
				list.get(father_pos).put("children",child);
				list.remove(i);
				len = list.size();//自己都被吃掉了，那就换吧
				father_pos = -1;
			}else{
				i++;
			}
		}
		ProfLogHelper.debug("build tree:"+list.size());
		return list;
	}
	/**
	 * @deprecated 这个操蛋的算法要重写
	 * 针对不知道第一层节点是什么的情况下使用,用于构建第一层树
	 * @param range_list
	 * @param pk_field
	 * @param seriesCode
	 * @return
	 * @throws Exception 
	 */
	public static List<JSONObject> convertToTree(List<JSONObject> range_list,String pk_field,String seriesCode) throws Exception{
		List<JSONObject> result = new ArrayList<JSONObject>();
		try {
			if(CollectionUtils.isEmpty(range_list)){
				return null;
			}
			if(StringUtils.isBlank(seriesCode)){
				seriesCode = FieldConst.SERIESCODE;
			}
			String shortStr = range_list.get(0).getString(seriesCode);
			JSONObject ret = range_list.get(0);
			for(JSONObject range:range_list){
				String seriesStr = range.getString(seriesCode);
				if (StringUtils.isBlank(seriesStr)) {
					result = convertToTree(range_list,null,pk_field,seriesCode);
					break;
				}
				if(seriesStr.length()<shortStr.length()){
					shortStr = seriesStr ;
					ret = range;
				}
			}
			result = convertToTree(range_list,shortStr,pk_field,seriesCode);
		} catch (Exception e) {
			slog.error(e);
			throw e;
		}
		return result;
	}
	/**
	 * 根据级次码去构建树状结构
	 * @param rangeList
	 * @param rootRefPK
	 * @param pkField
	 * @param seriesCode
	 * @return
	 * @throws Exception
	 */
	public static List<JSONObject> convertToTree(List<JSONObject> rangeList,String rootRefPK,String pkField,String seriesCode) throws Exception {
		if(StringUtils.isBlank(pkField)){
			ErrorBuilder.createSys().msg("pkField为空").execute();
		}
		List<JSONObject> result = new ArrayList<JSONObject>();
		try {
			if(StringUtils.isBlank(seriesCode)){//设定默认值
				seriesCode = FieldConst.SERIESCODE;
			}
			if (rangeList != null && rangeList.size() > 0) {
				if (StringUtils.isBlank(rootRefPK)) {// 当rootMetadataPK=null 或者rootMetadataPK="".
					for (JSONObject rangeVO1 : rangeList) {
						String seriesStr = rangeVO1.getString(seriesCode);
						if (StringUtils.isBlank(seriesStr)) {// 当节点的seriesCode为null.或者为""时代表为第一层节点
							String rangePK1 = rangeVO1.getString(pkField);
							List<JSONObject> childNode1 = convertToTree(rangeList, rangePK1, pkField,seriesCode);
							if (childNode1 != null && childNode1.size() > 0) {
								rangeVO1.put("children", childNode1);
							}
							result.add(rangeVO1);
						}
					}
				} else {// 当rootMetadataPK！=null 并且rootMetadataPK！="".
					for (JSONObject rangeVO2:rangeList) {
						String seriesStr = rangeVO2.getString(seriesCode);
						if (StringUtils.isBlank(seriesStr)) {
							continue;
						}
						String[] pkArray = seriesStr.split("/");
						String fatherPK = null;
						if (!ArrayUtils.isEmpty(pkArray)) {
							fatherPK = pkArray[pkArray.length - 1];
						}
						if (!StringUtils.isEmpty(fatherPK) && rootRefPK.equalsIgnoreCase(fatherPK)) {
							String rangePK2 = rangeVO2.getString(pkField);
							// 查询但前节点的子结点.
							List<JSONObject> childNode2 = convertToTree(rangeList, rangePK2,pkField, seriesCode);
							if (childNode2 != null && childNode2.size() > 0) {
								rangeVO2.put("children", childNode2);
							}
							result.add(rangeVO2);
						}
					}
				}
			}
		} catch (Exception e) {
			slog.error(e);
			throw new Exception("根据级次码去构建树状结构", e);
		}
		return result;
	}
}
