package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import com.maihaoche.mazda.utils.gradle.GradleRunner;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;


/**
 * Created by yang on 17/2/10.
 */
public class ActionModuleSet extends AnAction {

    boolean currentAll = true;//当前是全module

    @Override
    public void actionPerformed(AnActionEvent event) {
        //启动一个新的进程来执行。
        try {
            //检查平台是否是Android Studio
            Project project = getEventProject(event);
            if (project == null) {
                throw new NullPointerException("没有找到project");
            }
            //判断平台，要是android studio 平台
            NotificationUtils.info("project不为空，进入actionPerform逻辑");
            if (!PlatformUtils.isAndroidStudio()) {
                NotificationUtils.popError("请在Android Studio 平台下使用此插件", event);
                return;
            } else {
                NotificationUtils.info("当前IDE为Android Studio 平台");
            }
            //执行任务
            if (currentAll) {
//                GradleRunner.runGradleTasks(project, "tasks", new SyncProjectResultHandler(event));
                GradleRunner.runGradleTasks(project, "turnToSeekModule", new SyncProjectResultHandler(event));
                event.getPresentation().setText("toFullModule");
                currentAll = false;
            } else {
//                    GradleRunner.runGradleTasks(project, "init", new SyncProjectResultHandler(event));
                GradleRunner.runGradleTasks(project, "turnToAllModule", new SyncProjectResultHandler(event));
                event.getPresentation().setText("toSingleModule");
                currentAll = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtils.popError("插件运行时抛出异常，具体错误信息e:" + e.getMessage(), event);
        } catch (Error e) {
            NotificationUtils.popError("插件运行出错，具体错误信息e:" + e.getMessage(), event);
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


    /**
     * 显示某个action下的子action
     *
     * @param action
     * @return
     */
    private AnAction[] showSubActions(AnAction action) {
        if (action != null && action instanceof DefaultActionGroup && action.getTemplatePresentation().isEnabledAndVisible()) {
            AnAction[] anActions = ((DefaultActionGroup) action).getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
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
        boolean SyncFound = false;
        if (actionGroup != null) {
            AnAction[] anActions = actionGroup.getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    if ("Android".equals(anActions[i].getTemplatePresentation().getText())) {
                        AnAction[] actions = showSubActions(anActions[i]);
                        if (actions != null && actions.length > 0) {
                            for (int j = 0; j < actions.length; j++) {
                                if (actions[j] != null && "Sync Project with Gradle Files".equals(actions[j].getTemplatePresentation().getText())) {
                                    SyncFound = true;
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
        if (!SyncFound) {
            NotificationUtils.popError("在Tools菜单栏下没有找到action:Sync Project with Gradle Files", event);
        }
    }


    /**
     * 完成后，执行sync project 任务。
     */
    private class SyncProjectResultHandler implements ResultHandler {
        private AnActionEvent event;

        public SyncProjectResultHandler(AnActionEvent event) {
            this.event = event;
        }

        @Override
        public void onComplete(Object o) {
            NotificationUtils.info("任务执行完毕，任务执行输出:" + (o != null ? o.toString() : "null") + ",开始sync整工程");
            performSyncProject(event);
        }

        @Override
        public void onFailure(GradleConnectionException e) {
            NotificationUtils.error("执行命令行出错：e:" + e.getMessage());
        }

    }

}
