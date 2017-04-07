package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;

/**
 * Created by yang on 17/4/7.
 */
public abstract class BaseAction extends AnAction {
    @Override
    final public void actionPerformed(AnActionEvent event) {
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

    abstract void onActionPerform(AnActionEvent event) throws Throwable;
}
