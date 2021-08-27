package com.delitto.izumo.utils;


import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommandUtil {

    /**
     * 指令切割
     *
     * @param command
     * @return
     */
    public static String[] split(String command) {
        Environment enviroment = ApplicationContextUtil.get(Environment.class);
        String commandPrefix = enviroment.getProperty("bot-config.command-prefix");
        if (StringUtils.isNotBlank(command) && command.startsWith(commandPrefix)) {
            String[] ret = command.replaceAll(commandPrefix, "").split(" ");
            if (ret.length == 0) {
                return null;
            }
            return ret;
        }
        return null;
    }

    public static String[] splitAdmin(String command) {
        Environment enviroment = ApplicationContextUtil.get(Environment.class);
        String commandPrefix = enviroment.getProperty("bot-config.admin-command-prefix");
        if (StringUtils.isNotBlank(command) && command.startsWith(commandPrefix)) {
            String[] ret = command.substring(5).split(" ");
            if (ret.length == 0) {
                return null;
            }
            return ret;
        }
        return null;
    }

    public static boolean isPureImage(String msg) {
        if(StringUtils.isNotBlank(msg)) {
            if(msg.contains("[CQ:image,file=")) {
                Pattern pattern = Pattern.compile("^\\[CQ:image,file=\\w+\\.\\w+,url=https://gchat.qpic.cn/gchatpic_new/\\d+/[\\w-]+/\\d\\?term=\\d\\]$");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPureImage(MessageChain messageChain) {
        boolean flag = false;
        if(messageChain.size() > 0 ) {
            flag = true;
            for (Message msg : messageChain) {
                if (!(msg instanceof Image)) {
                    flag = false;
                }
            }
        }
        return flag;
    }

    public static String getImageUrl(MessageChain messageChain) {
        if(messageChain!=null) {
            for(Message msg: messageChain) {
                if(msg instanceof Image) {
                    return Image.queryUrl(((Image) msg));
                }
            }
        }
        return null;
    }

}
