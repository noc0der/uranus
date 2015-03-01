package net.popbean.pf.entity.model.helper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.field.annotation.Entity;
import net.popbean.pf.entity.field.annotation.RelationType;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.EntityType;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.exception.ErrorBuilder;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
/**
 * 实体模型帮助类
 * @author to0ld
 *
 */
public class EntityModelHelper {
	private static Map<String, EntityModel> _cache = new ConcurrentHashMap<>();
	private static Map<String, List<EntityModel>> _relation_cache = new ConcurrentHashMap<>();
	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static EntityModel build(Class<? extends IValueObject> clazz)throws Exception{
		String key = clazz.getName();
		EntityModel model = _cache.get(key);
		if(model != null){
			return model;
		}
		Entity entity = clazz.getAnnotation(Entity.class);
		if(entity == null){
			ErrorBuilder.createSys().msg("传入的类("+clazz+")没有定义@Entity，无法提取实体模型信息").execute();
		}
		model = new EntityModel();
		model.code = entity.code();
		model.name = entity.name();
		model.clazz = key;
		List<FieldModel> fm_list = parseFieldModelList(clazz);
		model.field_list = fm_list;
		_cache.put(key, model);//缓存
		return model;
	}
	/**
	 * 获得主键
	 * @param field_list
	 * @return
	 */
	public static FieldModel findPK(List<FieldModel> field_list){
		if(CollectionUtils.isEmpty(field_list)){
			return null;
		}
		for(FieldModel fm:field_list){
			if(Domain.PK.equals(fm.type)){
				return fm;
			}
		}
		return null;
	}
	/**
	 * 外键多了，那就搞出来不止一个了
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static List<EntityModel> buildForRelation(Class<? extends IValueObject> clazz)throws Exception{
		Entity entity = clazz.getAnnotation(Entity.class);
		//
		if(entity == null){
			ErrorBuilder.createSys().msg("传入的类("+clazz+")没有定义@Entity，无法提取实体模型信息").execute();
		}
		String key = entity.code();
		List<EntityModel> ret = _relation_cache.get(key);
		if(ret != null){
			return ret;
		}
		//得到传入的实体entity model
		EntityModel model = build(clazz);
		ret = buildForRelation(model);
		_relation_cache.put(key, ret);
		return ret;
	}
	public static List<EntityModel> buildForRelation(EntityModel entity)throws Exception{
//		model.field_list = fm_list;
		List<EntityModel> ret = new ArrayList<>();
		if(EntityType.Bridge.equals(entity.type)){//桥接表的情况
			//得找到两个ref，并且一主一从才行
			int loop = 0;//够2才能跑,暂时不管一个entity中多个master，多个slave的情况
			for(FieldModel fm:entity.field_list){
				if(Domain.Ref.equals(fm.type) && !RelationType.Master.equals(fm.rt)){
					loop+=1;//FIXME 如果要知道准确的少了啥多了啥，就采用i_master,i_slave
				}
				if(Domain.Ref.equals(fm.type) && !RelationType.Slave.equals(fm.rt)){
					loop+=1;
				}
			}
			if(loop !=2){//如果需要更明晰的，就改逻辑吧
				ErrorBuilder.createSys().msg("作为桥接表得有master&slave，少一个不行多一个也不行").execute();
			}
			ret.add(entity);
			return ret;
//			if(loop ==2){
//				EntityModel em = new EntityModel();
//				em.code = entity.code;
//				em.name = entity.name;
//				FieldModel pk_field = entity.findPK();
//				list.add(pk_field);
//				em.field_list = list;
//				ret.add(em);
//				return ret;
//			}
		}else{//非桥接表，找ref
			//只要找到一个就好
			for(FieldModel fm:entity.field_list){
				if(Domain.Ref.equals(fm.type) && !RelationType.None.equals(fm.rt)){
					EntityModel em = new EntityModel();
					em.code = entity.code;
					em.name = entity.name;
					//构建三个字段pk,main(),slave()
					FieldModel pk_field = findPK(entity.field_list);//同时也是slave
					FieldModel slave = ObjectUtils.clone(pk_field);
					slave.rt = RelationType.Slave;
					//获得master
					FieldModel master = ObjectUtils.clone(fm);
					master.rt = RelationType.Master;
					List<FieldModel> list = new ArrayList<>();
					list.add(pk_field);
					list.add(master);
					list.add(slave);
					em.field_list = list;
					ret.add(em);
				}
			}
			return ret;
		}
	}
	/**
	 * 根据静态模型去构建实体模型
	 * @param vo
	 * @return
	 */
	public static EntityModel build(IValueObject vo)throws Exception{
		//需要判断一下是动态模型，还是静态模型，静态就直接解析，动态则读取数据库数据
		if(vo == null){
			ErrorBuilder.createSys().msg("传入的参数为空，无法提取实体模型信息").execute();
		}
		return build(vo.getClass());
	}
	/**
	 * 将pojo中的field解析出来
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static List<FieldModel> parseFieldModelList(Class<?> clazz)throws Exception{
		Field[] f_list = clazz.getDeclaredFields();
		List<FieldModel> ret = new ArrayList<>();
		for(Field f:f_list){
			FieldModel model = buildFieldModel(f);
			if(model != null){
				ret.add(model);
			}
		}
		return ret;
	}
	public static FieldModel buildFieldModel(ResultSet rs)throws Exception{
		FieldModel field = new FieldModel();
		String key = rs.getString("COLUMN_NAME");
		int type = rs.getInt("DATA_TYPE");
		int size = rs.getInt("COLUMN_SIZE");//如果执行两次就会报错
		String def = rs.getString("COLUMN_DEF");//好像是mssql碰到的情况((0))
		//
		field.code = key;
		field.name = key;
		if(type == Types.VARCHAR){
			if(size == 1000){//pk
				field.type = Domain.Memo;
			}else if(size == 60){// modify by yaolei02
				field.type = Domain.PK;
			}else if(size == 120){
				field.type = Domain.Code;
			}
		}else if(type == Types.DECIMAL || type == Types.FLOAT){//后面这个是容错处理
			int p = rs.getInt("DECIMAL_DIGITS");
			if(p == 0){
				field.type = Domain.Stat;
				if(!StringUtils.isBlank(def)){
					def = def.replaceAll("\\(", "");
					def = def.replaceAll("\\)", "");
					field.defaultValue = def.trim();
				}
			}else{
				field.type = Domain.Money;
				field.precision = p;
				if(!StringUtils.isBlank(def)){
					def = def.replaceAll("\\(", "");
					def = def.replaceAll("\\)", "");
					if(!StringUtils.isBlank(def)){
						field.defaultValue = def.trim();
					}
				}
			}
		}else if(type == Types.DATE){
			field.type = Domain.Date;
		}else if(type == Types.TIMESTAMP){
			field.type = Domain.TimeStamp;
		}else if(type == Types.CHAR){
			field.type = Domain.PK;
		}else if(type == Types.SMALLINT || type == Types.TINYINT){
			field.type = Domain.Stat;
			if(!StringUtils.isBlank(def)){
				def = def.replaceAll("\\(", "");
				def = def.replaceAll("\\)", "");
				field.defaultValue = def.trim();
			}
		}else if(type == Types.INTEGER){
			field.type = Domain.Int;
			if(!StringUtils.isBlank(def)){
				def = def.replaceAll("\\(", "");
				def = def.replaceAll("\\)", "");
				field.defaultValue = def.trim();
			}
		}else if(type == Types.LONGVARCHAR){//FIXME 其实这个只是为了容错，正常情况绝对不会产生的
		}
		if(field == null){
			throw new IllegalArgumentException("该字段["+key+"]为不可识别的数据类型["+type+"]");
		}
		boolean isReq = (rs.getInt("NULLABLE") == ResultSetMetaData.columnNoNulls);
		field.required = isReq;
		return field;
	}
	/**
	 * 利用field反射得到fieldmodel
	 * @param f
	 * @return
	 */
	public static FieldModel buildFieldModel(Field f)throws Exception{
		net.popbean.pf.entity.field.annotation.Field a = f.getAnnotation(net.popbean.pf.entity.field.annotation.Field.class);
		if(a == null){
			return null;
		}
		FieldModel model = new FieldModel();
		if(StringUtils.isBlank(a.code())){
			model.code = f.getName();
		}else{
			model.code = a.code();	
		}
		model.type = a.domain();
		model.name = a.name();
		//
		if(Domain.Ref.equals(model.type)){//如果是ref类型，还得继续找
			if(!IValueObject.class.equals(a.relation())){
				EntityModel rlt_model = build(a.relation());
				model.code_relation_entity = rlt_model.code;
				model.pk_relation_entity = rlt_model.findPK().code;
			}
		}
		return model;
	}
}
