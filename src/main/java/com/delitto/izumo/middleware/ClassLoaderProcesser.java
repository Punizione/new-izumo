package com.delitto.izumo.middleware;


import com.delitto.izumo.utils.ApplicationContextUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ClassLoaderProcesser {
    private ClassLoaderProcesser() {}
    private Map<String, ExternJarClassHandler> responsityMap = new ConcurrentHashMap<>();

    public void addClassLoader(String moduleName,ExternJarClassHandler externJarClassHandler){
        responsityMap.put(moduleName, externJarClassHandler);
    }

    public boolean containsClassLoader(String key){
        return responsityMap.containsKey(key);
    }

    public ExternJarClassHandler getClassLoader(String key){
        return responsityMap.get(key);
    }

    public void removeClassLoader(String moduleName){
        ExternJarClassHandler externJarClassHandler = responsityMap.get(moduleName);
        try {
            List<String> registeredBean = externJarClassHandler.getRegisteredBean();
            for (String beanName : registeredBean) {
                log.info("删除bean[{}]", beanName);
                if(ApplicationContextUtil.getBeanFactory().containsBeanDefinition(beanName)) {
                    ApplicationContextUtil.getBeanFactory().removeBeanDefinition(beanName);
                }

            }
            externJarClassHandler.close();
            responsityMap.remove(moduleName);

        } catch (IOException e) {
            log.error("删除[{}]模块发生错误", moduleName);
            e.printStackTrace();
        }
    }

    private static class ClassloaderResponsityHodler{
        private static ClassLoaderProcesser instance = new ClassLoaderProcesser();
    }

    public static ClassLoaderProcesser getInstance(){
        return ClassloaderResponsityHodler.instance;
    }
}
