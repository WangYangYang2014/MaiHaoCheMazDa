package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.maihaoche.mazda.utils.PlatformUtils;

import static com.maihaoche.mazda.actions.ActionSwitchModule.createGradleWithSyncRunnable;

/**
 * Created by yang on 17/4/7.
 */
public class ActionToAllModule extends BaseAction {

    @Override
    void onActionPerform(AnActionEvent event) {
        PlatformUtils.executeBackgroundTask(createGradleWithSyncRunnable(event, "turnToAllModule", null));
    }
}
