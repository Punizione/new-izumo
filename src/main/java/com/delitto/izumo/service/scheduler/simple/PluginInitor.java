package com.delitto.izumo.service.scheduler.simple;

import com.delitto.izumo.middleware.MessageEventProcesser;
import com.delitto.izumo.utils.ContextBeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class PluginInitor {

    @Value("${bot-config.plugin-dir}")
    private String pluginJarsPath;

//    @Scheduled(initialDelay = 1000)
    public void execute() {
        ContextBeanUtil.reloadSpringBeans(pluginJarsPath);
        MessageEventProcesser.callCommandBindOuter();
    }
}
