package com.nexon.nutriai.constant.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAnnotation {

    String value() default "";

    // 添加请求类型枚举
    RequestType requestType() default RequestType.NORMAL;

    enum RequestType {
        NORMAL,     // 普通请求
        DIALOGUE,   // 对话请求
        LOGIN       // 登录请求
    }
}
