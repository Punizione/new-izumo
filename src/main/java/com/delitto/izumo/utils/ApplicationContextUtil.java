package com.delitto.izumo.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContextOuter(){
        return applicationContext;
    }

    public static <T> T get(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static Object get(String name) {
        return applicationContext.getBean(name);
    }

    public static String getConfigFromEnvironment(String key) {
        Environment enviroment = get(Environment.class);
        return enviroment.getProperty(key);
    }

    public static DefaultListableBeanFactory getBeanFactory(){
        return (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    }

}
