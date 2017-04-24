package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.MazdaUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import com.maihaoche.mazda.utils.gradle.GradleRunner;

import java.util.ArrayList;

/**
 * Created by yang on 17/4/10.
 */
public class ActionToSomeSingle extends BaseAction {

    private String mMainModule = "";
    private String mAARModules = "";

    private String mActionId = "";


    public ActionToSomeSingle(String mMainModule, String mAARModules) {
        this.mMainModule = mMainModule;
        this.mAARModules = mAARModules;
        mActionId = generateActionId(mMainModule, mAARModules);
    }

    @Override
    void onActionPerform(AnActionEvent event) throws Throwable {
        if (MazdaUtils.configureOK(event)) {
            ArrayList<String> arguments = new ArrayList<>();
            arguments.add("-P" + MazdaUtils.sTaskPMain + "=" + mMainModule);
            if (mAARModules != null && mAARModules.length() > 0) {
                arguments.add("-P" + MazdaUtils.sTaskPAAR + "=" + mAARModules);
            }
            PlatformUtils.executeBackgroundTask(GradleRunner.createGradleWithSyncRunnable(event, MazdaUtils.sToSingleTaskName, arguments));
        }
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (presentation != null) {
            presentation.setText(mMainModule);
            String[] savedSettings = PlatformUtils.getDatas(MazdaConstants.SAVED_SINGLE_MODULE_SETTING);
            boolean contain = false;
            if (savedSettings != null && savedSettings.length > 0) {
                for (int i = 0; i < savedSettings.length; i++) {
                    if (savedSettings[i].equals(getActionId())) {
                        contain = true;
                        break;
                    }
                }
            }
            presentation.setVisible(contain);
        }

    }

    public String getActionId() {
        return mActionId;
    }

    public String getMainModule() {
        return mMainModule;
    }

    /**
     * 生成actionId
     */
    public static String generateActionId(String mainModule, String aarModules) {
        return mainModule + "&" + aarModules;
    }

    /**
     * 从一个actionId获取显示该Action得 mMainModule
     */
    public static String getModuleNameFromId(String actionId) {
        if (actionId == null || actionId.isEmpty()) {
            return "";
        }
        if (actionId.contains("&")) {
            return actionId.substring(0, actionId.indexOf("&"));
        } else {
            return actionId;
        }
    }

    /**
     * 从一个actionId获取显示该Action得 mAARModules
     */
    public static String getAARsFromId(String actionId) {
        if (actionId == null || actionId.isEmpty()) {
            return "";
        }
        if (actionId.contains("&")) {
            return actionId.substring(actionId.indexOf("&"), actionId.length());
        } else {
            return actionId;
        }
    }


    /**
     * 注册action
     *
     * @param actionToSomeSingle
     * @param project
     * @return
     */
    public static boolean registerAction(ActionToSomeSingle actionToSomeSingle, Project project) {
        //先注册
        if (ActionManager.getInstance().getAction(actionToSomeSingle.getActionId()) == null) {
            ActionManager.getInstance().registerAction(actionToSomeSingle.getActionId(), actionToSomeSingle);
            return true;
        }
        return false;
    }
}
