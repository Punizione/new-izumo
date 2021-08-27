package com.delitto.izumo.utils;

import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {
    public static Map<Long, NewFriendRequestEvent> friendAddEventMap = new ConcurrentHashMap<>();
    public static Map<String, MemberJoinEvent> groupAddEventMap = new ConcurrentHashMap<>();
}
