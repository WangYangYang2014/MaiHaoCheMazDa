package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.externalSystem.util.ExternalSystemUiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import com.maihaoche.mazda.utils.gradle.GradleTaskExecutor;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


/**
 * Created by yang on 17/2/10.
 */
public class ActionSwitchModule extends BaseAction {

    private static final String ACTION_TOOLS_MENU = "ToolsMenu";
    private static final String ACTION_ANDROID = "Android";
    private static final String ACTION_SYNC = "Sync Project with Gradle Files";

    @Override
    void onActionPerform(AnActionEvent event) throws IllegalAccessException {
        Project project = event.getProject();
        //保存。相当于按下ctrl+s.必须执行在主线程
        PlatformUtils.saveAll();
        //执行转换为全部
        String taskName = "turnToSingleModule";
        String PMain = "singleModule";
        String PAAR = "requiredModules";
        String mainAPPName = "";
        String aarModuleName = "";
        //输入task的名字,p的key值

        //显示所有module的弹窗，让用户选择main app
        ArrayList<String> allModules = PlatformUtils.getProjectModules(project);
        ArrayList<String> mainAppNames = chooseModule(project, "请选择独立编译的module", allModules, true);
        if (mainAppNames != null && mainAppNames.size() > 0) {
            mainAPPName = mainAppNames.get(0);
        } else {
            NotificationUtils.info("任务取消", project);
            return;
        }
        allModules.remove(mainAPPName);
        //显示除了main app外的所有module的弹窗，让用户选择aar依赖的module
        ArrayList<String> aarModuleNames = chooseModule(project, "请选择ARR依赖的module", allModules, false);
        if (aarModuleNames != null && aarModuleNames.size() > 0) {
            for (String moduleName :
                    aarModuleNames) {
                if (aarModuleName.length() > 0) {
                    aarModuleName += "-" + moduleName;
                } else {
                    aarModuleName = moduleName;
                }
            }
        }
        //点击了ok，执行任务
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("-P" + PMain + "=" + mainAPPName);
        if (aarModuleName != null && aarModuleName.length() > 0) {
            arguments.add("-P" + PAAR + "=" + aarModuleName);
        }
        NotificationUtils.info("taskName：" + taskName + "，arguments：" + arguments.toString(), project);
        PlatformUtils.executeBackgroundTask(createGradleWithSyncRunnable(event, taskName, arguments));
    }


    /**
     * 弹窗选择
     *
     * @param project
     * @param title
     * @param allModules
     * @param singleChoose
     * @return
     */
    private ArrayList<String> chooseModule(Project project, String title, ArrayList<String> allModules, boolean singleChoose) {
        ArrayList<String> mChoosedModuleNames = new ArrayList<>();
        final JPanel content = new JPanel(new GridBagLayout());
        content.add(new JLabel(title), ExternalSystemUiUtil.getFillLineConstraints(0));
        final CheckBoxList<String> orphanModulesList = new CheckBoxList<String>();
        orphanModulesList.setSelectionMode(singleChoose ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        orphanModulesList.setItems(allModules, moduleName -> moduleName);
        orphanModulesList.setBorder(IdeBorderFactory.createEmptyBorder(8));
        content.add(orphanModulesList, ExternalSystemUiUtil.getFillLineConstraints(0));
        content.setBorder(IdeBorderFactory.createEmptyBorder(0, 0, 8, 0));

        DialogWrapper dialog = new DialogWrapper(project) {
            {
                setTitle("选择module");
                init();
            }

            @Nullable
            @Override
            protected JComponent createCenterPanel() {
                return new JBScrollPane(content);
            }

            @NotNull
            protected Action[] createActions() {
                return new Action[]{getOKAction()};
            }
        };

        dialog.showAndGet();

        for (int i = 0; i < allModules.size(); i++) {
            String module = allModules.get(i);
            if (orphanModulesList.isItemSelected(i)) {
                mChoosedModuleNames.add(module);
            }
        }
        return mChoosedModuleNames;
    }


    /**
     * 创建异步执行的任务
     */
    public static Runnable createGradleWithSyncRunnable(AnActionEvent event, String taskName, ArrayList<String> properties) {
        Project project = event.getProject();
        if (project == null) {
            throw new NullPointerException("没有找到project");
        }
        return () -> new GradleTaskExecutor(project, taskName, properties, new SyncProjectResultHandler(event, taskName)).queue();
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
    private static AnAction[] showSubActions(AnAction action) {
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

    private static void performSyncProject(AnActionEvent event) {
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
    private static class SyncProjectResultHandler implements ResultHandler {
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
        }

        @Override
        public void onFailure(GradleConnectionException e) {
            NotificationUtils.error("执行Grale任务\"" + mTaskName + "\"时：e:" + e.getMessage());
        }

    }

}
