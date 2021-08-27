package com.delitto.izumo.service.plugin.impl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.delitto.izumo.framework.base.PluginClassBean;
import com.delitto.izumo.framework.base.msg.MsgBuilder;
import com.delitto.izumo.framework.util.FileUtil;
import com.delitto.izumo.middleware.MessageEventProcesser;
import com.delitto.izumo.middleware.SchedulerFactory;
import com.delitto.izumo.utils.ApplicationContextUtil;
import com.delitto.izumo.utils.Constants;
import com.delitto.izumo.utils.ContextBeanUtil;
import lombok.extern.log4j.Log4j2;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.List;


@Log4j2
public class AdminPlugin {
    public void execute(@NotNull Bot bot, @NotNull MessageEvent event, String[] commands) {
        AdminPluginArgs pluginArgs = new AdminPluginArgs();
        JCommander.newBuilder().addObject(pluginArgs).build().parse(commands);
        MsgBuilder retMsg = new MsgBuilder();
        if(StringUtils.isNotBlank(pluginArgs.auEvent)) {
            if(Constants.friendAddEventMap.containsKey(Long.parseLong(pluginArgs.auEvent))) {
                NewFriendRequestEvent newFriendRequestEvent = Constants.friendAddEventMap.remove(Long.parseLong(pluginArgs.auEvent));
                newFriendRequestEvent.accept();
                retMsg.text("已同意[" + pluginArgs.auEvent + "]好友申请");
            } else {
                retMsg.text("添加好友请求[" + pluginArgs.auEvent + "]不存在");
            }
        } else if(StringUtils.isNotBlank(pluginArgs.ruEvent)) {
            if(Constants.friendAddEventMap.containsKey(Long.parseLong(pluginArgs.ruEvent))) {
                NewFriendRequestEvent newFriendRequestEvent = Constants.friendAddEventMap.remove(Long.parseLong(pluginArgs.auEvent));
                newFriendRequestEvent.reject(false);
                retMsg.text("已拒绝[" + pluginArgs.auEvent + "]好友申请");
            } else {
                retMsg.text("添加好友请求[" + pluginArgs.ruEvent + "]不存在");
            }
        } else if(pluginArgs.sjl) {
            String scheduleJarsPath = ApplicationContextUtil.getConfigFromEnvironment("bot-config.schedule-dir");
            List<PluginClassBean> jars = FileUtil.loadJars(scheduleJarsPath);
            retMsg.text("当前已加载定时任务:\n");
            if (jars.size() > 0) {
                for (PluginClassBean classBean : jars) {
                    String scheduleInfo = SchedulerFactory.loadScheduleInfo(classBean);
                    retMsg.text("[" + scheduleInfo + "][" + classBean.getMainClass() + "]\n");
                }
            } else {
                retMsg.text("空\n");
            }
        } else if(pluginArgs.pl) {
            String pluginJarsPath = ApplicationContextUtil.getConfigFromEnvironment("bot-config.plugin-dir");
            List<PluginClassBean> jars = FileUtil.loadJars(pluginJarsPath);
            retMsg.text("当前已加载插件:\n");
            if(jars.size()>0) {
                for (PluginClassBean classBean : jars) {
                    String scheduleInfo = SchedulerFactory.loadScheduleInfo(classBean);
                    retMsg.text("[" + scheduleInfo + "][" + classBean.getResponsePrefix() + "][权限需求:{" + classBean.getNeedPermission() + "}]\n" );
                }
            } else {
                retMsg.text("空\n");
            }
        } else if(pluginArgs.rp) {
            int count = ContextBeanUtil.reloadSpringBeans(ApplicationContextUtil.getConfigFromEnvironment("bot-config.plugin-dir"));
            MessageEventProcesser.callCommandBindOuter();
            retMsg.text("已重载[" + count + "]个插件");
        } else if(pluginArgs.info) {
            retMsg.text("[Admin插件:响应+Admin指令][参数说明]\n");
            retMsg.text("[sjl] 查看定时任务列表\n");
            retMsg.text("[-pl] 查看插件列表\n");
            retMsg.text("[-rp] 重载插件bean\n");
            retMsg.text("[-au|-AgreeUser] [requestId] 同意好友申请\n");
            retMsg.text("[-ru|-RejectUser] [requestId] 拒绝好友申请\n");
            retMsg.text("[-info|-help|-h] 使用帮助\n");
        }
        if(retMsg.size()>0) {
            event.getSubject().sendMessage(retMsg.build());
        }

    }

}
class AdminPluginArgs {
    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "定时任务", "-ScheduleJobList", "-sjl" }, description = "查看定时任务列表")
    boolean sjl = false;

    @Parameter(names = { "插件列表", "-PluginList", "-pl" }, description = "查看插件列表")
    boolean pl = false;

    @Parameter(names = {"-AgreeUser", "-au"}, description = "同意申请")
    String auEvent = "";

    @Parameter(names = {"-RejectUser", "-ru"}, description = "拒绝申请")
    String ruEvent = "";

    @Parameter(names = {"-rp", "-reloadPlugin"}, description = "重载插件bean")
    boolean rp = false;

    @Parameter(names = {"-h", "-help", "-info"}, description = "使用帮助")
    boolean info = false;

    @Parameter(names = "-debug", description = "Debug mode")
    boolean debug = false;
}
