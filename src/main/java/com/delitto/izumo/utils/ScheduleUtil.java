package com.delitto.izumo.utils;

import com.delitto.izumo.framework.base.scheduler.ScheduleTask;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Log4j2
@Component
public class ScheduleUtil {
    private static ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private static Map<String, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();


    static{
        threadPoolTaskScheduler.initialize();
        log.info("初始化线程池...");
    }

    /**
     * 启动某定时任务，以固定周期运行
     * @param scheduleTask
     * @param period
     */
    public static void start(ScheduleTask scheduleTask, long period){
        if (isExist(scheduleTask.getTaskId())){
            log.warn("启动定时任务["+ scheduleTask.getTaskId()+"]失败，任务已存在");
            return;
        }
        ScheduledFuture<?>scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(scheduleTask,period);
        scheduledFutureMap.put(scheduleTask.getTaskId(),scheduledFuture);
        log.info("启动定时任务[" + scheduleTask.getTaskId() + "]，执行周期为[" + period + "]毫秒");
    }

    /**
     * 修改定时任务执行时间
     * @param scheduleTask
     * @param startTime
     */
    public static void reset(ScheduleTask scheduleTask, Date startTime){
        //先取消定时任务
        String id = scheduleTask.getTaskId();
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(id);
        if(scheduledFuture != null && !scheduledFuture.isCancelled()){
            scheduledFuture.cancel(false);
        }
        scheduledFutureMap.remove(id);
        //然后启动新的定时任务
        scheduledFuture = threadPoolTaskScheduler.schedule(scheduleTask,startTime);
        scheduledFutureMap.put(id,scheduledFuture);
        log.info("重置定时任务["+ id +"]，执行时间为["+ startTime + "]");
    }

    /**
     * 判断某个定时任务是否存在或已经取消
     * @param taskId
     */
    public static Boolean isExist(String taskId) {
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(taskId);
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            return true;
        }
        return false;
    }

}
