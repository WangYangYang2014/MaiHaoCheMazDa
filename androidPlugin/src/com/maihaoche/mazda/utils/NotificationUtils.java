package com.maihaoche.mazda.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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

    private static NotificationUtils mSingleton = new NotificationUtils();

    private NotificationUtils() {
    }

    public static NotificationUtils getSingleton() {
        return mSingleton;
    }


    private static final Logger sPLUGIN_LOGGER = Logger.getInstance(NotificationUtils.class);

    private static final NotificationGroup GROUP_DISPLAY_ID_INFO_BALLOON =
            new NotificationGroup("com.maihaoche.mazda",
                    NotificationDisplayType.BALLOON, true);

    private static final NotificationGroup LOGGING_NOTIFICATION = new NotificationGroup("Gradle sync", NotificationDisplayType.NONE, true);

    /**
     * IntelliJ自带的API输出的info，android studio 会显示在底部statusbar下面。
     *
     * @param info
     */
    public void infoToStatusBar(String info) {
        sPLUGIN_LOGGER.info(info);
    }

    /**
     * 显示在gradle 的consol中的info
     *
     * @param infoMsg
     * @param project
     */
    public static void info(String infoMsg, Project project) {
        if (infoMsg == null || infoMsg.trim().equals("") || project == null) {
            return;
        }
        LOGGING_NOTIFICATION.createNotification(infoMsg, MessageType.INFO).notify(project);
    }

    /**
     * 严重的错误，需要弹窗提醒。
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

    /**
     * 冒气泡显示错误信息
     */
    public static void error(String errorMsg) {
        if (errorMsg == null || errorMsg.trim().equals("")) {
            return;
        }
        com.intellij.notification.Notification notificationX = NotificationUtils.GROUP_DISPLAY_ID_INFO_BALLOON.createNotification(errorMsg, NotificationType.ERROR);
        Notifications.Bus.notify(notificationX);
    }
}
