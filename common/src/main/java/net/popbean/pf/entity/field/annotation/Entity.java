package net.popbean.pf.entity.field.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.popbean.pf.entity.model.EntityType;
/**
 * 如果需要支持主从就得加上elementtype.field
 * @author to0ld
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	String code() default "";

	String name() default "";

	String memo() default "";

//	boolean dynamic() default false;// 默认不是动态结构
	EntityType type() default EntityType.Normal;
}
