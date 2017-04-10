package com.maihaoche.mazda.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.actions.ActionConfigure;
import com.maihaoche.mazda.constant.MazdaConstants;

/**
 * Created by yang on 17/4/10.
 */
public class MazdaUtils {

    public static String sToSingleTaskName = "";//任务名
    public static String sTaskPMain = "";//任务中配置主module的参数名
    public static String sTaskPAAR = "";//任务中配置aar依赖的参数名

    /**
     * 根据路径，执行某个命令。最多三个层级。既：ToolsMenu-->Android-->Sync...
     */
    public static void performActionWithPath(AnActionEvent event, String menuName, String... paths) {
        if (menuName == null || menuName.isEmpty()) {
            return;
        }
        String firstLevelPath = "";
        String secondLevelPath = "";
        if (paths != null && paths.length > 0) {
            firstLevelPath = paths[0];
            //至少有1级深度，切不为空
            if (firstLevelPath == null || firstLevelPath.isEmpty()) {
                NotificationUtils.error("在id为" + menuName + "的菜单栏,paths的第一级路径为空。");
                return;
            }
            if (paths.length > 1) {
                secondLevelPath = paths[1];
            }
        } else {
            NotificationUtils.error("在id为" + menuName + "的菜单栏,paths为空。");
            return;
        }
        Project project = AnAction.getEventProject(event);
        if (project == null) {
            throw new NullPointerException("performSyncProject中参数project不能为空");
        }
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction(menuName);
        boolean found = false;
        if (actionGroup != null) {
            AnAction[] anActions = actionGroup.getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    if (anActions[i].getTemplatePresentation() != null
                            && firstLevelPath.equals(anActions[i].getTemplatePresentation().getText())) {
                        if (paths.length == 1) {
                            anActions[i].actionPerformed(event);
                            return;
                        } else if (secondLevelPath == null || secondLevelPath.isEmpty()) {
                            return;
                        }
                        AnAction[] subActions = null;
                        if (anActions[i] != null && anActions[i] instanceof DefaultActionGroup && anActions[i].getTemplatePresentation().isEnabledAndVisible()) {
                            subActions = ((DefaultActionGroup) anActions[i]).getChildActionsOrStubs();
                        }
                        if (subActions != null && subActions.length > 0) {
                            for (int j = 0; j < subActions.length; j++) {
                                if (subActions[j] != null && secondLevelPath.equals(subActions[j].getTemplatePresentation().getText())) {
                                    found = true;
                                    subActions[j].actionPerformed(event);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new NullPointerException("没有找到id为" + menuName + "的菜单栏");
        }
        if (!found) {
            NotificationUtils.error("在id为" + menuName + "的菜单栏下没有找到Action：" + firstLevelPath + (secondLevelPath == null || secondLevelPath.isEmpty() ? "" : "->" + secondLevelPath));
        }
    }

    /**
     * 检查任务相关的配置数据是否ok
     */
    public static boolean configureOK(AnActionEvent event) {
        sToSingleTaskName = PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_TASK_NAME, "");
        sTaskPMain = PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_KEY_MAIN, "");
        sTaskPAAR = PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_KEY_AAR, "");
        String hint = "";
        if (sToSingleTaskName == null || sToSingleTaskName.isEmpty()) {
            hint = "未配置切换到单module的任务名，是否现在配置？";
        } else if (sTaskPMain == null || sTaskPMain.isEmpty()) {
            hint = "未配置切换到单module任务中主module对应的参数名，是否现在配置？";
        } else if (sTaskPAAR == null || sTaskPAAR.isEmpty()) {
            hint = "未配置切换到单module任务中AAR依赖对应的参数名，是否现在配置？";
        }
        if (hint == null || hint.isEmpty()) {
            return true;
        }
        ActionConfigure.showConfigureDialog(hint, event);
        return false;
    }
}
