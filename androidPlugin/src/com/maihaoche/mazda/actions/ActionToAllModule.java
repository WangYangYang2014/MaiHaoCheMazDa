package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.PlatformUtils;

import static com.maihaoche.mazda.utils.gradle.GradleRunner.createGradleWithSyncRunnable;

/**
 * Created by yang on 17/4/7.
 */
public class ActionToAllModule extends BaseAction {

    @Override
    void onActionPerform(AnActionEvent event) {
        String taskName = PlatformUtils.getData(MazdaConstants.TO_ALL_MODULE_TASK_NAME, "");
        if (taskName == null || taskName.isEmpty()) {
            ActionConfigure.showConfigureDialog("未配置切换到全Module的任务名，是否现在配置？", event);
            return;
        }
        PlatformUtils.executeBackgroundTask(createGradleWithSyncRunnable(event, taskName, null));
    }
}
