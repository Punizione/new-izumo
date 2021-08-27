package com.delitto.izumo.utils;

import com.delitto.izumo.framework.base.PluginClassBean;
import com.delitto.izumo.framework.util.FileUtil;
import com.delitto.izumo.middleware.ExternJarClassHandler;
import com.delitto.izumo.middleware.ClassLoaderProcesser;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Log4j2
public class ContextBeanUtil {

    public static int reloadSpringBeans(String pluginJarsPath) {
        int count = 0;
        List<PluginClassBean> jars = FileUtil.loadJars(pluginJarsPath);
        if(jars.size()>0) {
            for(PluginClassBean jarBean: jars) {
                File jar = new File(jarBean.getPath());
                URI uri = jar.toURI();
                try {
                    if(ClassLoaderProcesser.getInstance().containsClassLoader(jarBean.getMainClass())){
                        ClassLoaderProcesser.getInstance().removeClassLoader(jarBean.getMainClass());
                    }
                    String argsReplace = jarBean.getMainClass().replace("PluginMain", "PluginArgs");
                    if(ClassLoaderProcesser.getInstance().containsClassLoader(argsReplace)){
                        ClassLoaderProcesser.getInstance().removeClassLoader(argsReplace);
                    }
                    ExternJarClassHandler classLoader = new ExternJarClassHandler(new URL[]{uri.toURL()}, Thread.currentThread().getContextClassLoader());
                    ApplicationContextUtil.getBeanFactory().setBeanClassLoader(classLoader);
                    Thread.currentThread().setContextClassLoader(classLoader);
                    classLoader.initBean();
                    ClassLoaderProcesser.getInstance().addClassLoader(jarBean.getMainClass(), classLoader);
                    ClassLoaderProcesser.getInstance().addClassLoader(argsReplace, classLoader);
                    count ++ ;
                } catch (Exception e) {
                    log.error("执行注入Jar失败");
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }
        return count;
    }
}
