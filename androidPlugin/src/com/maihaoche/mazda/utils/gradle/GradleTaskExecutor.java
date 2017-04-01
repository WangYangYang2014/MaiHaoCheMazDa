package com.maihaoche.mazda.utils.gradle;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.gradle.tooling.ResultHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Task.Backgroundabel的任务，执行的时候，可以获取ProgressIndicator，该indicator是显示在ItelliJ 底部的进度条。
 * Created by yang on 17/4/1.
 */
public class GradleTaskExecutor extends Task.Backgroundable {
    private Project mProject;
    private String mTask = "";
    private org.gradle.tooling.ResultHandler mResultHandler = null;


    public GradleTaskExecutor(@Nullable Project project, String mTask, ResultHandler mResultHandler) {
        super(project, "Mazda 正在切换module结构", false);
        this.mProject = project;
        this.mTask = mTask;
        this.mResultHandler = mResultHandler;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        GradleRunner.runGradleTasks(mProject, mTask, mResultHandler);
        if (progressIndicator != null && !progressIndicator.isCanceled()) {
            progressIndicator.stop();
        }
    }


}
