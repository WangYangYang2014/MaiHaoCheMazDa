package com.maihaoche.mazda.utils.gradle;

import com.intellij.openapi.project.Project;
import org.gradle.tooling.*;

import java.io.File;

/**
 * Created by yang on 17/3/21.
 */
public class GradleRunner {

    /**
     * 运行一个gradle的task，默认是project的根目录。gradle版本是lib 库中的3.3
     *
     * @param project
     * @param command
     * @param resultHandler 结果回调
     */
    public static void runGradleTasks(Project project, String command, ResultHandler resultHandler) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(project.getBasePath()));
        ProjectConnection connection = connector.connect();
        BuildLauncher build = connection.newBuild();
        build.forTasks(command);
        build.setStandardOutput(System.out);
        build.run(resultHandler);
    }

}
