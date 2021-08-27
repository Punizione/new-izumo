package com.delitto.izumo.handler;


import com.delitto.izumo.middleware.MessageEventProcesser;
import com.delitto.izumo.utils.ContextBeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;


@Component
@Log4j2
@Order(value = 10)
public class PluginDynamicListener implements CommandLineRunner {
    @Value("${bot-config.plugin-dir}")
    private String pluginJarsPath;


    @Override
    public void run(String... args) throws Exception {
        final Timer timer = new Timer();
        log.info("[动态插件检测器启动]");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                WatchKey key;
                try{
                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    Paths.get(pluginJarsPath).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        //没有文件变更时阻塞
                        key = watchService.take();
                        ContextBeanUtil.reloadSpringBeans(pluginJarsPath);
                        MessageEventProcesser.callCommandBindOuter();
                        break;
//                        if(!key.reset()) {
//
//                        }
                    }
                } catch (Exception e) {
                    log.error("[动态插件检测器异常:{}]", e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 1000, 5000);
    }

}
