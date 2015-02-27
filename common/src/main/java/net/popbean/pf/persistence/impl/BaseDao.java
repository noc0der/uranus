package net.popbean.pf.persistence.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.helper.EntityWrapperHelper;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.helper.VOHelper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.exception.ErrorBuilder;
import net.popbean.pf.id.service.IDGenService;
import net.popbean.pf.log.prof.helper.ProfLogHelper;
import net.popbean.pf.persistence.IDataAccessObject;
import net.popbean.pf.persistence.SQLStruct;
import net.popbean.pf.persistence.helper.DaoHelper;
import net.popbean.pf.persistence.helper.DaoConst.Paging;
import net.popbean.pf.persistence.helper.DaoHelper.DbType;
import net.popbean.pf.persistence.service.IDBConnectionProviderService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

public abstract class BaseDao<V> implements IDataAccessObject<V> {
	protected static Logger log = Logger.getLogger("SERVICE");
	// @Autowired
	// @Qualifier("pf.idgen.service.uuid")
	// private IDGenService<V> _idGen;// 注入主键生成器
	// @Autowired
	// @Qualifier("pf.dbprovider.service.spring")
	// private IDBConnectionProviderService _dbConnProviderService;
	protected IDGenService<V> _idGen;// 注入主键生成器
	protected IDBConnectionProviderService _dbConnProviderService;

	//
	/**
	 * 判断指定的表是否存在
	 * 
	 * @param table_code
	 *            表名(估计要大写才行)
	 * @return
	 * @throws Exception
	 */
	public boolean isExists(String table_code, String col_code) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			//FIXME 其实select 1 from table_code where 1=1 limit 1 就可以了
			ProfLogHelper.begin();
			conn = getConn();
			//
			DatabaseMetaData dmd = conn.getMetaData();
			// 需要注意的是mssql 与 oracle是有差别的
			String schema = DaoHelper.getSchema(conn);
			if (StringUtils.isBlank(col_code)) {
				rs = dmd.getTables(null, schema, table_code.toUpperCase(), null);
			} else {
				rs = dmd.getColumns(null, dmd.getUserName().toUpperCase(), table_code, col_code.toUpperCase());
			}
			boolean flag = rs.next();
			ProfLogHelper.debug(" isExtists(" + table_code + ")=" + flag);
			return flag;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(rs, stmt, conn);
			throw e;
		} finally {
			after(rs, stmt, conn);
		}
	}

	public V save(IValueObject vo)throws Exception{
		return save(vo,false,null);
	}

	public V save(IValueObject vo, boolean allowClearNull, V pk_value) throws Exception {
		return save(vo, true, allowClearNull, pk_value);
	}
	public V save(IValueObject vo, boolean forceValidate, boolean allowClearNull, V pk_value) throws Exception {
		//FIXME 
//		EntityModel model = entityService.findModel(vo);//FIXME 采用findModel(String code)初期只需要代码里写好就行
		EntityModel model = EntityModelHelper.build(vo);
		JSONObject jo = JOHelper.vo2jo(vo);
		if (forceValidate) {
			VOHelper.validate(model, vo);
		}
		V pk = null;
		StringBuilder sql = new StringBuilder();
		FieldModel pkField = model.findPK();
		
		// 如果包含主键，则认定为修改,且传入的pk_value为空
		V value = (V)jo.get(pkField.code);
		if(value!=null && !StringUtils.isBlank(value.toString()) && (pk_value == null || StringUtils.isBlank(pk_value.toString()) ) ){
			pk = (V)jo.get(pkField.code);
			sql.append(DaoHelper.Sql.update(model, jo,allowClearNull));
		} else {
			if (pkField != null) {
				if (pk_value == null || StringUtils.isBlank(pk_value.toString())) {
					pk = genId();
				} else {
					pk = pk_value;
				}
				jo.put(pkField.code,pk);
			}
			sql.append(DaoHelper.Sql.insert(model, null));
		}
		jo = fixNumber(model.field_list, jo);//FIXME 应该引申为补齐默认值为宜
		executeChange(sql, jo);
		return pk;
	}
	/**
	 * 强制把零补上
	 * 
	 * @param jo
	 * @return
	 */
	protected JSONObject fixNumber(List<FieldModel> f_list, JSONObject jo) throws Exception {
		for (int i = 0, len = f_list.size(); i < len; i++) {
			FieldModel model = f_list.get(i);
			if(model.type.equals(Domain.Stat) || model.type.equals(Domain.Money) || model.type.equals(Domain.Int)){
				if (!JOHelper.has(model.code, jo) || StringUtils.isBlank(jo.getString(model.code))) {
					if(model.type.equals(Domain.Stat) || model.type.equals(Domain.Int)){
						jo.put(model.code, TypeUtils.castToInt(model.defaultValue));// 确保填补默认值
					}else if(model.type.equals(Domain.Money)){
						jo.put(model.code, TypeUtils.castToBigDecimal(model.defaultValue));// 确保填补默认值
					}else{
						jo.put(model.code, model.defaultValue);// 确保填补默认值	
					}
					
				}
			}
		}
		return jo;
	}
	protected <T extends IValueObject> T fixNumber(List<FieldModel> f_list, T vo) throws Exception {
		IValueObjectWrapper<T> wrapper = (IValueObjectWrapper<T>)EntityWrapperHelper.wrapper(vo.getClass());
		for (int i = 0, len = f_list.size(); i < len; i++) {
			FieldModel model = f_list.get(i);
			if(model.type.equals(Domain.Stat) || model.type.equals(Domain.Money) || model.type.equals(Domain.Int)){
				Object value = wrapper.get(vo, model.code);
				if(value == null || StringUtils.isBlank(value.toString())){
					if(model.type.equals(Domain.Stat) || model.type.equals(Domain.Int)){
						wrapper.set(vo,model.code,TypeUtils.castToInt(model.defaultValue));// 确保填补默认值
					}else if(model.type.equals(Domain.Money)){
						wrapper.set(vo,model.code,TypeUtils.castToBigDecimal(model.defaultValue));// 确保填补默认值
					}else{
						wrapper.set(vo,model.code,model.defaultValue);
					}
				}
			}
		}
		return vo;
	}
	/**
	 * 适用于delete from xx where pk_key=?
	 */
	@Override
	public int delete(IValueObject vo) throws Exception {
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.delete(vo));
		JSONObject jo = JOHelper.vo2jo(vo);
		return executeChange(sql, jo);
	}

	/**
	 * delete from xx where pk_key in (...)
	 * @param clazz
	 * @param pkList
	 * @return
	 * @throws Exception
	 */
	public int delete(Class<IValueObject> clazz, List<V> pkList) throws Exception {
		if (CollectionUtils.isEmpty(pkList)) {
			return -1;
		}
		EntityModel model = EntityModelHelper.build(clazz);
		FieldModel pk = model.findPK();
		StringBuilder sql = new StringBuilder("delete from ");
		sql.append(model.code);
		sql.append(" where ");
		sql.append(pk.code);
		sql.append(" ");
		sql.append(DaoHelper.Sql.in(pk.code, pkList.size()));
		JSONObject vo = new JSONObject();
		vo = DaoHelper.Sql.in(vo, pkList, pk.code);
		return executeChange(sql, vo);
	}

	@Override
	public List<JSONObject> query(StringBuilder sql, JSONObject param) throws Exception {
		Connection conn = getConn();
		// FIXME 为了确保排除空串
		param = JOHelper.cleanEmptyStr(param);
		return processQuery(conn, sql, param);
	}
	public <T extends IValueObject> List<T> query(StringBuilder sql, JSONObject param,Class<T> clazz) throws Exception {
		Connection conn = getConn();
		// FIXME 为了确保排除空串
		param = JOHelper.cleanEmptyStr(param);
		return processQuery(conn, sql, param,clazz);
	}
	/**
	 * 
	 * @param param
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public <T extends IValueObject> List<T> query(JSONObject param,Class<T> clazz) throws Exception {
		Connection conn = getConn();
		// FIXME 为了确保排除空串
		param = JOHelper.cleanEmptyStr(param);//这种对于key is null的剧情就歇菜了
		EntityModel model = EntityModelHelper.build(clazz);
		Set<String> bus = new HashSet<>();
		for(FieldModel fm:model.field_list){
			bus.add(fm.code);
		}
		//select * from xx where 1=1 
		StringBuilder sql = new StringBuilder(" select * from ").append(model.code).append(" ");
		sql.append(" where 1=1 ");
		Iterator<String> iter = param.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			if(bus.contains(key)){
				sql.append(" and ").append(key).append("=${"+key+"}");	
			}
		}
		return processQuery(conn, sql, param,clazz);
	}
	/**
	 * 
	 * @param sql
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public JSONObject find(StringBuilder sql, JSONObject jo) throws Exception {
		DbType type = DaoHelper.getDbType(getConn());
		StringBuilder sql_final = new StringBuilder().append(sql);
		if (type.equals(DbType.MYSQL)) {
			//FIXME 如果传入的是[select ....limit 1 ] [select ... limit 1]，需要拦截
			sql_final.append(" limit 1 ");//要是哪位大哥传进来的sql自己带了limit 1那就歇菜了估计最好还是保护一下为宜
		} else if (type.equals(DbType.ORACLE)) {// select * from (sql) where rownum<=1
			sql_final = new StringBuilder("select * from (").append(sql).append(") where rownum<=1");
		}

		List<JSONObject> list = query(sql_final, jo);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * 
	 * @param sql
	 * @param vo
	 * @param tech_msg
	 * @return
	 * @throws Exception
	 */
	public JSONObject find(StringBuilder sql, JSONObject vo, String tech_msg) throws Exception {
		return find(sql, vo, BusinessError.MSG_NORMAL, tech_msg);
	}
	

	public JSONObject find(StringBuilder sql, JSONObject vo, String buz_msg, String tech_msg) throws Exception {
		StringBuilder sql_final = new StringBuilder(sql);// 复制一份出来
		sql_final = sql_final.append(" limit 1 ");
		List<JSONObject> list = query(sql, vo);
		if (CollectionUtils.isEmpty(list)) {
			if (StringUtils.isBlank(tech_msg)) {
				return null;
			} else {
				ErrorBuilder.createSys().msg(tech_msg).execute();
			}
		}
		return list.get(0);
	}

	/**
	 * 该方法主要用于动态结构 需要注意的是这个方法不支持peroidtablemeta
	 * select * from xx where pk_key=?
	 * @param vo
	 * @param tech_msg
	 * @return
	 * @throws Exception
	 */
	public <T extends IValueObject> T find(T vo, String tech_msg) throws Exception {
		EntityModel model = EntityModelHelper.build(vo);
		FieldModel pk_field = model.findPK();
		IValueObjectWrapper wrapper = EntityWrapperHelper.wrapper(vo.getClass());
		JSONObject param = new JSONObject();
		param.put(pk_field.code, wrapper.get(vo, pk_field.code));
		//
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.select(model));
		sql.append(" from ").append(model.code);
		sql.append(" where ").append(pk_field.code).append("=${").append(pk_field.code).append("}");
		return find(sql,param,(Class<T>)vo.getClass(),tech_msg);
	}
	public <T extends IValueObject> T find(StringBuilder sql, JSONObject param,Class<T> clazz,String tech_msg) throws Exception {
		EntityModel model = EntityModelHelper.build(clazz);
		FieldModel pk_field = model.findPK();
		IValueObjectWrapper wrapper = EntityWrapperHelper.wrapper(clazz);
		//
		//FIXME 就查询条件而言，得支持
		StringBuilder sql_final = new StringBuilder(sql);// 复制一份出来
		sql_final = sql_final.append(" limit 1 ");
		List<T> list = query(sql_final, param, clazz);
		if (CollectionUtils.isEmpty(list)) {
			if (StringUtils.isBlank(tech_msg)) {
				return null;
			} else {
				ErrorBuilder.createSys().msg(tech_msg).execute();
			}
		}
		return list.get(0);
	}

	public List<JSONObject> paging(StringBuilder sql, JSONObject param, Paging paging) throws Exception {
		if (paging == null) {// 简单保护一下
			return query(sql, param);
		}
		return paging(sql, param, paging.currentPageNo, paging.pageSize);
	}

	@Override
	public List<JSONObject> paging(StringBuilder sql, JSONObject param, int pageNo, int pageSize) throws Exception {
		try {
			int start = 0;
			if (pageNo != -1 && pageSize != -1) {
				start = (pageNo - 1) * pageSize;
			}
			String sqlUpcase = sql.toString().toUpperCase();
			int pos = sqlUpcase.indexOf("FROM");
			// 获取 order by 的位置
			int pos2 = sqlUpcase.indexOf("ORDER BY");
			if (pos2 != -1) {
				while (sqlUpcase.substring(pos2).indexOf("#'#\"#") != -1) {
					pos2 = sqlUpcase.substring(pos2).indexOf("#'#\"#");
					pos2 = sqlUpcase.substring(pos2).indexOf("ORDER BY");
				}
			}
			StringBuilder count_sql = null;
			if (pos2 != -1) {
				count_sql = new StringBuilder("select count(1) as t_count ").append(sql.substring(pos, pos2));
			} else {
				count_sql = new StringBuilder("select count(1) as t_count ").append(sql.substring(pos));
			}
			JSONObject count = find(count_sql, param);
			if (JOHelper.isEmpty(count)) {
				return new ArrayList<>();
			}
			int total_count = count.getInteger("T_COUNT");
			StringBuilder sql_final = new StringBuilder().append(sql).append(" limit " + start + "," + pageSize + " ");
			List<JSONObject> list = query(sql_final, param);
			//
			if (list.size() > 0) {
				JSONObject one = list.get(0);
				if (pageSize <= 1) {
					pageSize = 20;
				}
				int totalPageCount = total_count / pageSize + (total_count % pageSize == 0 ? 0 : 1);
				one.put("pageNo", pageNo);
				one.put("totalPageCount", totalPageCount);
				one.put("totalCount", total_count);
				one.put("pageSize", pageSize);
			}
			return list;
		} catch (Exception e) {
			ErrorBuilder.createSys().cause(e).msg("[sql:" + sql + "][pageNo:" + pageNo + "][pageSize:" + pageSize + "]").execute();
		}
		return new ArrayList<JSONObject>();
	}
	/**
	 * 用于最终执行delete,insert,update的语句
	 */
	@Override
	public int executeChange(StringBuilder sql, JSONObject jo) throws Exception {
		Connection conn = getConn();
		int ret = processInsertAndUpdate(conn, sql, jo);
		return ret;
	}

	/**
	 * 适用于无参数无替换的场景
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public int executeChange(StringBuilder sql) throws Exception {
		PreparedStatement stmt = null;
		Connection conn = getConn();
		try {
			//
			ProfLogHelper.begin();
			stmt = conn.prepareStatement(sql.toString());
			int ret = stmt.executeUpdate();
			ProfLogHelper.debug(sql);
			return ret;
		} catch (Exception e) {
			after(null, stmt, conn);
			//ErrorBuilder.createSys(e).msg().fire(); --> ErrorBuilder.createSys(e).fire(msg);
			ErrorBuilder.createSys().cause(e).msg(DaoHelper.parseMsg(sql, null)).execute();
		} finally {
			after(null, stmt, conn);
		}
		return -1;
	}
	public void batch(List<String> sqls)throws Exception{
		if(CollectionUtils.isEmpty(sqls)){
			return ;
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			//
			conn = getConn();
			stmt = conn.createStatement();
			//
			for (String sql : sqls) {
				if(log.isDebugEnabled()){
					log.debug("("+sqls.size()+")"+sql);	
				}
				stmt.addBatch(sql);
			}
			if(sqls.size()>0){//保护
				stmt.executeBatch();
			}
		} catch (Exception e) {
			ErrorBuilder.createSys().cause(e).msg(JSON.toJSONString(sqls)).execute();
		} finally {
			after(null,stmt, conn);
		}
	}
	/**
	 * 针对insert,update,delete类型的sql进行处理
	 */
	@Override
	public int[] batch(StringBuilder sql, List<JSONObject> list) throws Exception {
		return batch(sql, getConn(), list);
	}
	public <T extends IValueObject> int[] batchForVO(StringBuilder sql, List<T> list) throws Exception {
		return batchForVO(sql, getConn(), list);
	}
	public <T extends IValueObject> int[] batchUpdate(List<T> list) throws Exception {
		if(CollectionUtils.isEmpty(list)){
			return new int[]{};
		}
		Class<T> clazz = (Class<T>)list.get(0).getClass();
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.update(clazz));
		return batchForVO(sql, list);
	}

	public <T extends IValueObject> int[] batchInsert(List<T> list) throws Exception {
		return batchInsert(list, null);
	}

	public <T extends IValueObject> int[] batchReplace(List<T> list) throws Exception {
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		Class<T> clazz = (Class<T>)list.get(0).getClass();
		EntityModel model = EntityModelHelper.build(clazz);
		IValueObjectWrapper<T> wrapper = EntityWrapperHelper.wrapper(clazz);
		List<FieldModel> f_list = model.field_list;
		V pk = null;
		FieldModel pkField = model.findPK();
		if (pkField != null) {
			for (int i = 0, len = list.size(); i < len; i++) {
				if (!VOHelper.has(pkField, list.get(i))) {// 如果已经在外部注入了。。。那就省事了
					pk = genId();
					wrapper.set(list.get(i), pkField.code, pk);
				}
				list.set(i, fixNumber(f_list, list.get(i)));
			}
			for (int i = 0, len = list.size(); i < len; i++) {
				VOHelper.validate(model, list.get(i));
			}
		}
		// FIXME 需要注意的是这个地方注入是有问题的，如果list中的每个元素不尽相同，则搞出来就有问题
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.replace(model));
		int[] ret = batchForVO(sql, list);
		return ret;
	}

	/**
	 * insert into table_name (col1...coln) select col1...coln from table_name
	 * where gurad1=?...guardm=?
	 */
	@Override
	public <T extends IValueObject> int[] batchInsert(List<T> list, FieldModel[] guardFields) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		// 1- 根据class拿到meta，
		EntityModel model = EntityModelHelper.build(list.get(0));
		return batchInsert(model, list, guardFields);
	}

	/**
	 * 
	 * @param tm
	 * @param list
	 * @param guardFields
	 * @return
	 * @throws Exception
	 */
	public <T extends IValueObject> int[] batchInsert(EntityModel tm, List<T> list, FieldModel[] guardFields) throws Exception {
		if(CollectionUtils.isEmpty(list)){
			return new int[]{};
		}
		ProfLogHelper.begin();
		List<FieldModel> f_list = tm.field_list;
		V pk = null;
		FieldModel pkField = tm.findPK();
		IValueObjectWrapper<T> wrapper = (IValueObjectWrapper<T>) EntityWrapperHelper.wrapper(list.get(0).getClass());
		if (pkField != null) {
			for (int i = 0, len = list.size(); i < len; i++) {
				if (!VOHelper.has(pkField, list.get(i))) {// 如果已经在外部注入了。。。那就省事了
					pk = genId();
					wrapper.set(list.get(i), pkField.code, pk);
				}
				list.set(i, fixNumber(f_list, list.get(i)));
			}
			for (int i = 0, len = list.size(); i < len; i++) {
				VOHelper.validate(tm, list.get(i));
			}
		}
		// FIXME 需要注意的是这个地方注入是有问题的，如果list中的每个元素不尽相同，则搞出来就有问题
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.insert(tm, guardFields));
		int[] ret = batchForVO(sql, list);
		ProfLogHelper.debug(tm.code + " batch insert list(" + list.size() + ") ");
		return ret;
	}
	public  int[] batchInsertJO(EntityModel tm, List<JSONObject> list, FieldModel[] guardFields) throws Exception {
		if(CollectionUtils.isEmpty(list)){
			return new int[]{};
		}
		ProfLogHelper.begin();
		List<FieldModel> f_list = tm.field_list;
		V pk = null;
		FieldModel pkField = tm.findPK();
		if (pkField != null) {
			for (int i = 0, len = list.size(); i < len; i++) {
				if(!JOHelper.has(pkField, list.get(i))){
					pk = genId();
					list.get(i).put(pkField.code, pk);
				}
				list.set(i, fixNumber(f_list, list.get(i)));
			}
			for (int i = 0, len = list.size(); i < len; i++) {
				VOHelper.validate(tm, list.get(i));
			}
		}
		// FIXME 需要注意的是这个地方注入是有问题的，如果list中的每个元素不尽相同，则搞出来就有问题
		StringBuilder sql = new StringBuilder(DaoHelper.Sql.insert(tm, guardFields));
		int[] ret = batch(sql, list);
		ProfLogHelper.debug(tm.code + " batch insert list(" + list.size() + ") ");
		return ret;
	}

	/**
	 * 
	 */

	@Override
	public boolean assertTrue(StringBuilder sql, JSONObject vo, String buz_msg) throws Exception {
		Connection conn = getConn();
		boolean flag = processCheckUnique(conn, sql, vo);
		if (!flag && !StringUtils.isBlank(buz_msg)) {
			ErrorBuilder.createBusiness().msg(buz_msg).execute();
		}
		return flag;
	}

	public V genId() throws Exception {
		V ret = getIdGen().gen(null);
		return ret;
	}

	/**
	 * 获得连接
	 * 
	 * @return
	 * @throws Exception
	 */
	public Connection getConn() throws Exception {
		Connection conn = getDbProvider().getConnection();
		return conn;
	}

	//
	/**
	 * 处理具体的查询过程
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            原始注册的sql语句
	 * @param param
	 *            需要被替换的参数容器
	 * **/
	private List<JSONObject> processQuery(Connection conn, StringBuilder sql, JSONObject param) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		SQLStruct struct = null;
		try {
			//
			struct = toSqlStruct(sql, param);
			stmt = makeStmt(conn, struct, param);
			List<JSONObject> ret = new ArrayList<JSONObject>();
			ProfLogHelper.begin();
			for (rs = stmt.executeQuery(); rs.next();) {
				JSONObject single = new JSONObject();
				DaoHelper.Rs.fetchJOFromResultSet(single, rs);
				ret.add(single);
			}
			ProfLogHelper.debug(DaoHelper.parseMsg(struct));
			return ret;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(rs, stmt, conn);
			ErrorBuilder.createSys().cause(e).msg(DaoHelper.parseMsg(struct));
		} finally {
			after(rs, stmt, conn);
		}
		return new ArrayList<JSONObject>();
	}
	private <T extends IValueObject> List<T> processQuery(Connection conn, StringBuilder sql, JSONObject param,Class<T> clazz) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		SQLStruct struct = null;
		try {
			//
			struct = toSqlStruct(sql, param);
			stmt = makeStmt(conn, struct, param);
			List<T> ret = new ArrayList<T>();
			ProfLogHelper.begin();
			for (rs = stmt.executeQuery(); rs.next();) {
				T single = DaoHelper.Rs.fetchVOFromResultSet(clazz, rs);
				ret.add(single);
			}
			ProfLogHelper.debug(DaoHelper.parseMsg(struct));
			return ret;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(rs, stmt, conn);
			ErrorBuilder.createSys().cause(e).msg(DaoHelper.parseMsg(struct)).execute();
		} finally {
			after(rs, stmt, conn);
		}
		return new ArrayList<T>();
	}
	/**
	 * 有可能要分为两个方法，一个是processSqlWithNull 处理SQL，替换变量
	 * 
	 * **/
	private PreparedStatement makeStmt(Connection conn, SQLStruct struct, JSONObject vo) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(struct.nataiveSql.toString());
			// 2-替换变量
			DaoHelper.Stmt.evaluateSQLWithNull(stmt, struct.values);
			return stmt;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(null, stmt, conn);
			throw e;
		}
	}

	private SQLStruct toSqlStruct(StringBuilder sql, JSONObject vo) throws Exception {
		SQLStruct strut = DaoHelper.Sql.parseSQL(sql.toString(), vo);
//		strut.bind(vo);
		return strut;
	}

	private int processInsertAndUpdate(Connection conn, StringBuilder sql, JSONObject jo) throws Exception {
		PreparedStatement stmt = null;
		try {
			//
			ProfLogHelper.begin();
			SQLStruct struct = toSqlStruct(sql, jo);
			stmt = makeStmt(conn, struct, jo);
			int ret = stmt.executeUpdate();
			ProfLogHelper.debug(DaoHelper.parseMsg(struct));
			return ret;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(null, stmt, conn);
			ErrorBuilder.createSys().msg(DaoHelper.parseMsg(sql, jo)).cause(e).execute();
		} finally {
			after(null, stmt, conn);
		}
		return -1;
	}

	/**
	 * 处理类似 select 1 from ....的语句
	 * 
	 * @return true(无符合sql条件的数据);false(有符合条件的数据)
	 * **/
	private boolean processCheckUnique(Connection conn, StringBuilder sql, JSONObject vo) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		SQLStruct struct = null;
		try {
			//
			ProfLogHelper.begin();
			struct = toSqlStruct(sql, vo);
			stmt = makeStmt(conn, struct, vo);
			rs = stmt.executeQuery();
			ProfLogHelper.debug(DaoHelper.parseMsg(struct));
			return !rs.next();
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(rs, stmt, conn);
			ErrorBuilder.createSys().cause(e).msg(DaoHelper.parseMsg(struct)).execute();
		} finally {
			after(rs, stmt, conn);
		}
		return false;
	}

	/**
	 * 善后处理
	 * 
	 * @param stmt
	 * @param conn
	 * @throws Exception
	 */
	protected void after(ResultSet rs, Statement stmt, Connection conn) throws Exception {
		if (rs != null) {
			rs.close();
		}
		if (stmt != null) {
			stmt.close();
			stmt = null;// 跟spring学的(JdbcTemplate)
		}
		if (conn != null) {
			getDbProvider().closeConnection(conn);
			conn = null;// 跟spring学的(JdbcTemplate)不知道这样会不会引起问题
		}
	}

	private int[] batch(StringBuilder sql, Connection conn, List<JSONObject> list) throws Exception {
		int count = CollectionUtils.isEmpty(list) ? 0 : list.size();
		if (count < 1) {// 如果没有就直接跑路
			log.debug("no data(" + sql + ")");
			return null;
		}
		PreparedStatement stmt = null;
		SQLStruct strut = null;
		try {
			ProfLogHelper.begin();
			// 需要注意的是如果太大就断掉了,最好100一插吧
			List<Integer> rets = new ArrayList<Integer>();

			int step = 500;// 一个批次的数量
			int mod = count % step;
			int total = count / step;
			if (mod != 0) {
				total++;
			}
			JSONObject vo;
			strut = DaoHelper.Sql.parseSQL(sql);
			stmt = conn.prepareStatement(strut.nataiveSql.toString());
			for (int i = 0; i < total; i++) {
				// log.debug("执行["+i+"]/["+total+"]批次的保存");
				stmt.clearBatch();// FIXME 有必要么？
				log.debug((i * step) + " of " + list.size() + "==>start");
				for (int ii = 0; (ii < step && (i * step + ii) < count); ii++) {
					vo = list.get(i * step + ii);
					strut.bind(vo);// 相当于绑定数据
					// if (log.isDebugEnabled()) {//没有必要，出错了再说
					// log.debug(DaoHelper.parseMsg(strut));
					// }
					DaoHelper.Stmt.evaluateSQLWithNull(stmt, strut.values);
					stmt.addBatch();
				}
				log.debug((i * step) + " of " + list.size() + "==>pre");
				int[] flags = stmt.executeBatch();
				log.debug((i * step) + " of " + list.size() + "==>" + strut.nataiveSql);
				for (int flag : flags) {
					rets.add(flag);
				}
			}
			// 处理结果
			int[] ret = new int[rets.size()];
			for (int i = 0, len = rets.size(); i < len; i++) {
				ret[i] = rets.get(i);
			}
			ProfLogHelper.debug(sql + ":\n" + list.size());
			return ret;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(null, stmt, conn);
			ErrorBuilder.createSys().msg(sql + "\n" + DaoHelper.parseMsg(strut)).cause(e).execute();
		} finally {
			after(null, stmt, conn);
		}
		return new int[]{};
	}
	private <T extends IValueObject> int[] batchForVO(StringBuilder sql, Connection conn, List<T> list) throws Exception {
		int count = CollectionUtils.isEmpty(list) ? 0 : list.size();
		if (count < 1) {// 如果没有就直接跑路
			log.debug("no data(" + sql + ")");
			return null;
		}
		PreparedStatement stmt = null;
		SQLStruct strut = null;
		try {
			ProfLogHelper.begin();
			// 需要注意的是如果太大就断掉了,最好100一插吧
			List<Integer> rets = new ArrayList<Integer>();

			int step = 500;// 一个批次的数量
			int mod = count % step;
			int total = count / step;
			if (mod != 0) {
				total++;
			}
			T vo;
			strut = DaoHelper.Sql.parseSQL(sql);
			stmt = conn.prepareStatement(strut.nataiveSql.toString());
			for (int i = 0; i < total; i++) {
				stmt.clearBatch();
				log.debug((i * step) + " of " + list.size() + "==>start");
				for (int ii = 0; (ii < step && (i * step + ii) < count); ii++) {
					vo = list.get(i * step + ii);
					strut.bind(vo);// 相当于绑定数据
					DaoHelper.Stmt.evaluateSQLWithNull(stmt, strut.values);
					stmt.addBatch();
				}
				log.debug((i * step) + " of " + list.size() + "==>pre");
				int[] flags = stmt.executeBatch();
				log.debug((i * step) + " of " + list.size() + "==>" + strut.nataiveSql);
				for (int flag : flags) {
					rets.add(flag);
				}
			}
			// 处理结果
			int[] ret = new int[rets.size()];
			for (int i = 0, len = rets.size(); i < len; i++) {
				ret[i] = rets.get(i);
			}
			ProfLogHelper.debug(sql + ":\n" + list.size());
			return ret;
		} catch (Exception e) {
			// 参考spring jdbc template的写法，尽快释放连接，据说能避免connection pool的死锁
			after(null, stmt, conn);
			ErrorBuilder.createSys().msg(sql + "\n" + DaoHelper.parseMsg(strut)).cause(e).execute();
		} finally {
			after(null, stmt, conn);
		}
		return new int[]{};
	}

	public IDBConnectionProviderService getDbProvider() {
		return _dbConnProviderService;
	}

	public abstract void setDbProvider(IDBConnectionProviderService value);

	public IDGenService<V> getIdGen() {
		return _idGen;
	}

	public abstract void setIdGen(IDGenService<V> value);
}
