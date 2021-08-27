package com.delitto.izumo.handler;

import com.delitto.izumo.middleware.MessageEventProcesser;
import com.delitto.izumo.utils.ContextBeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

@Component
@Log4j2
@Order(value = 5)
//implements CommandLineRunner
public class SpringRunnerLifeCycle implements CommandLineRunner {
    @Value("${bot-config.plugin-dir}")
    private String pluginJarsPath;


    @Override
    public void run(String... args) throws Exception {
        log.info("Spring Application已启动,执行注入Jar操作");
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ContextBeanUtil.reloadSpringBeans(pluginJarsPath);
                MessageEventProcesser.callCommandBindOuter();
            }
        }, 10000 );


    }
}
