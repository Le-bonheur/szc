package com.ssc.szc.persistence.autoconfig.mybatis.customer.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CustomerBeanBindTo {

    @AliasFor("datasources")
    String[] value() default {};

    @AliasFor("value")
    String[] datasources() default {};

}
