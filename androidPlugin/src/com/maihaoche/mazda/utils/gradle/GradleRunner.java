package com.maihaoche.mazda.utils.gradle;

import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;
import org.gradle.tooling.*;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by yang on 17/3/21.
 */
public class GradleRunner {

    /**
     * 运行一个gradle的task，默认是project的根目录。gradle版本是lib 库中的3.3
     *
     * @param project
     * @param resultHandler 结果回调,保存了taskName
     */
    public static void runGradleTasks(Project project, String taskName, ResultHandler resultHandler) {
        if (project == null || taskName == null || taskName.length() == 0) {
            throw new NullPointerException("runGradleTasks 传入的参数不能为空,project:" + project + ",taskName:" + taskName);
        }
        //使用安装目录下的gradle运行task。
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(project.getBasePath()))
//                .useInstallation(new File("/Users/yang/zMySoftwares/gradle-3.4.1"))
                .connect();
        try {
            if (!hasTaskInProject(connection, taskName)) {
                NotificationUtils.error("runGradleTasks出错，project:" + project.getName() + "中没有找到相应的task:" + taskName);
                return;
            }
            BuildLauncher build = connection.newBuild();
            build.forTasks(taskName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            build.setStandardOutput(outputStream);
            build.setStandardError(System.err);
            build.addProgressListener((ProgressListener) progressEvent -> {
                String outResult = outputStream.toString();
                if (outResult != null && outResult.length() > 0) {
                    NotificationUtils.info(outResult);
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
     * 显示该project 的各个task 以及子module的各个task
     * 该代码为Gradle Tooling API的sample代码，可以用来获得一个project中的所有module.getModules()方法。
     *
     * @param project
     */
    public static void showProjectTasks(Project project) {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(new File(project.getBasePath()));
        ProjectConnection connection = connector.connect();
        IdeaProject projectIdea = connection.getModel(IdeaProject.class);
        try {
            if (projectIdea != null) {
                for (IdeaModule module : projectIdea.getModules()) {
                    //只打印这两个module的task
                    if (module.getName().equals("bentley")
                            || module.getName().equals("module_seek")) {
                        NotificationUtils.info("  " + module);
                        NotificationUtils.info("  module details:");

                        NotificationUtils.info("    tasks from associated gradle projectIdea:");
                        for (GradleTask task : module.getGradleProject().getTasks()) {
                            NotificationUtils.info("      " + task.getName());
                        }
                    }
                }
            } else {
                NotificationUtils.info("connection.getModel(IdeaProject.class)出错，没有找到相应的IdeaProject");
            }
        } finally {
            connection.close();
        }
    }

}
