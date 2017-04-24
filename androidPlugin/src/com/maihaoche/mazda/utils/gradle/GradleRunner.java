package com.maihaoche.mazda.utils.gradle;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.constant.MazdaConstants;
import com.maihaoche.mazda.utils.MazdaUtils;
import com.maihaoche.mazda.utils.NotificationUtils;
import org.gradle.tooling.*;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang on 17/3/21.
 */
public class GradleRunner {

    /**
     * 运行一个gradle的task，默认是project的根目录。gradle版本是lib 库中的3.3
     *
     * @param project
     * @param taskName
     * @param arguments
     * @param resultHandler 结果回调,保存了taskName
     */
    public static void runGradleTasks(Project project, String taskName, ArrayList<String> arguments, ResultHandler resultHandler) {
        if (project == null || taskName == null || taskName.length() == 0) {
            throw new NullPointerException("runGradleTasks 传入的参数不能为空,project:" + project + ",taskName:" + taskName);
        }
        //使用安装目录下的gradle运行task。
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(project.getBasePath()))
                .connect();
        try {
            if (!hasTaskInProject(connection, taskName)) {
                NotificationUtils.error("runGradleTasks出错，project:" + project.getName() + "中没有找到相应的task:" + taskName);
                return;
            }
            BuildLauncher build = connection.newBuild();
            build.forTasks(taskName);

            List<String> buildArgs = new ArrayList<String>();
            buildArgs.add("--parallel");
            buildArgs.add("--max-workers=8");
            buildArgs.add("--configure-on-demand");
            buildArgs.add("--offline");
            if (arguments != null && arguments.size() > 0) {
                for (int i = 0; i < arguments.size(); i++) {
                    buildArgs.add(arguments.get(i));
                }
            }
            build.withArguments(buildArgs.toArray(new String[]{}));
            String argumentStr = "";
            for (int i = 0; i < buildArgs.size(); i++) {
                argumentStr += " " + buildArgs.get(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            build.setStandardOutput(outputStream);
            build.setStandardError(System.err);
            build.addProgressListener((ProgressListener) progressEvent -> {
                String outResult = outputStream.toString();
                if (outResult != null && outResult.length() > 0) {
                    NotificationUtils.info(outResult, project);
                    outputStream.reset();
                }
            });
            build.run(resultHandler);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


    /**
     * 判断project中是否包含某个task
     *
     * @param taskName
     * @return
     */
    public static boolean hasTaskInProject(ProjectConnection connection, String taskName) {
        if (connection == null || taskName == null) {
            return false;
        }
        GradleProject projectIdea = connection.getModel(GradleProject.class);
        if (projectIdea != null) {
            for (GradleTask task : projectIdea.getTasks()) {
                if (task.getName().equals(taskName)) {
                    return true;
                }
            }
        }
        return false;
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

    /**
     * 执行gradle任务的结果回调类
     * 完成后，执行sync project 任务。
     */
    public static class SyncProjectResultHandler implements ResultHandler {
        private AnActionEvent event;
        private String mTaskName;

        public SyncProjectResultHandler(AnActionEvent event, String mTaskName) {
            this.event = event;
            this.mTaskName = mTaskName;
        }

        @Override
        public void onComplete(Object o) {
            NotificationUtils.info("Gradle任务\"" + mTaskName + "\"执行完毕，开始sync整工程", AnAction.getEventProject(event));
            MazdaUtils.performActionWithPath(event, MazdaConstants.ACTION_TOOLS_MENU, MazdaConstants.ACTION_ANDROID, MazdaConstants.ACTION_ANDROID_SYNC);
        }

        @Override
        public void onFailure(GradleConnectionException e) {
            NotificationUtils.error("执行Grale任务\"" + mTaskName + "\"时：e:" + e.getMessage());
        }

    }
}
