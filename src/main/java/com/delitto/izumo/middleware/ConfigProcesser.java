package com.delitto.izumo.middleware;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Log4j2
public class ConfigProcesser {
    public static JSONObject frameworkConifg = new JSONObject();
    public static JSONObject pluginConfig = new JSONObject();

    @Value("${bot-config.framework-config-file}")
    String framworkConfigFileName;

    @Value("${bot-config.framework-config-path}")
    String framworkConfigFilePath;

    @Value("${bot-config.plugin-config-file}")
    String pluginConfigFileName;

    @Value("${bot-config.plugin-config-path}")
    String pluginConfigFilePath;


    @PostConstruct
    public void init() {
        log.info("[ConfigProcesser][配置注入]");
        //frameworkConifg = FileUtil.loadJson(framworkConfigFileName, framworkConfigFilePath);
        //pluginConfig = FileUtil.loadJson(pluginConfigFileName, pluginConfigFilePath);
    }

    @PreDestroy
    public void destory() {
        //FileUtil.writeJson(frameworkConifg, framworkConfigFileName, framworkConfigFilePath);
        //FileUtil.writeJson(pluginConfig, pluginConfigFileName, pluginConfigFilePath);
    }


//    @Scheduled(cron = "0 0 0/1 * * ?")
    public void refresh() {
        //每小时执行一次刷新配置
        init();
    }
}
