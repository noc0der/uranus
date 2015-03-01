package net.popbean.pf.entity.helper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.IValueObjectWrapper;
import net.popbean.pf.entity.field.Domain;
import net.popbean.pf.entity.model.EntityModel;
import net.popbean.pf.entity.model.FieldModel;
import net.popbean.pf.entity.model.helper.EntityModelHelper;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 帮助创建bean wrapper
 * @author to0ld
 *
 */

public class EntityWrapperHelper implements Opcodes{
	//
	@SuppressWarnings(value="rawtypes")
	private static  Map<String,IValueObjectWrapper> _cache = new ConcurrentHashMap<>();
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings(value={"unchecked","rawtypes"})
	public static <T extends IValueObject> IValueObjectWrapper<T> wrapper(Class<T> clazz)throws Exception{
		String key = clazz.getName();
		IValueObjectWrapper<T> ret = _cache.get(key);
		if(ret != null){
			return ret;
		}
		byte[] b = dump(clazz);
		SimpleClassLoader myClassLoader = new SimpleClassLoader();
		String wrapper_name = key+"Wrapper";//testcase/vo/AccountVOWrapper
		
		Class c = myClassLoader.defineClass(wrapper_name, b);
		ret = (IValueObjectWrapper)c.newInstance();
		_cache.put(key, ret);
		return ret;
	}
	@SuppressWarnings(value={"unchecked","rawtypes"})
	public static <T extends IValueObject> IValueObjectWrapper<T> wrapper(EntityModel model)throws Exception{
		String key = model.clazz;
		if(StringUtils.isBlank(key)){//如果没有clazz，就直接用code
			key = model.code;
		}
		IValueObjectWrapper ret = _cache.get(key);
		if(ret != null){
			return ret;
		}
		byte[] b = dump(model);
		SimpleClassLoader myClassLoader = new SimpleClassLoader();
		String wrapper_name = key+"Wrapper";//testcase/vo/AccountVOWrapper
		Class<IValueObjectWrapper> c = (Class<IValueObjectWrapper>)myClassLoader.defineClass(wrapper_name, b);
		ret = (IValueObjectWrapper)c.newInstance();
		_cache.put(key, ret);
		return ret;
	} 
	/**
	 * 提供根据entity model生成wrapper的方法，以便可以通过动态的方式(比如从数据库中读取组装entity model)
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public static byte[] dump(EntityModel model)throws Exception{
		String obj_vendor = "java/lang/Object";
		String str_vendor = "java/lang/String";
		String name = model.clazz;//testcase.vo.AccountVO
		String name_vendor = name.replaceAll("\\.", "/");//testcase/vo/AccountVO
		String wrapper_name = name_vendor+"Wrapper";//testcase/vo/AccountVOWrapper
		String interface_name = IValueObjectWrapper.class.getName().replaceAll("\\.", "/");//net/popbean/entity/IValueObjectWrapper
		//
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, 
				wrapper_name,
				"L"+obj_vendor+";L"+interface_name+"<L"+name_vendor+";>;", 
				obj_vendor,
				new String[] { interface_name });
		//
		MethodVisitor mv;
		{//构造函数，无参那种，也是object默认的那种
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, obj_vendor, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		//FIXME set方法
		List<FieldModel> field_list = model.field_list;
		{
			mv = cw.visitMethod(ACC_PUBLIC, "set", "(L"+name_vendor+";Ljava/lang/String;Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			//FIXME 需要在这里有个循环
			for(FieldModel f: field_list){
				String domain_type_vendor = getDomainClassVendor(f);//java/lang/String
				
//				mv.visitLdcInsn(f.code);//FIXME
//				mv.visitVarInsn(ALOAD, 2);
//				mv.visitMethodInsn(INVOKEVIRTUAL, str_vendor, "equals", "(L"+obj_vendor+";)Z", false);
//				Label l1 = new Label();
//				mv.visitJumpInsn(IFEQ, l1);
//				mv.visitVarInsn(ALOAD, 1);
////				mv.visitLdcInsn(Type.getType("L"+domain_type_vendor+";"));
////				mv.visitVarInsn(ALOAD, 3);
//				mv.visitVarInsn(ALOAD, 1);
//				mv.visitFieldInsn(GETFIELD, name_vendor, f.code, "L"+domain_type_vendor+";");//FIXME field.code+field.domain
//				mv.visitMethodInsn(INVOKEVIRTUAL, obj_vendor, "getClass", "()Ljava/lang/Class;", false);
//				mv.visitVarInsn(ALOAD, 3);
//				mv.visitMethodInsn(INVOKESTATIC, "net/popbean/entity/helper/VOHelper", "cast", "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
//				mv.visitTypeInsn(CHECKCAST, domain_type_vendor);//FIXME field.domain
//				mv.visitFieldInsn(PUTFIELD, name_vendor, f.code, "L"+domain_type_vendor+";");//field.code+field.domain
//				mv.visitInsn(RETURN);
//				mv.visitLabel(l1);
//				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				//----------------------------
				mv.visitLdcInsn(f.code);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitMethodInsn(INVOKEVIRTUAL, str_vendor, "equals", "(L"+obj_vendor+";)Z", false);
				Label l2 = new Label();
				mv.visitJumpInsn(IFEQ, l2);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(Type.getType("L"+domain_type_vendor+";"));
				mv.visitVarInsn(ALOAD, 3);
				mv.visitMethodInsn(INVOKESTATIC, "net/popbean/pf/entity/helper/VOHelper", "cast", "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
				mv.visitTypeInsn(CHECKCAST, domain_type_vendor);
				mv.visitFieldInsn(PUTFIELD, name_vendor, f.code, "L"+domain_type_vendor+";");
				mv.visitInsn(RETURN);
				mv.visitLabel(l2);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				//
			}
			//以下为固定部分
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 4);
			mv.visitEnd();
		}
		//get方法
		{
			mv = cw.visitMethod(ACC_PUBLIC, "get", "(L"+name_vendor+";Ljava/lang/String;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			for(FieldModel f:field_list){
				//
				String domain_type_vendor = getDomainClassVendor(f);//java/lang/String
				//
				mv.visitLdcInsn(f.code);//FIXME f.code
				mv.visitVarInsn(ALOAD, 2);
				mv.visitMethodInsn(INVOKEVIRTUAL, str_vendor, "equals", "(L"+obj_vendor+";)Z", false);//
				Label l2 = new Label();
				mv.visitJumpInsn(IFEQ, l2);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitFieldInsn(GETFIELD, name_vendor, f.code, "L"+domain_type_vendor+";");//name_vendor+field.code,field.domain
				mv.visitInsn(ARETURN);
				mv.visitLabel(l2);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			
			//固定部分
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		//
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "get", "(L"+obj_vendor+";L"+str_vendor+";)L"+obj_vendor+";", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, name_vendor);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, wrapper_name, "get", "(L"+name_vendor+";L"+str_vendor+";)L"+obj_vendor+";", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "set", "(L"+obj_vendor+";L"+str_vendor+";L"+obj_vendor+";)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, name_vendor);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, wrapper_name, "set", "(L"+name_vendor+";L"+str_vendor+";L"+obj_vendor+";)V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 4);
			mv.visitEnd();
		}
		cw.visitEnd();
		return cw.toByteArray();
	}
	/**
	 * 构建wrapper的实现
	 * @param clazz 其实必须是有ivalue object的
	 * @return
	 */
	private static <T extends IValueObject> byte[] dump(Class<T> clazz)throws Exception{
		EntityModel model = EntityModelHelper.build(clazz);
		return dump(model);
	}

	/**
	 * 根据field model得到模型
	 * @param model
	 * @return
	 */
	private static String getDomainClassVendor(FieldModel model){
		if(model.type.equals(Domain.Code) || model.type.equals(Domain.Memo) || model.type.equals(Domain.PK) || model.type.equals(Domain.Seriescode)){
			return "java/lang/String";
		}else if(model.type.equals(Domain.Stat)){
			return "java/lang/Integer";
		}else if(model.type.equals(Domain.Money)){
			return "java/math/BigDecimal";
		}else if(model.type.equals(Domain.Date)){
			return "java/sql/Date";
		}else if(model.type.equals(Domain.TimeStamp)){
			return "java/sql/Timestamp";
		}
		return "java/lang/String";
	}
	//
	/**
	 * 
	 * @author to0ld
	 *
	 */
	private static class SimpleClassLoader extends ClassLoader {
//		public Class<?> defineClass(String className, byte[] byteCodes) {
//			return super.defineClass(className, byteCodes, 0, byteCodes.length);
//		}
		public Class<?> defineClass(String className, byte[] byteCodes) {
			return super.defineClass(className, byteCodes, 0, byteCodes.length);
		}
	}
}
