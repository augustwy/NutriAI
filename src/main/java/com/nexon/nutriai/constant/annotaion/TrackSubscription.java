package com.nexon.nutriai.constant.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackSubscription {

    String value() default "";

    // 或者使用参数名称方式
    String streamIdParamName();
}
