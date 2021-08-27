package com.delitto.izumo.middleware;


import com.delitto.izumo.framework.base.PluginClassBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@Component
@Log4j2
public class SchedulerFactory {
    public static String loadScheduleInfo(PluginClassBean classBean)  {
        try {
            URL url = new URL("file:" + classBean.getPath());
            URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());
            Class<?> clazz = classLoader.loadClass(classBean.getMainClass());
            Method getInfo = clazz.getMethod("getInfo");
            return (String)getInfo.invoke(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception nsfe) {
            return "";
        }
    }
}
