package com.maihaoche.mazda.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

/**
 * Created by yang on 17/3/17.
 */
public class NotificationUtils {
    private static final NotificationGroup GROUP_DISPLAY_ID_INFO =
            new NotificationGroup("com.maihaoche.mazda",
                    NotificationDisplayType.BALLOON, true);

    public static void info(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = NotificationUtils.GROUP_DISPLAY_ID_INFO.createNotification(errorMsg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notificationX);
    }

    public static void error(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = NotificationUtils.GROUP_DISPLAY_ID_INFO.createNotification(errorMsg, NotificationType.ERROR);
        Notifications.Bus.notify(notificationX);
    }
}
