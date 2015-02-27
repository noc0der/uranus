package net.popbean.pf.entity.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.popbean.pf.business.service.impl.AbstractBusinessService;
import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.helper.JOHelper;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;
import net.popbean.pf.entity.service.EntityStructBusinessService;
import net.popbean.pf.entity.struct.dao.EntityStructDao;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.helper.ClassRefHelper;
import net.popbean.pf.id.helper.IdGenHelper;
import net.popbean.pf.security.vo.SecuritySession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

@Service("service/pf/entity/struct")
public class EntityStructBusinessServiceImpl extends AbstractBusinessService implements EntityStructBusinessService {
	//
	@Autowired
	@Qualifier("dao/pf/entity/schema")
	EntityStructDao _esDao;

	//
	public List<JSONObject> fetchSchema() throws BusinessError {
		try {
			// 没有必要，拼凑一个sql就搞定了
			List<JSONObject> ret = _esDao.fetchSchemeList();
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	public void dropSchema(String schema) throws BusinessError {
		try {
			int loop = 0;
			while (loop++ <= 3) {
				List<JSONObject> entity_list = _esDao.fetchEntityListBySchema(schema);
				if (entity_list.size() == 0) {
					break;
				}
				JSONObject vo = new JSONObject();
				for (JSONObject e : entity_list) {
					// FIXME 找出外键来，干掉，不然清除不了
					Map<String, Integer> index_cache = new HashMap<>();
					List<JSONObject> index_list = _esDao.fetchIndex(schema, e.getString("TABLE_NAME"));
					if (!CollectionUtils.isEmpty(index_list)) {
						for (JSONObject i : index_list) {
							String index_name = i.getString("INDEX_NAME");
							Integer count = index_cache.get(index_name);
							if (count == null) {
								index_cache.put(index_name, 1);
							} else {
								index_cache.put(index_name, count++);
							}
						}
						for (JSONObject i : index_list) {
							if (JOHelper.equalsStringValue(i, "INDEX_NAME", "PRIMARY")) {
								continue;
							}
							Integer count = index_cache.get(i.getString("INDEX_NAME"));
							if (count > 1) {// 联合唯一索引
								continue;
							}
							try {
								StringBuilder drop_index_sql = new StringBuilder("alter table ").append(e.getString("TABLE_NAME")).append(" drop foreign key ")
										.append(i.getString("INDEX_NAME"));
								_commondao.executeChange(drop_index_sql, vo);
							} catch (Exception e2) {
								System.out.println("error drop foreign key:" + i.getString("INDEX_NAME"));
							}

						}
					}
					StringBuilder sql = new StringBuilder("drop table ").append(e.getString("TABLE_NAME"));
					try {
						_commondao.executeChange(sql, vo);
					} catch (Exception e2) {// FIXME 这个需要写到日志中
						System.out.println("error:drop table:" + e.getString("TABLE_NAME"));
					}
				}
			}

		} catch (Exception e) {
			processError(e);
		}
	}

	@Override
	public boolean exists(String entity_code, String col_code) throws BusinessError {
		try {
			return _commondao.isExists(entity_code, col_code);
		} catch (Exception e) {
			processError(e);
		}
		return false;
	}

	@Override
	public List<String> diffDbStructByEntityModel(Class<? extends IValueObject> clazz) throws BusinessError {
		try {

			List<String> ret = new ArrayList<String>();
			EntityModel tm = EntityModelHelper.build(clazz);
			boolean flag = _commondao.isExists(tm.code, null);// 可以换成是entitymanager么？否，因为刚开始的时候连entitymanager要访问的表都没有，访问个屁啊
			if (!flag) {// 不存在，插入一个
				String temp = _esDao.buildCreateTableSql(tm);// 创建
				if (!StringUtils.isBlank(temp) && !CollectionUtils.contains(ret.iterator(), temp)) {
					ret.add(temp);
				}
			} else {// 如果已经存在就是更新的局了
					// 或许先进行判断，是否有数据，如果没数据直接删除拉倒了
				String table_code = tm.code;
				boolean isEmpty = _esDao.isEmpty(table_code);
				if (isEmpty) {// 空表，可以进行删除操作
					String temp = _esDao.buildCreateTableSql(tm);
					if (!StringUtils.isBlank(temp) && !CollectionUtils.contains(ret.iterator(), temp)) {// 我的意思是说，如果不重建就别删了，否则会有问题吧，现在没数据并不代表将来没数据
						String dropSql = "drop table " + table_code;
						// 不是自己家的表，不动
						if ((table_code.toUpperCase().startsWith("RLT_") || table_code.toUpperCase().startsWith("PB_"))
								&& !CollectionUtils.contains(ret.iterator(), dropSql)) {// 确保没有重复的
							ret.add(dropSql);
						}
						ret.add(temp);
					}
				} else {// 已经有了数据，建议最好采用更新的方式进行，不要删除属性
					List<String> temp = _esDao.compareStruct(tm);
					if (!CollectionUtils.isEmpty(temp)) {
						for (String t : temp) {
							if (!CollectionUtils.contains(ret.iterator(), t)) {
								ret.add(t);
							}
						}
					}
				}
			}
			return ret;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	@Override
	public EntityModel buildEntityModelByDbStruct(String entity_code) throws BusinessError {
		try {
			return _esDao.buildTableMetaByDbStruct(entity_code);
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	@Override
	public void syncDbStructByEntityModel(List<EntityModel> em_list, SecuritySession session) throws BusinessError {
		try {
			List<JSONObject> list = new ArrayList<>();
			for (EntityModel tm : em_list) {
				JSONObject jo = new JSONObject();
				jo.put("CODE_ENTITY", tm.code);
				list.add(jo);
				List<String> sqls = diffDbStructByEntityModel((Class<IValueObject>) tm.getClass());
				_commondao.batch(sqls);
			}
			// 更新状态
			StringBuilder sql = new StringBuilder(" update pb_pf_entity set istat=5 where code_entity=${CODE_ENTITY} ");
			_commondao.batch(sql, list);
		} catch (Exception e) {
			processError(e);
		}
	}

	@Override
	public void syncEntityModel(List<EntityModel> em_list, SecuritySession session) throws BusinessError {
		try {
			for (EntityModel model : em_list) {
				List<EntityModel> list = EntityModelHelper.buildForRelation(model);
				_commondao.batchReplace(list);
				syncEntityModel(model, session);
			}
		} catch (Exception e) {
			processError(e);
		}
	}
	private String syncEntityModel(EntityModel em, SecuritySession session) throws BusinessError {
		try {

			String pk_entity = em.pk_entity;

			// 2- 修改：无论entity_code是否发生变化，统一用原来的主键删除
			List<FieldModel> field_list = em.field_list;
			// 进行重名的保护
			//FIXME code_account_code vs c_account_code vs c_account vs code_account 只好取code_account了
			StringBuilder sql = new StringBuilder(" select 1 from pb_pf_entity where code_entity=${code_entity} $[and pk_entity!=${pk_entity}]");
			JSONObject param = JOHelper.vo2jo(em);
			_commondao.assertTrue(sql, param, "不能重名，已经存在entity_code=" + em.code);

			// 清除老数据
			// FIXME 这个逻辑有问题，万一改了entity_code咋办
			sql = new StringBuilder(" delete from pb_pf_field where pk_entity in (select a.pk_entity from pb_pf_entity a where a.code_entity=${code_entity}) ");
			_commondao.executeChange(sql, param);
			sql = new StringBuilder(" delete from pb_pf_entity where entity_code=${code_entity} ");
			_commondao.executeChange(sql, param);
			// 插入新数据
			String pk_entity_new = IdGenHelper.genID(em.code, null);
			if (StringUtils.isBlank(pk_entity)) {
			} else {// 修改
				sql = new StringBuilder(" delete from mt_pf_field where pk_entity=${pk_entity} ");
				_commondao.executeChange(sql, param);
				sql = new StringBuilder(" delete from pb_pf_entity where pk_entity=${pk_entity} ");
				_commondao.executeChange(sql, param);
			}
			//
			em.pk_entity = null;// 确保一定能插入
			_commondao.save(em, false, pk_entity_new);
			for (FieldModel v : field_list) {
				String pk_field = IdGenHelper.genID(em.code, v.code);
				v.pk_field = pk_field;
				v.pk_entity = pk_entity_new;
			}
			_commondao.batchReplace(field_list);
			return pk_entity_new;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	@Override
	public void syncDbStruct(Class<? extends IValueObject> clazz, SecuritySession session) throws BusinessError {
		try {
			List<String> sqls = diffDbStructByEntityModel(clazz);
			_commondao.batch(sqls);
		} catch (Exception e) {
			processError(e);
		}		
	}

	@Override
	public void syncDbStruct(SecuritySession session) throws BusinessError {
		try {
			List<String> rs = ClassRefHelper.scanEntity("net.popbean");
			for(String clazz:rs){
				String[] t = clazz.split("#@#");
				if(t.length == 2){//去掉jarname
					clazz = t[1];
				}
				log.debug("process entity begin:"+clazz);
				syncDbStruct((Class<? extends IValueObject>)Class.forName(clazz),session);
				log.debug("process entity end:"+clazz);
			}
		} catch (Exception e) {
			processError(e);
		}		
	}

	@Override
	public List<String> diffDbStructByEntityModel(SecuritySession session) throws BusinessError {
		try {
			List<String> rs = ClassRefHelper.scanEntity("net.popbean");
			List<String> sqls = new ArrayList<String>();
			for(String clazz:rs){
				String[] t = clazz.split("#@#");
				if(t.length == 2){
					clazz = t[1];
				}
				log.debug("analysis:"+clazz);//往这里输出是否合适？
				
				List<String> temp = diffDbStructByEntityModel((Class<? extends IValueObject>)Class.forName(clazz));
				if(!CollectionUtils.isEmpty(temp)){
					for(String tt:temp){
						if(CollectionUtils.contains(sqls.iterator(), tt)){
							sqls.add(tt);
						}
					}
				}
			}
			return sqls;
		} catch (Exception e) {
			processError(e);
		}
		return null;
	}

	@Override
	public void cleanDbStruct(String scheme, String table, String column, Boolean isForce, SecuritySession session) throws BusinessError {
		try {
			//-得到表的清单
			List<JSONObject> entities = _esDao.fetchEntityListBySchema(scheme);//
			if(CollectionUtils.isEmpty(entities)){
				log.debug("没有找到任何table，返回");
				return ;
			}
			//筛选出没有数据的表
			for(JSONObject e:entities){
				String table_code = e.getString("TABLE_NAME");
				if(table_code.indexOf("$") == -1 ){//不是bin$xxx的表
					boolean isEmpty = true;
					if(!isForce){
						isEmpty = _esDao.isEmpty(table_code);
					}
					if(isEmpty){
						if(StringUtils.isEmpty(table)){//如果没有指定table，无差别的删除
							_esDao.drop(table_code, null);
						}else if(table_code.equals(table)){//指定了的话，就定点删除吧
							_esDao.drop(table_code,column);								
						}
					}
				}
			}
			//执行删除操作
		} catch (Exception e) {
			processError(e);
		}		
	}
	@Override
	public void impByEntityClass(List<String> clazz_list,Boolean isSyncDb,SecuritySession session)throws BusinessError{
		try {
			List<EntityModel> tm_list = new ArrayList<>();
			for(String clazz : clazz_list){
				EntityModel tm = EntityModelHelper.build((Class<IValueObject>)Class.forName(clazz));
				tm_list.add(tm);
			}
			syncEntityModel(tm_list, session);
			if(isSyncDb){
				syncDbStructByEntityModel(tm_list, session);
			}
		} catch (Exception e) {
			processError(e);
		}
	}
	@Override
	public void syncDbStructByClazz(List<String> clazz_list,SecuritySession client)throws BusinessError{
		try {
			List<EntityModel> tm_list = new ArrayList<>();
			for(String clazz : clazz_list){
				Class<IValueObject> cl = (Class<IValueObject>)Class.forName(clazz);
				EntityModel em = EntityModelHelper.build(cl);
				tm_list.add(em);
			}
			syncDbStructByEntityModel(tm_list, client);//这样做是有问题的，丢失了relation
		} catch (Exception e) {
			processError(e);
		}
	}
}
