package com.delitto.izumo.utils;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.event.events.*;

@Log4j2
public class EventUtil {

    public static boolean isNoticeEvent(BotEvent event) {
        if(event!=null) {
            return event instanceof NewFriendRequestEvent ||
                    event instanceof MemberJoinEvent ||
                    event instanceof MemberLeaveEvent ||
                    event instanceof BotInvitedJoinGroupRequestEvent ||
                    event instanceof BotJoinGroupEvent ||
                    event instanceof MessageEvent;
        }
        return false;
    }
}
