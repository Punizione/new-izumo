package com.delitto.izumo.handler;

import com.delitto.izumo.middleware.MessageEventProcesser;
import com.delitto.izumo.middleware.LogProcessor;
import com.delitto.izumo.middleware.NoticeEvenetProcesser;
import com.delitto.izumo.utils.ApplicationContextUtil;
import com.delitto.izumo.utils.ContextBeanUtil;
import com.delitto.izumo.utils.EventUtil;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoggerAdapters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Log4j2
@Order(value = 1)
public class BotHandler implements ApplicationRunner {

    private Bot bot = null;
    private final ApplicationContext context;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Value("${bot-config.qq}")
    private long qq;

    @Value("${bot-config.password}")
    private String password;

    @Value("${bot-config.log.netlog-output}")
    private String netLogPath;

    @Value("${bot-config.device-info}")
    private String deviceInfo;

    public BotHandler(ApplicationContext context){
        this.context = context;
    }

    public Bot getBot() {
        return bot;
    }

    @Override
    public void run(ApplicationArguments args) {
        startBot();
    }

    @PreDestroy
    private void destroy(){
        executorService.shutdownNow();
    }

    public void startBot(){
        if (bot != null) {
            return;
        }

        Bot bot = BotFactory.INSTANCE.newBot(qq, password, new BotConfiguration() {
            {
                //???????????????????????????
                fileBasedDeviceInfo(deviceInfo);
                //??????????????????
                setProtocol(MiraiProtocol.ANDROID_WATCH);
                setBotLoggerSupplier(bot -> new LogProcessor());
//                LoggerAdapters.useLog4j2();
                // ??????????????????????????????
                redirectNetworkLogToDirectory(new File(netLogPath));


            }
        });

        this.bot = bot;
        executorService.submit(()->{
            bot.login();
            //??????????????????
            bot.getEventChannel()
                    .filter(botEvent -> botEvent instanceof MessageEvent)
                    .registerListenerHost(new MessageEventProcesser(context, bot));

            //??????????????????
            bot.getEventChannel()
                    .filter(EventUtil::isNoticeEvent)
                    .registerListenerHost(new NoticeEvenetProcesser(context, bot));
            bot.join();
        });
    }

}
