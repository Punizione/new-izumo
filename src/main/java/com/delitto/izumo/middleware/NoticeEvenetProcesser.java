package com.delitto.izumo.middleware;

import com.delitto.izumo.framework.base.msg.MsgBuilder;
import com.delitto.izumo.utils.ApplicationContextUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.BotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Objects;

public class NoticeEvenetProcesser extends SimpleListenerHost {

    private final Bot bot;
    private final ApplicationContext applicationContext;

    private final Environment enviroment;
    private final Long adminQQ;


    public NoticeEvenetProcesser(ApplicationContext applicationContext, Bot bot) {
        this.bot = bot;
        this.applicationContext = applicationContext;
        enviroment = ApplicationContextUtil.get(Environment.class);
        adminQQ = Long.parseLong(enviroment.getProperty("bot-config.admin"));

    }

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
        Objects.requireNonNull(bot.getFriend(adminQQ)).sendMessage(
                new MsgBuilder()
                        .text(exception.getMessage())
                        .build()
        );
    }

    @EventHandler
    public void onNotice(@NotNull BotEvent event) throws Exception {

    }
}
