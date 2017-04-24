package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;

/**
 * Created by yang on 17/4/7.
 */
public abstract class BaseAction extends AnAction {
    @Override
    final public void actionPerformed(AnActionEvent event) {
        if (!PlatformUtils.isAndroidStudio()) {
            NotificationUtils.popError("请在Android Studio 平台下运行该插件(Please use this plugin on Android Studio.)", event);
            return;
        }
        Presentation presentation = event.getPresentation();
        presentation.setEnabled(false);
        Project project = event.getProject();
        NotificationUtils.info("Mazda开始执行任务", project);
        try {
            onActionPerform(event);
        } catch (Throwable throwable) {
            NotificationUtils.popError("插件运行出错，具体错误信息e:" + throwable.getMessage() + "，请在Android Studio 命令行中执行该任务,查看详细信息", event);
        } finally {
            if (presentation != null) {
                presentation.setEnabled(true);
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        //通过event.getData接口来获取环境数据
        Project project = event.getData(PlatformDataKeys.PROJECT);
        //project还没有初始化完毕
        if (!project.isInitialized()) {
            event.getPresentation().setVisible(false);
        } else {
            event.getPresentation().setVisible(true);
        }
    }

    abstract void onActionPerform(AnActionEvent event) throws Throwable;
}
