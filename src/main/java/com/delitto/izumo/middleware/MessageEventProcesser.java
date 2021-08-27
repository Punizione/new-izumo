package com.delitto.izumo.middleware;

import com.beust.jcommander.JCommander;
import com.delitto.izumo.framework.base.msg.MsgBuilder;
import com.delitto.izumo.framework.base.plugin.AbstractPluginBase;
import com.delitto.izumo.framework.base.plugin.ConmandPrefix;
import com.delitto.izumo.framework.base.plugin.PluginBase;
import com.delitto.izumo.framework.base.plugin.PluginConfig;
import com.delitto.izumo.framework.util.ExceptionUtil;
import com.delitto.izumo.framework.util.TypeUtil;
import com.delitto.izumo.service.plugin.base.NLPPlugin;
import com.delitto.izumo.service.plugin.impl.AdminPlugin;
import com.delitto.izumo.utils.ApplicationContextUtil;
import com.delitto.izumo.utils.CommandUtil;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class MessageEventProcesser extends SimpleListenerHost {

    private final Bot bot;
    private final ApplicationContext applicationContext;
    private final Environment enviroment;
    private final Long adminQQ;
    private final String commandPrefix;
    private final String adminPrefix;


    public static Map<String, PluginBase<?>> commandCache = new ConcurrentHashMap<>(4);
    public static Map<String, String> alias2bind = new ConcurrentHashMap<>(10);

    public MessageEventProcesser(ApplicationContext applicationContext, Bot bot) {

        this.bot = bot;
        this.applicationContext = applicationContext;
        enviroment = ApplicationContextUtil.get(Environment.class);
        adminQQ = Long.parseLong(enviroment.getProperty("bot-config.admin"));
        commandPrefix = enviroment.getProperty("bot-config.command-prefix");
        adminPrefix = enviroment.getProperty("bot-config.admin-command-prefix");
        initCommandBind();
    }


    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
        Objects.requireNonNull(bot.getFriend(adminQQ)).sendMessage(
                new MsgBuilder()
                        .text(exception.getMessage())
                        .build()
        );
    }

    @Async
    @EventHandler
    public void onMessage(@NotNull MessageEvent event) throws Exception {
        String msgString = event.getMessage().contentToString();
        if(StringUtils.isNotBlank(msgString)) {
            Contact subject = event.getSubject();
            User sender = event.getSender();

            if(msgString.startsWith(commandPrefix)) {
                String[] commands = CommandUtil.split(msgString);
                if(commands != null) {
                    PluginBase<?> pluginBase = commandCache.get(alias2bind.getOrDefault(commands[0], ""));
                    if(pluginBase == null) {
                        return;
                    }
                    if(pluginBase instanceof AbstractPluginBase) {
                        ((AbstractPluginBase) pluginBase).pluginConfig = new PluginConfig();
                        String proxyIp = enviroment.getProperty("bot-config.proxy-param.server");
                        int proxyPort = Integer.parseInt(enviroment.getProperty("bot-config.proxy-param.port"));
                        ((AbstractPluginBase) pluginBase).pluginConfig.setProxyIP(proxyIp);
                        ((AbstractPluginBase) pluginBase).pluginConfig.setProxyPort(proxyPort);
                        ((AbstractPluginBase) pluginBase).pluginConfig.setImageSavePath(enviroment.getProperty("bot-config.mirai-dir"));
                        ((AbstractPluginBase) pluginBase).pluginConfig.setTempSavePath(enviroment.getProperty("bot-config.temp-dir"));
                    }

                    Method executeMethod1 = PluginBase.class.getMethod("execute", Object.class, Contact.class, User.class);
                    Type actualTypeArgument = getActualTypeArgument(pluginBase);
                    //获得实际类型的实例
                    Object pluginArgs = TypeUtil.getClass(
                            actualTypeArgument,
                            ClassLoaderProcesser.getInstance().getClassLoader(actualTypeArgument.getTypeName())
                    ).getDeclaredConstructor().newInstance();

                    //注入参数
                    JCommander.newBuilder().addObject(pluginArgs).build().parse(commands);
                    try{
                        Boolean flag =  (Boolean) executeMethod1.invoke(pluginBase, pluginArgs, subject, sender);
                        if(!flag) {
                            Method executeMethod2 = PluginBase.class.getMethod("execute", MessageEvent.class);
                            executeMethod2.invoke(pluginBase, event);
                        }
                    } catch (Exception e) {
                        Objects.requireNonNull(bot.getFriend(adminQQ)).sendMessage(new MsgBuilder()
                                .text("内部错误：" + e.getCause().getMessage())
                                .build()
                        );
                        log.error("内部错误：{}", e.getCause().getMessage());
                        e.printStackTrace();
                    }
                }

            } else if(msgString.startsWith(adminPrefix)) {
                AdminPlugin plugin = new AdminPlugin();
                String[] commands = CommandUtil.splitAdmin(msgString);
                if(commands!=null) {
                    try {
                        plugin.execute(bot, event, commands);
                    } catch (Exception e) {
                        Objects.requireNonNull(bot.getFriend(adminQQ)).sendMessage(new MsgBuilder()
                                .text("内部错误：" + e.getMessage())
                                .build()
                        );
                        log.error("内部错误：{}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                NLPPlugin plugin = new NLPPlugin();
                try {
                    plugin.execute(bot, event);
                } catch (Exception e) {
                    Objects.requireNonNull(bot.getFriend(adminQQ)).sendMessage(new MsgBuilder()
                            .text("内部错误：" + e.getCause().getMessage())
                            .build()
                    );
                    log.error("内部错误：{}", e.getCause().getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    private Type getActualTypeArgument(PluginBase<?> commandController){
        // 找到实现接口中的Reply
        for (Type anInterface : commandController.getClass().getGenericInterfaces()) {
            if (anInterface instanceof ParameterizedType){
                if (((ParameterizedType) anInterface).getRawType().getTypeName().equals(PluginBase.class.getTypeName())){
                    // 获得实现时填入的具体参数
                    return ((ParameterizedType) anInterface).getActualTypeArguments()[0];
                }
            }
        }
        // 实际上不会执行到
        return null;
    }

    public void initCommandBind() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(ConmandPrefix.class);
        beansWithAnnotation.forEach((name, bean) -> {
            if(bean instanceof PluginBase) {
                ConmandPrefix annotation = bean.getClass().getAnnotation(ConmandPrefix.class);
                String bind = annotation.bind();
                String[] alias = annotation.alias();

                if (commandCache.containsKey(bind)){
                    String which = commandCache.get(bind).getClass().getSimpleName();
                    throw ExceptionUtil.classError(bean.getClass(), "命令已有其他Plugin绑定(%s)，请考虑修改bind值(%s)", which, bind);
                }
                commandCache.put(bind, (PluginBase<?>) bean);

                for (String alia : alias){
                    if (alias2bind.containsKey(alia)){
                        log.warn(ExceptionUtil.classWarning(bean.getClass(), "别名(%s)已被占用，将忽略"), alia);
                    }else {
                        alias2bind.put(alia, bind);
                    }
                }
                alias2bind.put(bind, bind);

            }
        });
    }

    public static void callCommandBindOuter(){
        Map<String, Object> beansWithAnnotation = ApplicationContextUtil.getApplicationContextOuter().getBeansWithAnnotation(ConmandPrefix.class);
        commandCache.clear();
        alias2bind.clear();
        for(String key: beansWithAnnotation.keySet()) {
            Object bean = beansWithAnnotation.get(key);
            if(bean instanceof PluginBase) {
                ConmandPrefix annotation = bean.getClass().getAnnotation(ConmandPrefix.class);
                String bind = annotation.bind();
                String[] alias = annotation.alias();

                if (commandCache.containsKey(bind)){
                    String which = commandCache.get(bind).getClass().getSimpleName();
                    throw ExceptionUtil.classError(bean.getClass(), "命令已有其他Plugin绑定(%s)，请考虑修改bind值(%s)", which, bind);
                }
                commandCache.put(bind, (PluginBase<?>) bean);

                for (String alia : alias){
                    if (alias2bind.containsKey(alia)){
                        log.warn(ExceptionUtil.classWarning(bean.getClass(), "别名(%s)已被占用，将忽略"), alia);
                    }else {
                        alias2bind.put(alia, bind);
                    }
                }
                alias2bind.put(bind, bind);
            }
        }
//        beansWithAnnotation.forEach((name, bean) -> {
//            if(bean instanceof PluginBase) {
//                ConmandPrefix annotation = bean.getClass().getAnnotation(ConmandPrefix.class);
//                String bind = annotation.bind();
//                String[] alias = annotation.alias();
//
//                if (commandCache.containsKey(bind)){
//                    String which = commandCache.get(bind).getClass().getSimpleName();
//                    throw ExceptionUtil.classError(bean.getClass(), "命令已有其他Plugin绑定(%s)，请考虑修改bind值(%s)", which, bind);
//                }
//                commandCache.put(bind, (PluginBase<?>) bean);
//
//                for (String alia : alias){
//                    if (alias2bind.containsKey(alia)){
//                        log.warn(ExceptionUtil.classWarning(bean.getClass(), "别名(%s)已被占用，将忽略"), alia);
//                    }else {
//                        alias2bind.put(alia, bind);
//                    }
//                }
//                alias2bind.put(bind, bind);
//
//            }
//        });
    }
}
