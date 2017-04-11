package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.util.ExternalSystemUiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.MazdaUtils;
import com.maihaoche.mazda.utils.NotificationUtils;
import com.maihaoche.mazda.utils.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by yang on 17/4/10.
 */
public class ActionConfigure extends BaseAction {

    @Override
    void onActionPerform(AnActionEvent event) throws Throwable {
        Project project = event.getProject();
        //总的内容
        final JPanel content = new JPanel(new GridBagLayout());
        //设置切换为全module任务名
        content.add(new JLabel("请输入切换为全module的任务名:        "), ExternalSystemUiUtil.getFillLineConstraints(0));
        JTextField toAllTaskNameInput = getTextField(PlatformUtils.getData(MazdaConstants.TO_ALL_MODULE_TASK_NAME, ""));
        content.add(toAllTaskNameInput, ExternalSystemUiUtil.getFillLineConstraints(0));
        //设置切换为单module任务名
        content.add(new JLabel("请输入切换为单module的任务名:        "), ExternalSystemUiUtil.getFillLineConstraints(0));
        JTextField toSingleTaskNameInput = getTextField(PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_TASK_NAME, ""));
        content.add(toSingleTaskNameInput, ExternalSystemUiUtil.getFillLineConstraints(0));
        //设置切换为单module的参数名.
        content.add(new JLabel("请输入切换为单module的任务中，主模块对应的参数名:        "), ExternalSystemUiUtil.getFillLineConstraints(0));
        JTextField taskKeyMain = getTextField(PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_KEY_MAIN, ""));
        content.add(taskKeyMain, ExternalSystemUiUtil.getFillLineConstraints(0));
        content.add(new JLabel("请输入切换为单module的任务中，AAR依赖对应的参数名:        "), ExternalSystemUiUtil.getFillLineConstraints(0));
        JTextField taskKeyAAR = getTextField(PlatformUtils.getData(MazdaConstants.TO_SINGLE_MODULE_KEY_AAR, ""));
        content.add(taskKeyAAR, ExternalSystemUiUtil.getFillLineConstraints(0));

        //清空缓存
        JButton clearCacheBtn = new JButton("清楚缓存");
        clearCacheBtn.addActionListener(e -> {
            PlatformUtils.setDatas(MazdaConstants.SAVED_SINGLE_MODULE_SETTING, null);
            NotificationUtils.info("清楚缓存成功", project);
            ActionGroupMazda.updateOutside(event);
        });
        clearCacheBtn.setBorder(IdeBorderFactory.createEmptyBorder(14, 4, 4, 4));
        content.add(clearCacheBtn, ExternalSystemUiUtil.getLabelConstraints(0));
        //显示dialog
        content.setBorder(IdeBorderFactory.createEmptyBorder(0, 0, 8, 0));

        DialogWrapper dialog = new DialogWrapper(project) {
            {
                setTitle("配置参数");
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
        if (okClicked) {
            PlatformUtils.setData(MazdaConstants.TO_ALL_MODULE_TASK_NAME, toAllTaskNameInput.getText());
            PlatformUtils.setData(MazdaConstants.TO_SINGLE_MODULE_TASK_NAME, toSingleTaskNameInput.getText());
            PlatformUtils.setData(MazdaConstants.TO_SINGLE_MODULE_KEY_MAIN, taskKeyMain.getText());
            PlatformUtils.setData(MazdaConstants.TO_SINGLE_MODULE_KEY_AAR, taskKeyAAR.getText());
            NotificationUtils.info("配置修改成功", project);
        }
    }


    /**
     * 创建一个文本输入框
     */
    private JTextField getTextField(String defaultStr) {
        JTextField jTextField = new JTextField(defaultStr);
        jTextField.setBorder(IdeBorderFactory.createEmptyBorder(4, 4, 4, 4));
        return jTextField;
    }

    /**
     * 显示配置窗口
     *
     * @param msg
     * @param event
     */
    public static void showConfigureDialog(String msg, AnActionEvent event) {
        //总的内容
        final JPanel content = new JPanel(new GridBagLayout());
        //设置切换为全module任务名
        content.add(new JLabel(msg), ExternalSystemUiUtil.getFillLineConstraints(0));
        DialogWrapper dialog = new DialogWrapper(event.getProject()) {
            {
                setTitle("错误提示");
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
        if (okClicked) {
            MazdaUtils.performActionWithPath(event, MazdaConstants.ACTION_TOOLS_MENU, MazdaConstants.ACTION_MAZDA, MazdaConstants.ACTION_MAZDA_CONFIGURE);
        }
    }

}
