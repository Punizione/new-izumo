package com.delitto.izumo.service.plugin.base;

import lombok.extern.log4j.Log4j2;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

@Log4j2
public class NLPPlugin {
    public void execute(Bot bot, MessageEvent event){
        if(event instanceof GroupMessageEvent) {

        }
    }
}
