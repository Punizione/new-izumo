package com.delitto.izumo.middleware;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase;
import org.jetbrains.annotations.Nullable;

@Log4j2
public class LogProcessor  extends MiraiLoggerPlatformBase {
    @Override
    protected void debug0(@Nullable String s, @Nullable Throwable throwable) {
        log.debug(s, throwable);
    }

    @Override
    protected void error0(@Nullable String s, @Nullable Throwable throwable) {
        log.error(s, throwable);
    }

    @Override
    protected void info0(@Nullable String s, @Nullable Throwable throwable) {
        log.info(s, throwable);
    }

    @Override
    protected void verbose0(@Nullable String s, @Nullable Throwable throwable) {
        log.info(s, throwable);
    }

    @Override
    protected void warning0(@Nullable String s, @Nullable Throwable throwable) {
        log.warn(s, throwable);
    }

    @Override
    public @Nullable String getIdentity() {
        return "Izumo-Platform";
    }
}
