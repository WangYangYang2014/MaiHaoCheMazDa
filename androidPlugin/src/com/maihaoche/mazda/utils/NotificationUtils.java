package com.maihaoche.mazda.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

/**
 * Created by yang on 17/3/17.
 */
public class NotificationUtils {
    private static final NotificationGroup GROUP_DISPLAY_ID_INFO_BALLOON =
            new NotificationGroup("com.maihaoche.mazda",
                    NotificationDisplayType.NONE, true);

    public static void info(String infoMsg) {
        if (infoMsg == null || infoMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = NotificationUtils.GROUP_DISPLAY_ID_INFO_BALLOON.createNotification(infoMsg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notificationX);
    }

    /**
     * 严重的错误，需要弹窗提醒。
     *
     * @param errorMsg
     * @param event
     */
    public static void popError(String errorMsg, AnActionEvent event) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        StatusBar statusBar = WindowManager.getInstance()
                .getStatusBar(DataKeys.PROJECT.getData(event.getDataContext()));
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(errorMsg, MessageType.ERROR, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.atRight);
    }

    public static void error(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = NotificationUtils.GROUP_DISPLAY_ID_INFO_BALLOON.createNotification(errorMsg, NotificationType.ERROR);
        Notifications.Bus.notify(notificationX);
    }
}
