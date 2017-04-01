package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import com.maihaoche.mazda.utils.gradle.GradleTaskExecutor;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;


/**
 * Created by yang on 17/2/10.
 */
public class ActionSwitchModule extends AnAction {

    private static final String ACTION_TOOLS_MENU = "ToolsMenu";
    private static final String ACTION_ANDROID = "Android";
    private static final String ACTION_SYNC = "Sync Project with Gradle Files";

    private boolean currentAll = true;//当前是全module


    @Override
    public void actionPerformed(AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        presentation.setEnabled(false);
        try {
            //保存。相当于按下ctrl+s.必须执行在主线程
            PlatformUtils.saveAll();
            PlatformUtils.executeBackgroundTask(createModuleSetRunnable(event));
        } catch (Throwable throwable) {
            NotificationUtils.popError("插件运行出错，具体错误信息e:" + throwable.getMessage(), event);
        } finally {
            if (presentation != null) {
                presentation.setEnabled(true);
            }
        }
    }

    /**
     * 创建异步执行的任务
     */
    private Runnable createModuleSetRunnable(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            throw new NullPointerException("没有找到project");
        }
        return () -> {
            NotificationUtils.info("Mazda开始执行", project);
            //检查平台是否是Android Studio
            String taskToSingle = "";
            String taskToAll = "";
            if (!PlatformUtils.isAndroidStudio()) {
                taskToSingle = "tasks";
                taskToAll = "help";
                NotificationUtils.popError("请在Android Studio 平台下使用此插件", event);
            } else {
                taskToSingle = "turnToSeekModule";
                taskToAll = "turnToAllModule";
            }
            //执行任务
            String taskToRun = currentAll ? taskToSingle : taskToAll;
            new GradleTaskExecutor(project, taskToRun, new SyncProjectResultHandler(event, taskToRun)).queue();
        };
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
            throw new NullPointerException("performSyncProject中参数project不能为空");
        }
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction(ACTION_TOOLS_MENU);
        boolean SyncFound = false;
        if (actionGroup != null) {
            AnAction[] anActions = actionGroup.getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    if (ACTION_ANDROID.equals(anActions[i].getTemplatePresentation().getText())) {
                        AnAction[] actions = showSubActions(anActions[i]);
                        if (actions != null && actions.length > 0) {
                            for (int j = 0; j < actions.length; j++) {
                                if (actions[j] != null && ACTION_SYNC.equals(actions[j].getTemplatePresentation().getText())) {
                                    SyncFound = true;
                                    actions[j].actionPerformed(event);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new NullPointerException("没有找到id为" + ACTION_TOOLS_MENU + "的菜单栏");
        }
        if (!SyncFound) {
            NotificationUtils.error("在id为" + ACTION_TOOLS_MENU + "的菜单栏下没有找到Action：" + ACTION_ANDROID + "->" + ACTION_SYNC + "。");
        }
    }


    /**
     * 执行gradle任务的结果回调类
     * 完成后，执行sync project 任务。
     */
    private class SyncProjectResultHandler implements ResultHandler {
        private AnActionEvent event;
        private String mTaskName;

        public SyncProjectResultHandler(AnActionEvent event, String mTaskName) {
            this.event = event;
            this.mTaskName = mTaskName;
        }

        @Override
        public void onComplete(Object o) {
            NotificationUtils.info("Gradle任务\"" + mTaskName + "\"执行完毕，开始sync整工程", getEventProject(event));
            performSyncProject(event);
            if (currentAll) {
                currentAll = false;
                event.getPresentation().setText("切为全Module");
            } else {
                currentAll = true;
                event.getPresentation().setText("切为单Module");
            }
        }

        @Override
        public void onFailure(GradleConnectionException e) {
            NotificationUtils.error("执行Grale任务\"" + mTaskName + "\"时：e:" + e.getMessage());
        }

    }

}
