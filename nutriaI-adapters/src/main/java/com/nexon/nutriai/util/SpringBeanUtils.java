package com.nexon.nutriai.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Spring Bean 工具类
 * 用于在非Spring管理的类中获取Spring容器中的Bean
 */
@Component
public class SpringBeanUtils {
    
    private static ApplicationContext applicationContext;
    
    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        SpringBeanUtils.applicationContext = context;
    }
    
    /**
     * 获取指定类型的Bean
     * 
     * @param <T> Bean类型
     * @param clazz Bean类型Class
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    
    /**
     * 根据名称获取Bean
     * 
     * @param <T> Bean类型
     * @param name Bean名称
     * @return Bean实例
     */
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }
    
    /**
     * 根据名称和类型获取Bean
     * 
     * @param <T> Bean类型
     * @param name Bean名称
     * @param clazz Bean类型Class
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
}
