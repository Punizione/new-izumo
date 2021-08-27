package com.delitto.izumo.middleware;

import com.delitto.izumo.framework.base.plugin.ConmandPrefix;
import com.delitto.izumo.utils.ApplicationContextUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log4j2
public class ExternJarClassHandler extends URLClassLoader {
    //属于本类加载器加载的jar包
    private JarFile jarFile;

    //保存已经加载过的Class对象
    private Map<String,Class> cacheClassMap = new HashMap<>();

    //保存本类加载器加载的class字节码
    private Map<String,byte[]> classBytesMap = new HashMap<>();

    //需要注册的spring bean的name集合
    private List<String> registeredBean = new ArrayList<>();

    
    public ExternJarClassHandler(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        URL url = urls[0];
        String path = url.getPath();
        try {
            jarFile = new JarFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化类加载器执行类加载
        init();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(findLoadedClass(name)==null){
            return super.loadClass(name);
        }else{
            return cacheClassMap.get(name);
        }

    }

    private void init() {
        Enumeration<JarEntry> en = jarFile.entries();
        InputStream input = null;
        try{
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String name = je.getName();
                //这里添加了路径扫描限制
                if (name.endsWith(".class")) {
                    String className = name.replace(".class", "").replaceAll("/", ".");
                    input = jarFile.getInputStream(je);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int bufferSize = 4096;
                    byte[] buffer = new byte[bufferSize];
                    int bytesNumRead = 0;
                    while ((bytesNumRead = input.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesNumRead);
                    }
                    byte[] classBytes = baos.toByteArray();
                    classBytesMap.put(className,classBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //将jar中的每一个class字节码进行Class载入
        for (Map.Entry<String, byte[]> entry : classBytesMap.entrySet()) {
            String key = entry.getKey();
            Class<?> aClass = null;
            try {
                aClass = loadClass(key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            cacheClassMap.put(key,aClass);
        }

    }

    public void initBean(){
        for (Map.Entry<String, Class> entry : cacheClassMap.entrySet()) {
            String className = entry.getKey();
            Class<?> cla = entry.getValue();
            if(isCommandPrefixBeanClass(cla)){
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cla);
                BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
                //设置当前bean定义对象是单利的
                beanDefinition.setScope("singleton");

                //将变量首字母置小写
                String beanName = StringUtils.uncapitalize(className);

                beanName =  beanName.substring(beanName.lastIndexOf(".")+1);
                beanName = StringUtils.uncapitalize(beanName);

                ApplicationContextUtil.getBeanFactory().registerBeanDefinition(beanName,beanDefinition);
                registeredBean.add(beanName);
                log.info("注册bean[" + beanName + "]");
            }
        }
    }

    public List<String> getRegisteredBean() {
        return registeredBean;
    }

    public boolean isSpringBeanClass(Class<?> cla){
        if(cla==null){
            return false;
        }
        //是否是接口
        if(cla.isInterface()){
            return false;
        }

        //是否是抽象类
        if( Modifier.isAbstract(cla.getModifiers())){
            return false;
        }

        if(cla.getAnnotation(Component.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Repository.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Service.class)!=null){
            return true;
        }

        return false;
    }

    public boolean isCommandPrefixBeanClass(Class<?> cla){
        if(cla==null){
            return false;
        }
        //是否是接口
        if(cla.isInterface()){
            return false;
        }

        //是否是抽象类
        if( Modifier.isAbstract(cla.getModifiers())){
            return false;
        }

        if(cla.getAnnotation(ConmandPrefix.class)!=null){
            return true;
        }

        return false;
    }

}
