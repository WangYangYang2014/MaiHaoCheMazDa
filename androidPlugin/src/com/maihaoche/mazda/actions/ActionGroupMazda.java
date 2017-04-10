package com.maihaoche.mazda.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.PlatformUtils;

import java.util.HashSet;

/**
 * Created by yang on 17/4/10.
 */
public class ActionGroupMazda extends DefaultActionGroup {

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
        String[] savedSettings = PlatformUtils.getDatas(MazdaConstants.SAVED_SINGLE_MODULE_SETTING);
        if (savedSettings == null || savedSettings.length == 0) {
            return;
        }
        AnAction[] childActions = getChildActionsOrStubs();
        HashSet<String> currentActionNames = new HashSet<>();
        if (childActions != null && childActions.length > 0) {
            for (int i = 0; i < childActions.length; i++) {
                currentActionNames.add(childActions[i].getTemplatePresentation().getText());
            }
        }
        for (int i = 0; i < savedSettings.length; i++) {
            String savedActionId = savedSettings[i];
            if (currentActionNames.contains(ActionToSomeSingle.getModuleNameFromId(savedActionId))) {
                continue;
            } else {
                ActionToSomeSingle actionToSomeSingle = new ActionToSomeSingle(ActionToSomeSingle.getModuleNameFromId(savedActionId)
                        , ActionToSomeSingle.getAARsFromId(savedActionId));
                //先注册
                if (ActionToSomeSingle.registerAction(actionToSomeSingle, project)) {
                    //再添加到group中
                    addToThisGroup(actionToSomeSingle);
                }
            }
        }
    }

    /**
     * 外部调用update
     *
     * @param event
     */
    public static void updateOutside(AnActionEvent event) {
        DefaultActionGroup mazdaGroup = getMazdaAction();
        if (mazdaGroup != null) {
            mazdaGroup.update(event);
        }
    }

    /**
     * 从toolsmenu中寻找到Mazda
     */
    public static void addToThisGroup(ActionToSomeSingle toSomeSingleAction) {
        if (toSomeSingleAction == null) {
            return;
        }
        DefaultActionGroup mazdaGroup = getMazdaAction();
        if (mazdaGroup == null) {
            return;
        }
        AnAction[] mazdaActions = mazdaGroup.getChildActionsOrStubs();
        //去掉原来的
        if (mazdaActions != null && mazdaActions.length > 0) {
            for (int j = 0; j < mazdaActions.length; j++) {
                if (mazdaActions[j] != null && mazdaActions[j].getTemplatePresentation() != null
                        && mazdaActions[j].getTemplatePresentation().getText() != null
                        && mazdaActions[j].getTemplatePresentation().getText().equals(toSomeSingleAction.getMainModule())) {
                    mazdaGroup.remove(mazdaActions[j]);
                }
            }
        }
        mazdaGroup.add(toSomeSingleAction, new Constraints(Anchor.AFTER, "ToSingleModuleActionId"));
    }

    private static DefaultActionGroup getMazdaAction() {

        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction(MazdaConstants.ACTION_TOOLS_MENU);
        if (actionGroup != null) {
            AnAction[] anActions = actionGroup.getChildActionsOrStubs();
            if (anActions != null && anActions.length > 0) {
                for (int i = 0; i < anActions.length; i++) {
                    //找到了mazda
                    AnAction anAction = anActions[i];
                    if (anAction instanceof DefaultActionGroup
                            && anAction.getTemplatePresentation() != null
                            && anAction.getTemplatePresentation().getText() != null
                            && MazdaConstants.ACTION_MAZDA.equals(anAction.getTemplatePresentation().getText())) {
                        return (DefaultActionGroup) anAction;
                    }
                }
            }
        }
        return null;
    }

}
