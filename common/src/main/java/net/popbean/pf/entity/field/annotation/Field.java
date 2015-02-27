package net.popbean.pf.entity.field.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.popbean.pf.entity.IValueObject;
import net.popbean.pf.entity.field.Domain;

@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Field {

	String code() default "";

	String name() default "";

	Domain domain();

	int length() default 12;

	int precision() default 2;

	boolean required() default true;

	String range() default "";

	// 以下适用于relation ship的情况，如果有必要就单独弄一个annotation
	// FIXME 可能会导致业务模块之间的强依赖，需要继续观察，如果有问题，切换回String
	// type == domain.ref时有效，如果pb_pf_ds_range有必要就放到common module中
	Class<IValueObject> relation() default IValueObject.class;

	RelationType rt() default RelationType.None;
}
