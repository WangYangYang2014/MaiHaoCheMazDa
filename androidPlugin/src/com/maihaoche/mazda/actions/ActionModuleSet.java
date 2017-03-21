package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import org.gradle.tooling.*;
import com.maihaoche.mazda.utils.gradle.GradleKiller;
import com.maihaoche.mazda.utils.gradle.GradleRunner;


/**
 * Created by yang on 17/2/10.
 */
public class ActionModuleSet extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        //检查平台是否是Android Studio
        Project project = getEventProject(event);
        if (project == null) {
            throw new NullPointerException("没有找到project");
        }
        //判断平台，要是android studio 平台
        NotificationUtils.info("project不为空，进入actionPerform逻辑");
        if (!PlatformUtils.isAndroidStudio()) {
            NotificationUtils.error("请在Android Studio 平台下使用此插件");
            return;
        } else {
            NotificationUtils.info("当前IDE为Android Studio 平台");
        }

        //当前有gradle 任务正在执行，提示是否终止。
        if (GradleKiller.isGradleRunning()) {
            NotificationUtils.error("有gradle任务正在执行，停止正在执行的Gradle任务!");
            return;
        }

        //执行任务
        runFullClean(project, new ResultHandler() {
            @Override
            public void onComplete(Object o) {
                NotificationUtils.info("任务执行完毕，开始sync整工程");
                performSyncProject(event);
            }

            @Override
            public void onFailure(GradleConnectionException e) {
                NotificationUtils.error("执行命令行出错：e:" + e.getMessage());
            }
        });
    }

    /**
     * 执行一个gradle 命令 "grealde fullClean"
     *
     * @param project
     * @param resultHandler
     */
    private void runFullClean(Project project, ResultHandler resultHandler) {
        if (project == null) {
            return;
        }
        GradleRunner.runGradleTasks(project, "fullClean", resultHandler);
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


    /**
     * 显示某个action下的子action
     *
     * @param action
     * @return
     */
    private AnAction[] showSubActions(AnAction action) {
        if (action != null && action instanceof DefaultActionGroup && action.getTemplatePresentation().isVisible()) {
            AnAction[] anActions = ((DefaultActionGroup) action).getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    AnAction anAction = anActions[i];
                    String message = action.getTemplatePresentation().getText() + "下的anAction:" + anAction.getTemplatePresentation().getText() + "    "
                            + "isVisible():" + anAction.getTemplatePresentation().isVisible() + "    "
                            + "isEnabled:" + anAction.getTemplatePresentation().isEnabled() + "    "
                            + "isEnabledAndVisible:" + anAction.getTemplatePresentation().isEnabledAndVisible() + "    ";
                    NotificationUtils.info(message);
                }
                return anActions;
            }
        }
        return null;
    }


    /**
     * 执行工具菜单下的 "Sync Project with Gradle Files"动作
     */
    private void performSyncProject(AnActionEvent event) {
        Project project = getEventProject(event);
        if (project == null) {
            throw new NullPointerException("没有找到project");
        }
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("ToolsMenu");
        if (actionGroup != null) {
            AnAction[] anActions = actionGroup.getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    if ("Android".equals(anActions[i].getTemplatePresentation().getText())) {
                        AnAction[] actions = showSubActions(anActions[i]);
                        if (actions != null && actions.length > 0) {
                            for (int j = 0; j < actions.length; j++) {
                                if (actions[j] != null && "Sync Project with Gradle Files".equals(actions[j].getTemplatePresentation().getText())) {
                                    actions[j].actionPerformed(event);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new NullPointerException("没有找到ToolsMenu对应的子item");
        }
    }

}
