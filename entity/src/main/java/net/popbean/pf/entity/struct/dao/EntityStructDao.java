package net.popbean.pf.entity.struct.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.EntityType;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;
import net.popbean.pf.entity.struct.EntityStruct;
import net.popbean.pf.entity.struct.impl.MysqlEntityStructImpl;
import net.popbean.pf.entity.struct.impl.OracleEntityStructImpl;
import net.popbean.pf.persistence.helper.DaoHelper;
import net.popbean.pf.persistence.helper.DaoHelper.DbType;
import net.popbean.pf.persistence.impl.CommonDao;
@Repository("dao/pf/entity/schema")
public class EntityStructDao extends CommonDao {
	/**
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public List<String> compareStruct(EntityModel model) throws Exception {
		ResultSet rs_col = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		List<String> ret = new ArrayList<String>();
		try {// 其中有一段代码是冗余的(graphentitydetail)，需要进一步优化
			conn = getConn();
			EntityStruct convert = getTableMetaConvert(conn);
			//
			// String pk_field = null;
			DatabaseMetaData dmd = conn.getMetaData();

			String table_code = model.code.toUpperCase();
			String schema = DaoHelper.getSchema(conn);
			//
			// rs_pk = dmd.getPrimaryKeys(null, schema, table_code);
			// if(rs_pk.next()){
			// pk_field = rs_pk.getString("COLUMN_NAME").toUpperCase();
			// }
			// rs_pk.close();

			//
			Map<String, FieldModel> bus = new HashMap<>();
			List<FieldModel> tm_all_field = new ArrayList<>();
			for (FieldModel f : model.field_list) {
				tm_all_field.add(f);
				bus.put(f.code.toUpperCase(), f);

			}
			//
			// 获得现在的表结构
			List<FieldModel> fields = new ArrayList<FieldModel>();
			for (rs_col = dmd.getColumns(null, schema, table_code, null); rs_col.next();) {
				FieldModel f = EntityModelHelper.buildFieldModel(rs_col);
				fields.add(f);
				// 从新结构中，寻找当前表中的某个字段
				FieldModel newField = findField(tm_all_field, f);

				// 如果没有找到，要看是不是动态结构，如果是动态结构，则不予理会；如果不是，则要删除
				if (newField != null) {// 如果能在新的结构中找到，说明，顶多只有数据类型的变化
					// FIXME (目前不支持修改类型，因为涉及到数据的转化问题)
				} else {
					if (EntityType.Dynamic.equals(model.type)) {
						// FIXME 不需要处理，动态结构很有可能是实施阶段自己定义的
					} else {
						// 要进行删除的处理吧
						String temp = convert.alter(table_code, newField, f);
						if (temp.toUpperCase().indexOf(" DROP ") != -1) {// 目前还是先不要删除字段吧，多吓人啊
							continue;
						}
						if (!CollectionUtils.contains(ret.iterator(), temp)) {
							ret.add(temp);// 其实应该排除一下看看有无重复的
						}
					}
				}
			}
			//
			Iterator<Entry<String, FieldModel>> iter = bus.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, FieldModel> entry = iter.next();
				FieldModel f = entry.getValue();
				FieldModel temp = findField(fields, f);// temp == null then add
				String sql = convert.alter(table_code, f, temp);
				if (CollectionUtils.contains(ret.iterator(), sql)) {
					ret.add(sql);
				}
			}
			return ret;
			// 获得之后，需要双向对比，否则会有冗余的字段
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs_col != null && !rs_col.isClosed()) {
				rs_col.close();
			}
			after(null, stmt, conn);
		}
	}

	/**
	 * 获得转化器
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public EntityStruct getTableMetaConvert(Connection conn) throws Exception {
		EntityStruct ret = null;
		String jdbc_url = conn.getMetaData().getURL().toLowerCase();
		int pos = jdbc_url.indexOf("oracle");
		if (pos != -1) {
			ret = new OracleEntityStructImpl();
		}
		pos = jdbc_url.indexOf("mysql");// jtds
		if (pos != -1) {
			ret = new MysqlEntityStructImpl();
		}
		return ret;
	}

	/**
	 * 判断有误在list中存在
	 * 
	 * @param list
	 * @param field
	 * @return
	 * @throws Exception
	 */
	private FieldModel findField(List<FieldModel> list, FieldModel field) throws Exception {
		for (FieldModel f : list) {
			if (f.code.toLowerCase().equals(field.code.toLowerCase())) {
				return f;
			}
		}
		return null;
	}

	public String buildCreateTableSql(EntityModel tm) throws Exception {
		Connection conn = null;
		String create_sql = "";
		try {
			conn = getConn();
			EntityStruct convert = getTableMetaConvert(conn);
			create_sql = convert.create(tm);
			return create_sql;
		} catch (Exception e) {
			log.error(DaoHelper.parseMsg(create_sql, new Object[] {}));
			throw e;
		}
	}

	/**
	 * 判断一个表是不是空表
	 * 
	 * @param entity_code
	 * @return
	 * @throws Exception
	 */
	public boolean isEmpty(String entity_code) throws Exception {
		if (StringUtils.isBlank(entity_code)) {// 如果苗头不对，及时忽略
			return false;// 确保不会被执行删除？
		}
		// FIXME (如果是大表，其实limit 1会更快吧)
		StringBuilder sql = new StringBuilder("select count(1) RC from ").append(entity_code);
		List<JSONObject> list = query(sql, null);
		if (CollectionUtils.isEmpty(list)) {
			return true;
		}
		return false;
	}

	public List<JSONObject> fetchSchemeList() throws Exception {
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		List<JSONObject> ret = new ArrayList<JSONObject>();
		try {
			conn = getConn();
			DatabaseMetaData dmd = conn.getMetaData();
			for (rs = dmd.getSchemas(); rs.next();) {
				// {TABLE_SCHEM:xxxx}
				JSONObject single = new JSONObject();
				ResultSetMetaData rsmd = rs.getMetaData();
				single.put(rsmd.getColumnName(1), rs.getString(1));
				ret.add(single);
			}
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			after(rs, stmt, conn);
		}
	}

	public List<JSONObject> fetchEntityListBySchema(String schema) throws Exception {
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		List<JSONObject> ret = new ArrayList<JSONObject>();
		try {
			conn = getConn();
			String s = schema;
			DbType dbType = DaoHelper.getDbType(conn);
			if (StringUtils.isBlank(schema) && DbType.ORACLE.equals(dbType)) {
				s = conn.getMetaData().getUserName().toUpperCase();
			}
			DatabaseMetaData dmd = conn.getMetaData();
			for (rs = dmd.getTables(null, s, "%", null); rs.next();) {
				// {TABLE_SCHEM:xxxx}
				JSONObject single = new JSONObject();
				//
				single.put("TABLE_CAT", rs.getObject(1));
				single.put("TABLE_SCHEM", rs.getObject(2));
				single.put("TABLE_NAME", rs.getObject(3));
				single.put("TABLE_TYPE", rs.getObject(4));
				ret.add(single);
			}
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			after(rs, stmt, conn);
		}
	}

	public List<JSONObject> fetchIndex(String schema, String tab_code) throws Exception {
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		List<JSONObject> ret = new ArrayList<JSONObject>();
		try {
			conn = getConn();
			DatabaseMetaData dmd = conn.getMetaData();
			for (rs = dmd.getIndexInfo(null, schema, tab_code, false, false); rs.next();) {
				// {TABLE_SCHEM:xxxx}
				JSONObject single = new JSONObject();
				//
				single.put("TABLE_CAT", rs.getObject(1));
				single.put("TABLE_SCHEM", rs.getObject(2));
				single.put("TABLE_NAME", rs.getObject(3));
				single.put("NON_UNIQUE", rs.getObject(4));
				single.put("INDEX_QUALIFIER", rs.getObject(5));
				single.put("INDEX_NAME", rs.getObject(6));
				single.put("TYPE", rs.getObject(7));
				ret.add(single);
			}
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			after(rs, stmt, conn);
		}
	}
	/**
	 * 利用db的metadata获得指定entity的结构，并转化为entity model
	 * @param entity_code
	 * @return
	 * @throws Exception
	 */
	public EntityModel buildTableMetaByDbStruct(String entity_code)throws Exception{
		ResultSet rs_pk = null;
		ResultSet rs_col = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = getConn();
			//
			String pk_field = null;
			DatabaseMetaData dmd = conn.getMetaData();
			
			rs_pk = dmd.getPrimaryKeys(null, dmd.getUserName().toUpperCase(), entity_code.toUpperCase());
			if(rs_pk.next()){
				pk_field = rs_pk.getString("COLUMN_NAME").toUpperCase();
			}
			rs_pk.close();
			//
			EntityModel ret = new EntityModel();
			ret.code = entity_code;
			List<FieldModel> field_list = new ArrayList<>();
			for(rs_col = dmd.getColumns(null, dmd.getUserName().toUpperCase(), entity_code, null);rs_col.next();){
				FieldModel f = EntityModelHelper.buildFieldModel(rs_col);
				if(f.code.equals(pk_field)){
					f.type = Domain.PK;
					f.length = 60;
				}
				field_list.add(f);
			}
			rs_col.close();
			if(field_list.size()!=0){//这个判断有点畸形
				ret.field_list = field_list;
				return ret;
			}
			//
			return null;
		} catch (Exception e) {
			throw e;
		}finally{
			if(rs_pk!=null && !rs_pk.isClosed()){
				rs_pk.close();
			}
			if(rs_col!=null && !rs_col.isClosed()){
				rs_col.close();
			}
			after(null,stmt, conn);
		}
	}
	public void drop(String table_code,String col_code)throws Exception{
		if(StringUtils.isBlank(col_code)){
			StringBuilder sql = new StringBuilder("drop table ").append(table_code);
			executeChange(sql, null);
		}else{
			StringBuilder sql = new StringBuilder(" alter table "+table_code+" drop  column "+col_code+" ");
			executeChange(sql, null);
		}
	}
}
