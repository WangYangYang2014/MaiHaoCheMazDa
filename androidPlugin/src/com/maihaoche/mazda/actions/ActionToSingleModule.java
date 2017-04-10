package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUiUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.MazdaUtils;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import com.maihaoche.mazda.utils.gradle.GradleRunner;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;


/**
 * Created by yang on 17/2/10.
 */
public class ActionToSingleModule extends BaseAction {

    @Override
    void onActionPerform(AnActionEvent event) throws IllegalAccessException {
        if (!MazdaUtils.configureOK(event)) {
            return;
        }
        Project project = event.getProject();
        //保存。相当于按下ctrl+s.必须执行在主线程
        PlatformUtils.saveAll();
        //显示所有module的弹窗，让用户选择main app
        new GetProjectModulesTask(project, "Mazda正在获取工程module", false, new ResultHandler<ArrayList<String>>() {
            @Override
            public void onComplete(ArrayList<String> allModules) {
                UIUtil.invokeAndWaitIfNeeded((Runnable) () -> ApplicationManager.getApplication().invokeLater(() -> onProjectModulesGet(allModules, event)));
            }

            @Override
            public void onFailure(GradleConnectionException e) {
                throw new NullPointerException("无法找到工程中的module");
            }
        }) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                ArrayList<String> allModules = PlatformUtils.getProjectModules(project);
                if (progressIndicator != null && !progressIndicator.isCanceled()) {
                    progressIndicator.stop();
                }

                if (resultHandler != null) {
                    if (allModules != null && allModules.size() > 0) {
                        resultHandler.onComplete(allModules);
                    } else {
                        resultHandler.onFailure(null);
                    }
                }

            }
        }
                .queue();
    }

    /**
     * 扫描到所有的module之后
     */
    private void onProjectModulesGet(ArrayList<String> allModules, AnActionEvent event) {
        if (allModules == null || allModules.size() == 0 || event == null || event.getProject() == null) {
            return;
        }
        //执行转换为全部
        Project project = event.getProject();
        String mainAPPName = "";
        String aarModules = "";
        ArrayList<String> mainAppNames = chooseModule(project, "请选择独立编译的module", allModules, true);
        if (mainAppNames != null && mainAppNames.size() > 0) {
            mainAPPName = mainAppNames.get(0);
        } else {
            NotificationUtils.info("任务取消", project);
            return;
        }
        allModules.remove(mainAPPName);
        //显示除了main app外的所有module的弹窗，让用户选择aar依赖的module
        ArrayList<String> selectedAARModules = allModules.size() > 0 ? chooseModule(project, "请选择ARR依赖的module", allModules, false) : null;
        if (selectedAARModules != null && selectedAARModules.size() > 0) {
            for (String moduleName :
                    selectedAARModules) {
                if (aarModules.length() > 0) {
                    aarModules += "-" + moduleName;
                } else {
                    aarModules = moduleName;
                }
            }
        }
        //点击了ok，执行任务
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("-P" + MazdaUtils.sTaskPMain + "=" + mainAPPName);
        if (aarModules != null && aarModules.length() > 0) {
            arguments.add("-P" + MazdaUtils.sTaskPAAR + "=" + aarModules);
        }
        PlatformUtils.executeBackgroundTask(GradleRunner.createGradleWithSyncRunnable(event, MazdaUtils.sToSingleTaskName, arguments));
        //讲最新的一次配置保存。
        ActionToSomeSingle actionToSomeSingle = new ActionToSomeSingle(mainAPPName, aarModules);
        if (isNewSetting(mainAPPName, actionToSomeSingle.getActionId())) {
            //先注册
            if (ActionToSomeSingle.registerAction(actionToSomeSingle, project)) {
                //再添加到group中
                ActionGroupMazda.addToThisGroup(actionToSomeSingle);
            }
        }
    }

    /**
     * 判断是否是新的配置，如果是，则保存下来。
     */
    private boolean isNewSetting(String mainAPPName, String newActionId) {
        String[] savedSettings = PlatformUtils.getDatas(MazdaConstants.SAVED_SINGLE_MODULE_SETTING);
        if (savedSettings != null && savedSettings.length > 0) {
            boolean found = false;
            String[] settingsTemp = new String[savedSettings.length + 1];
            for (int i = 0; i < savedSettings.length; i++) {
                String actionId = savedSettings[i];
                if (ActionToSomeSingle.getModuleNameFromId(actionId).equals(mainAPPName)) {
                    found = true;
                    if (savedSettings[i].equals(newActionId)) {
                        return false;
                    }
                    savedSettings[i] = newActionId;
                    break;
                } else {
                    settingsTemp[i] = savedSettings[i];
                }
            }
            if (!found) {
                settingsTemp[savedSettings.length] = newActionId;
                savedSettings = settingsTemp;
            }
        } else {
            savedSettings = new String[]{newActionId};
        }
        PlatformUtils.setDatas(MazdaConstants.SAVED_SINGLE_MODULE_SETTING, savedSettings);
        return true;
    }

    /**
     * 弹窗选择,单选，则是radiogroup组件。复选，则是checkbox组件
     *
     * @param project
     * @param title
     * @param allModules
     * @param singleChoose
     * @return
     */
    private ArrayList<String> chooseModule(Project project, String title, ArrayList<String> allModules, boolean singleChoose) {
        ArrayList<String> mChoosedModuleNames = new ArrayList<>();
        //布局
        final JPanel content = new JPanel(new GridBagLayout());
        content.add(new JLabel(title), ExternalSystemUiUtil.getFillLineConstraints(0));
        final ArrayList<JRadioButton> radioButtons = new ArrayList<>();
        final CheckBoxList<String> orphanModulesList = new CheckBoxList<>();
        if (singleChoose) {
            Box box = Box.createVerticalBox();
            for (int i = 0; i < allModules.size(); i++) {
                JRadioButton radioButton = new JRadioButton(allModules.get(i));
                radioButtons.add(radioButton);
                radioButton.addItemListener(e -> {
                    //选中这个，取消其他的选中
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        for (int j = 0; j < radioButtons.size(); j++) {
                            if (radioButtons.get(j) != radioButton) {
                                radioButtons.get(j).setSelected(false);
                            }
                        }
                    }
                });
                box.add(radioButton);
            }
            box.setBorder(IdeBorderFactory.createEmptyBorder(8));
            content.add(box, ExternalSystemUiUtil.getFillLineConstraints(0));
        } else {
            orphanModulesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            orphanModulesList.setItems(allModules, moduleName -> moduleName);
            orphanModulesList.setBorder(IdeBorderFactory.createEmptyBorder(8));
            content.add(orphanModulesList, ExternalSystemUiUtil.getFillLineConstraints(0));
        }
        content.setBorder(IdeBorderFactory.createEmptyBorder(0, 0, 8, 0));

        DialogWrapper dialog = new DialogWrapper(project) {
            {
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

        boolean okClicked = dialog.showAndGet();

        if (!okClicked) {
            return null;
        }
        if (singleChoose) {
            for (int i = 0; i < radioButtons.size(); i++) {
                if (radioButtons.get(i).isSelected()) {
                    mChoosedModuleNames.add(radioButtons.get(i).getText());
                }
            }
        } else {
            for (int i = 0; i < allModules.size(); i++) {
                String module = allModules.get(i);
                if (orphanModulesList.isItemSelected(i)) {
                    mChoosedModuleNames.add(module);
                }
            }
        }
        return mChoosedModuleNames;
    }

    /**
     * 扫描工程，获取project
     */
    private abstract class GetProjectModulesTask extends Task.Backgroundable {
        protected ResultHandler<ArrayList<String>> resultHandler = null;

        public GetProjectModulesTask(@Nullable Project project, @Nls @NotNull String title, boolean canBeCancelled, ResultHandler<ArrayList<String>> resultHandler) {
            super(project, title, canBeCancelled);
            this.resultHandler = resultHandler;
        }
    }

}
