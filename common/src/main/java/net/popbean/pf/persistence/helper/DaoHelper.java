package net.popbean.pf.persistence.helper;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Struct;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.helper.EntityWrapperHelper;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.persistence.SQLStruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class DaoHelper {
	protected static Logger log = Logger.getLogger("SERVICE");// 这个将来要换成别的logger
	//
	/**
	 * 数据库类型
	 * @author to0ld
	 *
	 */
	public enum DbType{
		MYSQL,ORACLE,MSSQL
	}

	/**
	 * <pre>
	 * 为方便从ResultSet提取所需的数据服务
	 * </pre>
	 * 
	 * @author to0ld
	 * 
	 */
	public static class Rs {
		/**
		 * <pre>
		 * 遍历rs，将数据压制到vo中，并返回
		 * 应用场景：适用于自己拼写sql的情况
		 * 注意:有别名时的情况
		 * </pre>
		 * 
		 * @param rs
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public static JSONObject fetchJOFromResultSet(JSONObject vo, ResultSet rs) throws Exception {
			// 1-get rsmetadata
			ResultSetMetaData meta = rs.getMetaData();
			// 2-get map<String,Object>
			for (int i = 1, len = meta.getColumnCount(); i <= len; i++) {
				int col_type = meta.getColumnType(i);
				//在mysql的体系中meta.getColumnName(i)是原始的名字，无法拿到别名
//				String col_name = meta.getColumnName(i).toUpperCase();
				String col_name = meta.getColumnLabel(i).toUpperCase();
				if (col_type == Types.TIMESTAMP) {
					String cn = meta.getColumnTypeName(i).toUpperCase();
					if (cn.indexOf("TIMESTAMP") != -1 || cn.indexOf("DATETIME") != -1) {// oracle 11g的驱动会将date,timestamp都视为93 
						vo.put(col_name, rs.getTimestamp(i));
					} else {
//						if (log.isDebugEnabled()) {
//							log.debug("当前columntypename:"+cn+"-columnname:"+colName);//有可能会拿不到数据
//						}
						Date dt = rs.getDate(i);
						if (dt != null) {
							long l = dt.getTime();
							vo.put(col_name, new Date(l));
						}
					}
				} else if (col_type == Types.DATE) {
					vo.put(col_name, rs.getDate(i));
				} else if (col_type == Types.BOOLEAN) {
					vo.put(col_name, rs.getBoolean(i));
				} else if(col_type == Types.LONGVARBINARY){
					byte[] bytes = (byte[])rs.getObject(i);
//					vo.set(col_name,new String(bytes, "UTF-8"));
					if(bytes!=null){
						vo.put(col_name,new String(bytes));	
					}
				}else {
					vo.put(col_name, rs.getObject(i));
				}
			}
			return vo;
		}
		public static <T extends IValueObject> T fetchVOFromResultSet(Class<T> clazz, ResultSet rs) throws Exception {
			T ret = clazz.newInstance();
			IValueObjectWrapper<T> wrapper = EntityWrapperHelper.wrapper(clazz);
			// 1-get rsmetadata
			ResultSetMetaData meta = rs.getMetaData();
			// 2-get map<String,Object>
			for (int i = 1, len = meta.getColumnCount(); i <= len; i++) {
				int col_type = meta.getColumnType(i);
				//在mysql的体系中meta.getColumnName(i)是原始的名字，无法拿到别名
				String col_name = meta.getColumnLabel(i).toLowerCase();
				if (col_type == Types.TIMESTAMP) {
					String cn = meta.getColumnTypeName(i).toLowerCase();
					if (cn.indexOf("TIMESTAMP") != -1 || cn.indexOf("DATETIME") != -1) {// oracle 11g的驱动会将date,timestamp都视为93 
						wrapper.set(ret, col_name, rs.getTimestamp(i));//FIXME 有大小写的问题
					} else {
						Date dt = rs.getDate(i);
						if (dt != null) {
							long l = dt.getTime();
							wrapper.set(ret,col_name,new Date(l));
						}
					}
				} else if (col_type == Types.DATE) {
					wrapper.set(ret,col_name,rs.getDate(i));
				} else if (col_type == Types.BOOLEAN) {
					wrapper.set(ret,col_name,rs.getBoolean(i));
				} else if(col_type == Types.LONGVARBINARY){
					byte[] bytes = (byte[])rs.getObject(i);
//					vo.set(col_name,new String(bytes, "UTF-8"));
					if(bytes!=null){
						wrapper.set(ret,col_name,new String(bytes));
					}
				}else {
					wrapper.set(ret,col_name,rs.getObject(i));
				}
			}
			return ret;
		}

		/**
		 * 
		 * @param fields
		 * @param one
		 * @param rs
		 * @return
		 * @throws Exception
		 */
		public static JSONObject fetchJOFromResultSet(List<FieldModel> fields, JSONObject one, ResultSet rs) throws Exception {
			String[] keys = new String[fields.size()];
			for (int i = 0, len = fields.size(); i < len; i++) {
				keys[i] = fields.get(i).code;
			}
			return fetchJOFromResultSet(fields, one, rs, keys);
		}

		/**
		 * 
		 * @param fields
		 * @param rs
		 * @param keys
		 *            selectbuilder中定义的列名
		 * @return
		 * @throws Exception
		 */
		public static  JSONObject fetchJOFromResultSet(List<FieldModel> fields, JSONObject one, ResultSet rs, String[] keys) throws Exception {
			for (int i = 0, len = keys.length; i < len; i++) {
				FieldModel field = fields.get(i);
				if (field == null) {
					one.put(keys[i], rs.getObject(keys[i]));
				} else if (Domain.Clob.equals(field.type)) {
					Clob clob = rs.getClob(keys[i]);
					if (clob != null) {// 尚未经过测试,使用缓冲，确保效率
						one.put(keys[i], processLobColumn(clob));
					}
					//
				} else if (Domain.TimeStamp.equals(field.type)) {
					one.put(field.code, rs.getTimestamp(keys[i]));
				} else {
					Object value = rs.getObject(keys[i]);
					if (value != null) {// 如果为空 则不予处理
						one.put(field.code, value);
					}
				}
			}
			return one;
		}

		/**
		 * 处理lob类型字段
		 * 
		 * @param clob
		 * @return
		 * @throws Exception
		 */
		private static String processLobColumn(Clob clob) throws Exception {
			Reader reader = null;
			BufferedReader br = null;
			try {
				reader = clob.getCharacterStream();
				br = new BufferedReader(reader);
				StringBuilder sb = new StringBuilder();
				int icount = 0;
				char[] in_buffer = new char[5 * 1024];
				while ((icount = br.read(in_buffer)) != -1) {
					sb.append(in_buffer, 0, icount);
				}
				return String.valueOf(in_buffer);
			} catch (Exception e) {
				throw e;
			} finally {
				if (br != null) {
					br.close();
				}
				if (reader != null) {
					reader.close();
				}
			}
		}
	}

	public static class Sql {
		public static String replace(EntityModel model) throws Exception {
			String suffix = ") values(";
			String pref = " replace into " + model.code + "(";
			int pos = 0;
			List<FieldModel> field_list = model.field_list;
			Set<String> bus = new HashSet<>();
			for (FieldModel field : field_list) {
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
				for(String key:tmp){
					if(!bus.contains(key)){
						if (pos != 0) {
							pref += ",";
							suffix += ",";
						}
						pref += key;
						suffix += "${" + key + "}";
						bus.add(key);
						pos++;
					}
				}
			}
			return pref + suffix + " ) ";
		}
		public static String insert(EntityModel model) throws Exception {
			StringBuilder suffix = new StringBuilder(") values(");
			StringBuilder pref = new StringBuilder(" insert into " + model.code + "(");
//			List<String> list = new ArrayList<String>();
			int pos = 0;
			List<FieldModel> field_list =model.field_list;
			Set<String> bus = new HashSet<>();
			for (FieldModel field : field_list) {
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
				for(String key:tmp){
					if(!bus.contains(key)){
						if (pos != 0) {
							pref.append(",");
							suffix.append(",");
						}
						pref.append(key);
						suffix.append("${" + key + "}");
						bus.add(key);
						pos++;
					}
				}
			}
			return pref.append(suffix).append(")").toString();
		}

		/**
		 * 用于生成具有保护效果的sql
		 * 
		 * @param clazz
		 * @param guardFields
		 * @return
		 * @throws Exception
		 */
		public static String insert(Class<? extends IValueObject> clazz, FieldModel[] guardFields) throws Exception {
			EntityModel model = EntityModelHelper.build(clazz);
			return insert(model, guardFields);
		}

		public static String insert(EntityModel tm, FieldModel[] guardFields) throws Exception {
			if(ArrayUtils.isEmpty(guardFields)){
				return insert(tm);
			}
			// insert into table_code (...) select distinct ... from mt_pf_batch
			// where not exists ()
			// select 1 from table_code where guard_field
			StringBuilder pref = new StringBuilder(" insert into " + tm.code + "(");
			StringBuilder alter = new StringBuilder(" select distinct ");
			//
			int pos = 0;
			List<FieldModel> field_list = tm.field_list;
			Set<String> bus = new HashSet<>();
			for (FieldModel field : field_list) {
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
				for(String key:tmp){
					if(!bus.contains(key)){
						if (pos != 0) {
							pref.append(",");
							alter.append(",");
						}
						pref.append(key);
						alter.append("${").append(key).append("}");
						bus.add(key);
						pos++;
					}
				}
			}
			pref.append(")");
			alter.append(" from mt_pf_batch where not exists(");
			alter.append("select 1 from ").append(tm.code).append(" where 1=1 ");
			for (FieldModel f : guardFields) {
				alter.append(" and ").append(f.code).append("=${").append(f.code).append("} ");
			}
			alter.append(")");
			//
			return pref.append(alter).toString();
		}

		public static String select(EntityModel model) throws Exception {
			return select(model.field_list);
		}

		public static String select(EntityModel model, String pref) throws Exception {
			return select(model.field_list, pref);
		}

		public static String select(List<FieldModel> fields, String pref) throws Exception {
			StringBuilder sql = new StringBuilder(" select ");
			int pos = 0;
			Set<String> bus = new HashSet<>();
			for (FieldModel f : fields) {
				List<String> tmp = new ArrayList<>();
				tmp.add(f.code.toUpperCase());
				for(String key:tmp){
					if(!bus.contains(key)){
						if (pos++ != 0) {
							sql.append(",");
						}
						sql.append(pref).append(".").append(f.code);
					}
				}
			}
			return sql.toString();
		}

		public static String select(List<FieldModel> fields) throws Exception {
			StringBuilder sql = new StringBuilder(" select ");
			int pos = 0;
			Set<String> bus = new HashSet<>();
			for (FieldModel f : fields) {
				List<String> tmp = new ArrayList<>();
				tmp.add(f.code.toUpperCase());
				for(String key:tmp){
					if(!bus.contains(key)){
						if (pos != 0) {
							sql.append(",");
						}
						sql.append(key);
						bus.add(key);
						pos++;
					}
				}
			}
			return sql.toString();
		}

		/**
		 * update tablename set col1=${col1}...coln=${coln} where pk=${pk}
		 * 需要注意的是，该方法是全部罗列字段，如果vo中没有值，会直接在数据库中设置为空
		 * 
		 * @param clazz
		 * @return
		 * @throws Exception
		 */
		public static String update(Class<? extends IValueObject> clazz) throws Exception {
			EntityModel model = EntityModelHelper.build(clazz);
			return update(model);
		}
		/**
		 * 根据entity model拼凑一个完整的update set ... where pk_key=?的语句
		 * @param model
		 * @return
		 * @throws Exception
		 */
		private static String update(EntityModel model) throws Exception {
			StringBuilder sql = new StringBuilder(" update ");
			sql.append(model.code).append(" set ");
			Set<String> bus = new HashSet<>();
			FieldModel pk = model.findPK();
			int pos = 0;
			List<FieldModel> field_list = model.field_list;
			for (FieldModel field : field_list) {
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
				for(String key:tmp){
					if(!field.code.equals(pk.code) && !bus.contains(key)){
						if (pos++ != 0) {
							sql.append(",");
						}
						sql.append(key).append("=${").append(key).append("}");
						bus.add(key);
					}
				}
			}
			sql.append(" where ").append(pk.code).append("=${");
			sql.append(pk.code).append("}");
			//
			return sql.toString();
		}

		/**
		 * 
		 * @param model
		 * @param vo
		 * @param isNullClear
		 *            true:vo中没有value的key，在数据库中将被清空，否则将被忽略
		 * @return
		 * @throws Exception
		 */
		public static String update(EntityModel model, IValueObject vo,boolean isNullClear) throws Exception {
			IValueObjectWrapper wrapper = EntityWrapperHelper.wrapper(vo.getClass());
			
			StringBuilder sql = new StringBuilder(" update ");
			sql.append(model.code).append(" set ");
			FieldModel pk = model.findPK();
			int pos = 0;
			Set<String> bus = new HashSet<>();
			List<FieldModel> field_list = model.field_list; 
			for (FieldModel field : field_list) {
				if (field.equals(pk)) {
					continue;
				}
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
				Object v = wrapper.get(vo, field.code);
				if(v!=null){
//				if (JOHelper.has(field, vo)) {// 有值
					for(String k:tmp){
						if(!bus.contains(k)){
							if (pos != 0) {
								sql.append(",");
							}
							sql.append(k).append("=${").append(k).append("}");	
							bus.add(k);
							pos++;
						}							
					}					
				} else {// 没有值
					if (isNullClear) {// 空代表清空
						for(String k:tmp){
							if(!bus.contains(k)){
								if (pos != 0) {
									sql.append(",");
								}
								sql.append(k).append("=null");	
								bus.add(k);
								pos++;
							}							
						}
					}
				}
			}
			if (pos == 0) {
				ErrorBuilder.createSys().msg("没有可更新的字段，建议查看vo的数据").execute();
//				throw new BuzException("发生异常，无法进行后续处理", "没有可更新的字段，建议查看vo的数据");
			}
			sql.append(" where ").append(pk.code).append("=${");
			sql.append(pk.code).append("}");
			return sql.toString();
		}
		public static String update(EntityModel model, JSONObject vo,boolean isNullClear) throws Exception {
			
			StringBuilder sql = new StringBuilder(" update ");
			sql.append(model.code).append(" set ");
			FieldModel pk = model.findPK();
			int pos = 0;
			Set<String> bus = new HashSet<>();
			List<FieldModel> field_list = model.field_list; 
			for (FieldModel field : field_list) {
				if (field.equals(pk)) {
					continue;
				}
				List<String> tmp = new ArrayList<>();
				tmp.add(field.code.toUpperCase());
//				Object v = wrapper.get(vo, field.code);
				Object v = vo.get(field.code);
				if(v!=null){
//				if (JOHelper.has(field, vo)) {// 有值
					for(String k:tmp){
						if(!bus.contains(k)){
							if (pos != 0) {
								sql.append(",");
							}
							sql.append(k).append("=${").append(k).append("}");	
							bus.add(k);
							pos++;
						}							
					}					
				} else {// 没有值
					if (isNullClear) {// 空代表清空
						for(String k:tmp){
							if(!bus.contains(k)){
								if (pos != 0) {
									sql.append(",");
								}
								sql.append(k).append("=null");	
								bus.add(k);
								pos++;
							}							
						}
					}
				}
			}
			if (pos == 0) {
				ErrorBuilder.createSys().msg("没有可更新的字段，建议查看vo的数据").execute();
//				throw new BuzException("发生异常，无法进行后续处理", "没有可更新的字段，建议查看vo的数据");
			}
			sql.append(" where ").append(pk.code).append("=${");
			sql.append(pk.code).append("}");
			return sql.toString();
		}
		/**
		 * 拼凑 key in(${value_0},${value_1}...)
		 * 
		 * @param key
		 * @param len
		 *            如果len大于1000，应该自动给截成两段
		 * @return
		 */
		public static String in(String key, int len) throws Exception {
			if (len < 1 || len > 8192) {
				ErrorBuilder.createSys().msg("传入的待拼接字符长度设置不合理(" + len + ")").execute();
			}
			String ret = "in (";
			for (int i = 0; i < len; i++) {
				if (i != 0) {
					ret += ",";
				}
				ret += "${" + key + "_" + i + "}";
			}
			ret += ")";
			return ret;
		}

		/**
		 * 将param中的需要被in的key都罗列上
		 * 
		 * @param param
		 * @param pk_list
		 * @param pk_key
		 * @return
		 * @throws Exception
		 */
		public static <V> JSONObject in(JSONObject param, List<V> pk_list, String pk_key) throws Exception {
			if(CollectionUtils.isEmpty(pk_list)){
				return param;
			}
			for (int i = 0, len = pk_list.size(); i < len; i++) {
				if(pk_list.get(i) instanceof JSONObject){
					param.put(pk_key + "_" + i, ((JSONObject)pk_list.get(i)).get(pk_key));// PK_0,PK_1...
				}else{
					param.put(pk_key + "_" + i, pk_list.get(i));// PK_0,PK_1...	
				}
				
			}
			return param;
		}
		public static <V> JSONObject in(List<V> pk_list, String pk_key) throws Exception {
			return in(new JSONObject(),pk_list,pk_key);
		}
		public static StringBuilder delete(IValueObject vo) throws Exception {
			EntityModel model = EntityModelHelper.build(vo);
			return delete(model, vo);
		}
		/**
		 * delete from table_name where pk=${pk}
		 * @param model
		 * @param vo 只是为了获得分表的表名
		 * @return
		 * @throws Exception
		 */
		public static StringBuilder delete(EntityModel model, IValueObject vo) throws Exception {
			// 
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ").append(model.code);
			String pk = model.findPK().code;
			sb.append(" where ").append(pk).append("=${").append(pk).append("}");
			return sb;
		}

		/**
		 * <pre>
		 * 将sql mapp中注册的sql转化为native sql 
		 * 1-转义${} 
		 * 2-转移$[](第二期，支持嵌套)
		 * </pre>
		 * @param sql
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public static SQLStruct parseSQL(String sql, JSONObject vo) throws Exception {
			String sql_vendor = processConditionPart(sql, vo);
			SQLStruct ret = parseSQLNotconditon(sql_vendor,vo); 
			return ret;
		}
		/**
		 * 处理可选条件
		 * @param sql
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		private static String processConditionPart(String sql, JSONObject vo) throws Exception {
			// 首先去掉那些$[]
			StringBuilder buf = new StringBuilder(sql);
			String cond_pre = "$[";// 可选条件
			String prefix = "${";
			//
			int start_cond_index = buf.lastIndexOf(cond_pre);// 从尾部开始
			System.out.println();
			while (start_cond_index != -1) {
				int end_cond_index = findPlaceholderEndIndex(buf, start_cond_index, "$[", "]");
				if (end_cond_index != -1) {
					// 从这里得到三个变量start,end,condition，其中condition有可能是嵌套,假定嵌套必然有()
					// String condition = buf.substring(start_cond_index +cond_pre.length(), end_cond_index);
					// 从condition中提取${key}中的key，如果有key，vo中无值，则去掉这个condition
					String key = null;
					int start_param_index = buf.indexOf(prefix, start_cond_index);
					if (start_param_index != -1) {
						// 该方法考虑到嵌套的情况
						int end_param_index = findPlaceholderEndIndex(buf, start_param_index, "${", "}");
						if (end_param_index != -1) {
							key = buf.substring(start_param_index + prefix.length(), end_param_index);
							if (!StringUtils.isBlank(key)) {
								if (JOHelper.hasIgnoreCase(key, vo)) {// 如果存在数据，则保留
									buf = buf.replace(start_cond_index, start_cond_index + 2, "");// 之所以放上两个空格，是为了避免位置变化
									end_cond_index -= 2;
									buf = buf.replace(end_cond_index, end_cond_index + 1, "");// 之所以放上两个空格，是为了避免位置变化
								} else {// 放弃可选条件
									buf = buf.replace(start_cond_index, end_cond_index + 1, "");// 为preparedstatement而努力,干掉${c}
								}
							}
						} else {// 这说明连括号都不匹配
							throw new Exception("sql mapp编写的语句错误，没有找到与$[匹配的封闭符号(sql:" + sql + ")");
						}
					} else {// 如果没有参数。。。纯属你大爷的，没事加啥$[啊
						buf = buf.replace(start_cond_index, end_cond_index+1, "");
						//还原位置
//						buf.replace(start_cond_index, start_cond_index + 2, "  ");// 之所以放上两个空格，是为了避免位置变化
//						buf.replace(end_cond_index, end_cond_index + 1, " ");// 之所以放上两个空格，是为了避免位置变化
					}
					// start_cond_index =
					// buf.indexOf(cond_pre,start_cond_index+1);
					start_cond_index = buf.lastIndexOf(cond_pre);// 从尾部开始
				} else {// 如果没有找到封口，也是个错啊
					throw new Exception("sql mapp编写的语句错误，没有找到与$[匹配的封闭符号(sql:" + sql + ")");
				}
			}
			return buf.toString();
		}
		public static SQLStruct parseSQL(StringBuilder sql) throws Exception {
			return parseSQLNotconditon(sql.toString());
		}

		/**
		 * 将sql mapp注册的sql转化为native sql(可被preparedstatement执行的) - 支持$[ ]
		 * **/
		public static SQLStruct parseSQLNotconditon(String sql) throws Exception {
			SQLStruct ret = new SQLStruct();
			List<String> list = new ArrayList<String>();
			StringBuffer buf = new StringBuffer(sql);
			String prefix = "${";
			int startIndex = buf.indexOf(prefix);
			while (startIndex != -1) {
				int endIndex = findPlaceholderEndIndex(buf, startIndex, "${", "}");
				if (endIndex != -1) {
					// 给出变量
					String placeholder = buf.substring(startIndex + prefix.length(), endIndex);
					list.add(placeholder.toUpperCase());// 将其保留,大写保留之
					buf.replace(startIndex, endIndex + 1, "?");// 为preparedstatement而努力,干掉${c}
					if (endIndex + 1 < sql.length()) {
						// startIndex = endIndex+1;//指定新的位置，如果已经超标就直接出局
						int position = buf.indexOf(prefix);
						if (position == startIndex) {// 说明只有一个${}且没匹配上
							startIndex = -1;
						} else {
							startIndex = position;
						}

					} else {
						startIndex = -1;
					}
				} else {
					startIndex = -1;
				}
			}
			ret.nataiveSql = buf;
			ret.cols = list;// 如果没替换完，让sql去整错误信息
			ret.translated = true;
			return ret;
		}
		public static SQLStruct parseSQLNotconditon(String sql,JSONObject jo) throws Exception {
			SQLStruct ret = new SQLStruct();
			List<String> list = new ArrayList<String>();
			StringBuffer buf = new StringBuffer(sql);
			String prefix = "${";
			int startIndex = buf.indexOf(prefix);
			List<Object> params = new ArrayList<Object>();
			while (startIndex != -1) {
				int endIndex = findPlaceholderEndIndex(buf, startIndex, "${", "}");
				if (endIndex != -1) {
					// 给出变量
					String placeholder = buf.substring(startIndex + prefix.length(), endIndex);
					//如果sql中有，但是传入的参数没有，那也是个悲剧啊
					placeholder = placeholder.toLowerCase();
					if(StringUtils.isBlank(jo.getString(placeholder))){
						ErrorBuilder.createSys().msg("["+placeholder+"]为空，无法置换").execute();
					}
					list.add(placeholder);// 将其保留,小写保留之
					Object t = JOHelper.getObjectByIgnoreCase(jo, placeholder);//依然有可能为空
					if(t == null){
						ErrorBuilder.createSys().msg("传入的数据中没有key="+placeholder+"("+JSON.toJSONString(jo)+")").execute();//如果有必要就给出整个vo(toJsonString)
					}
					params.add(t);
					//
					buf.replace(startIndex, endIndex + 1, "?");// 为preparedstatement而努力,干掉${c}
					if (endIndex + 1 < sql.length()) {
						// startIndex = endIndex+1;//指定新的位置，如果已经超标就直接出局
						int position = buf.indexOf(prefix);
						if (position == startIndex) {// 说明只有一个${}且没匹配上
							startIndex = -1;
						} else {
							startIndex = position;
						}

					} else {
						startIndex = -1;
					}
				} else {
					startIndex = -1;
				}
			}
			ret.nataiveSql = buf;
			ret.cols = list;// 如果没替换完，让sql去整错误信息
			ret.values = params;
			ret.translated = true;
			return ret;
		}
		/**
		 * 
		 * @param buf
		 * @param startIndex
		 * @return
		 */
		public static int findPlaceholderEndIndex(CharSequence buf, int startIndex, String startpre, String endpre) {
			int index = startIndex + startpre.length();
			int withinNestedPlaceholder = 0;// 判断是否嵌套
			// String suffix = "}";
			while (index < buf.length()) {
				if (org.springframework.util.StringUtils.substringMatch(buf, index, endpre)) {
					if (withinNestedPlaceholder > 0) {
						withinNestedPlaceholder--;
						index = index + endpre.length();
					} else {
						return index;
					}
				} else if (org.springframework.util.StringUtils.substringMatch(buf, index, startpre)) {
					withinNestedPlaceholder++;
					index = index + startpre.length();
				} else {
					index++;
				}
			}
			return -1;
		}
	}

	public static class Stmt {
		/**
		 * 简单替换任意类型sql中的问号(不支持空值的替换)<br>
		 * 
		 * @param stmt
		 * @param values
		 * @throws Exception
		 */
		public static void evaluateSQL(PreparedStatement stmt, List<Object> values) throws Exception {
			evaluateSQL(stmt, values, 1);
		}

		public static void evaluateSQL(PreparedStatement stmt, List<Object> values, int pos) throws Exception {
			if(CollectionUtils.isEmpty(values)){
				return ;
			}
			for (int i = 0, len = values.size(); i < len; i++) {
				if(values.get(i) == null){
					throw new IllegalArgumentException("第[" + (i + 1) + "]个数值为空，无法执行变量替换");
				} else if (values.get(i) instanceof Timestamp) {
					stmt.setTimestamp(i + pos, (Timestamp) values.get(i));
				} else {
					stmt.setObject(i + pos, values.get(i));
				}
			}
		}

		public static void evaluateSQLWithNull(PreparedStatement stmt, List<Object> values) throws Exception {
			evaluateSQLWithNull(stmt, values, 1);
		}

		/**
		 * 如果其中的？遇到空值，则予以处理掉
		 * 
		 * @param stmt
		 * @param values
		 * @param pos
		 *            指定替换的起始位置
		 * @throws Exception
		 */
		public static void evaluateSQLWithNull(PreparedStatement stmt, List<Object> values, int pos) throws Exception {
			if(CollectionUtils.isEmpty(values)){
				return;
			}
			int type;
			for (int i = 0, len = values.size(); i < len; i++) {
				if (values.get(i) == null ) {
					// throw new Exception("第[" + (i + 1) + "]个数值为空，无法执行变量替换");
					// stmt.setObject(i+pos, Types.NULL);
					String dbalias = stmt.getConnection().getMetaData().getDatabaseProductName();
					if (dbalias.indexOf("Microsoft") > -1) {// mssql|sybase类型数据库其实均可
						type = stmt.getParameterMetaData().getParameterType(pos + i);
					} else {// oracle,mysql类型数据库
						type = Types.CHAR;// oracle|mysql驱动还不支持从数据库中取出字段的类型,碰到oracle就先用char对付,谁让它万能呢
					}
					stmt.setNull(i + pos, type);// 需要测试
				} else if (values.get(i) instanceof Timestamp) {
					stmt.setTimestamp(i + pos, (Timestamp) values.get(i));
//				}else if(values.get(i) instanceof String && values.get(i).toString().length()>4000){//假定这是blob类型的处理
//					stmt.setBytes(i+pos, values.get(i).toString().getBytes());//这种方式效率好像不行
				// 注意子类在前，父类在后
				} else if (values.get(i) instanceof Date){
				    stmt.setDate(i + pos, (Date)values.get(i));
				} else if (values.get(i) instanceof java.util.Date){
				    java.util.Date dt = (java.util.Date)values.get(i);
                    stmt.setDate(i + pos, new Date(dt.getTime()));
				} else {
					stmt.setObject(i + pos, values.get(i));
				}
			}
		}
	}


	/**
	 * 获取当前db conn的类型
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static DbType getDbType(Connection conn) throws Exception {
		String dbalias = conn.getMetaData().getDatabaseProductName().toUpperCase();
		// 根据数据库类型分发
		if (dbalias.indexOf("MICROSOFT") > -1) {
			return DbType.MSSQL;
//			return DAOConst.DBTYPE_MSSQL;
		}else if(dbalias.indexOf("MYSQL") > -1){
			return DbType.MYSQL;
		} else {
			return DbType.ORACLE;
//			return DAOConst.DBTYPE_ORACLE;
		}
	}

	public static String getSchema(Connection conn) throws Exception {
		DbType dbType = getDbType(conn);
		return DbType.MSSQL.equals(dbType) ? null : conn.getMetaData().getUserName().toUpperCase();
	}

	/**
	 * 判断是否是大字段类型<br>
	 * 
	 * @param field
	 * @return
	 */
	public static boolean isBlobOrClob(FieldModel field) {
		if (field == null) {
			return false;
		}
		return (field.type == Domain.Clob);
	}

	/**
	 * 分页查询时的起止范围<br>
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public static int[] fetchLimit(int pageNo, int pageSize) {
		int[] rets = new int[2];
		if (pageNo != -1 && pageSize != -1) {
			rets[0] = (pageNo - 1) * pageSize + 1;
			rets[1] = pageNo * pageSize;

		} else {
			rets[0] = 1;
			rets[1] = Integer.MAX_VALUE;
		}
		return rets;
	}

	/**
	 * 其实可以考虑直接输出调用堆栈
	 * 
	 * @return
	 */
	private static String buildStackTrace() {// 获得调用的堆栈
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		// String[] rets = new String[2];
		String ret = "";
		if (elements == null) {
			return ret;
		}
		//
		StringBuilder tmp = new StringBuilder();
		//如果是CommonBizServiceImpl，需要跃过中间的一层，到下面的调用中
		//如果是常规情况，需要到basedao的调用处
		for(int i=1,len=elements.length;i<len;i++){
			if(elements[i].getClassName().indexOf("net.popbean") == -1 || elements[i].getClassName().indexOf("$$") != -1){
				continue;
			}
			if(elements[i].getClassName().indexOf("net.popbean.persistence") == -1){//出了持久层处理了
				if(elements[i].getClassName().indexOf("CommonBizServiceImpl") != -1){
					continue;
				}else{
					if (i < len - 1 
							&& elements[i + 1].getClassName().indexOf("net.popbean") != -1
							&& elements[i + 1].getClassName().indexOf("$$") == -1) {// 还是只处理自己家的数据好了
						tmp.append("<").append(elements[i + 1].getClassName()).append(".").append(elements[i + 1].getMethodName()).append("(").append(elements[i + 1].getLineNumber()).append(")>.\n");
					}
					tmp.append("<").append(elements[i].getClassName()).append(".").append(elements[i].getMethodName()).append("(").append(elements[i].getLineNumber()).append(")>\n") ;
					break;					
				}
			}
		}
		//
		return tmp.toString();
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public static String parse(String sql,JSONObject jo,String def_str) throws Exception {
		if(StringUtils.isBlank(sql)){
			return null;
		}
		StringBuffer buf = new StringBuffer(sql);
		String prefix = "${";
		int startIndex = buf.indexOf(prefix);
		while (startIndex != -1) {
			int endIndex = DaoHelper.Sql.findPlaceholderEndIndex(buf, startIndex, "${", "}");
			if (endIndex != -1) {
				// 给出变量
				String placeholder = buf.substring(startIndex + prefix.length(), endIndex);
				if(JOHelper.has(placeholder.toUpperCase(), jo)){
					String value = jo.getString(placeholder.toUpperCase());
					buf.replace(startIndex, endIndex + 1, value);// 为preparedstatement而努力,干掉${c}	
				}else{
					buf.replace(startIndex, endIndex + 1, def_str);// 为preparedstatement而努力,干掉${c}
				}
				
				if (endIndex + 1 < sql.length()) {
					// startIndex = endIndex+1;//指定新的位置，如果已经超标就直接出局
					int position = buf.indexOf(prefix);
					if (position == startIndex) {// 说明只有一个${}且没匹配上
						startIndex = -1;
					} else {
						startIndex = position;
					}

				} else {
					startIndex = -1;
				}
			} else {
				startIndex = -1;
			}
		}
		return buf.toString();
	}
	/**
	 * 
	 * @param sql
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static String parseMsg(String sql, Object[] values) throws Exception {
		StringBuilder ret = new StringBuilder(buildStackTrace()).append(sql);
		for (Object value : values) {
			ret.append(value).append("@@");
		}
		return ret.toString();
	}

	public static String parseMsg(String sql, List<Object> values) throws Exception {
		StringBuilder ret = new StringBuilder(buildStackTrace()).append(sql);
		for (Object value : values) {
			ret.append(value).append("@@");
		}
		return ret.toString();
	}

	public static String parseMsg(StringBuilder sql, JSONObject vo) throws Exception {
		return parseMsg(sql.toString(), vo);
	}

	public static String parseMsg(String sql, JSONObject vo) throws Exception {
		if(StringUtils.isBlank(sql)){
			return sql;
		}
		Matcher m = Pattern.compile("\\$\\{(\\w+)(#)*([([0-9][:][\u4e00-\u9fa5][@])])*}").matcher(sql);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String key = m.group();
			key = key.substring(2, key.length()-1);
			int pos = key.indexOf("#");
			String value = null;
			if(pos !=-1){
				String rangeset = key.substring(pos+1);
				key = key.substring(0,pos);
				String[] pairs = rangeset.split("@");
				Map<String,String> bus = new HashMap<>();
				for(String p:pairs){
					String[] p_list = p.split(":");
					bus.put(p_list[0], p_list[1]);
				}
				value = vo.getString(key);
				value = bus.get(value);
			}else{
				value = vo.getString(key);
			}
			if(!StringUtils.isBlank(value)){
				m.appendReplacement(sb, value);
			}else{
				m.appendReplacement(sb, "N/A");
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static String parseMsg(SQLStruct struct) throws Exception {
		if (struct == null) {
			ErrorBuilder.createSys().msg("传入的strut为空，需要检查传入条件").execute();
		}
		StringBuilder ret = new StringBuilder("call stack:\n").append(buildStackTrace()).append("execute sql:\n").append(struct.nataiveSql);
		ret.append("\n").append("param list:");
		for (Object value : struct.values) {
			ret.append(value).append("@@");
		}
		return ret.toString();
	}
}