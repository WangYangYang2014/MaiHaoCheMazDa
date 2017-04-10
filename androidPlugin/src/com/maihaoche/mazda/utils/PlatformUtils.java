package com.maihaoche.mazda.utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.util.ui.UIUtil;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by yang on 17/2/10.
 */
public class PlatformUtils {

    /**
     * 判断平台是否是AndroidStudio
     */
    public static boolean isAndroidStudio() {
//        return "AndroidStudio".equals(getPlatformPrefix());
        return true;
    }

    /**
     * 判断系统是否是windows系统
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * 在异步线程池中执行任务，真正的异步任务。不会阻塞主线程
     */
    public static void executeBackgroundTask(@NotNull Runnable runnable) {
        //如果是UI线程。则必须加入队列才能执行
        if (SwingUtilities.isEventDispatchThread()) {
            ApplicationManager.getApplication().executeOnPooledThread(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 该操作相当于用户点击ctrl+s
     * 注意，该操作不能在非EventDispatchThread（主线程）执行。必须在主线程。
     */
    public static void saveAll() throws IllegalAccessException {
        if (SwingUtilities.isEventDispatchThread()) {
            FileDocumentManager.getInstance().saveAllDocuments();
        } else {
            throw new IllegalAccessException("saveAll 只能在DispatchThread线程，既主线程执行");
        }
    }


    /**
     * 涉及到Project修改的操作。在主线程执行某个耗时任务，需要调用invokeAndWaitIfNeeded
     * 注意，该方法执行的任务会阻塞主线程！
     */
    public static void executeProjectChanges(@NotNull Project project, @NotNull Runnable changes) {
        if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
            if (!project.isDisposed()) {
                changes.run();
            }
            return;
        }
        UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
            //在UI主线程执行任务。执行耗时的任务
            //官方文档：http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
            //总结：文件修改，涉及project module的修改，等操作都需要用该API来run.
            ApplicationManager.getApplication().runWriteAction(() -> {
                if (!project.isDisposed()) {
                    ProjectRootManagerEx.getInstanceEx(project).mergeRootsChangesDuring(changes);
                }
            });
        });
    }

    /**
     * //     * 显示该project 的各个task 以及子module的各个task
     * //     * 该代码为Gradle Tooling API的sample代码，可以用来获得一个project中的所有module.getModules()方法。
     * //     *
     * //     * @param project
     * //
     */
    public static ArrayList<String> getProjectModules(Project project) {
        ArrayList<String> mModuleNames = new ArrayList<>();
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(new File(project.getBasePath()));
        ProjectConnection connection = connector.connect();
        IdeaProject projectIdea = connection.getModel(IdeaProject.class);
        try {
            if (projectIdea != null) {
                for (IdeaModule module : projectIdea.getModules()) {
                    mModuleNames.add(module.getName());
                }
            } else {
                NotificationUtils.info("connection.getModel(IdeaProject.class)出错，没有找到相应的IdeaProject", project);
            }
        } finally {
            connection.close();
        }
        return mModuleNames;
    }

    /**
     * 获取本地存储的key value ，string类型
     */
    public static String getData(String key, String defaultValue) {
        return PropertiesComponent.getInstance().getValue(key, defaultValue);
    }

    /**
     * 数组数据
     */
    public static String[] getDatas(String key) {
        return PropertiesComponent.getInstance().getValues(key);
    }

    /**
     * 存储key value数据，string[]类型
     */
    public static void setDatas(String key, String[] setValue) {
        PropertiesComponent.getInstance().setValues(key, setValue);
    }

    /**
     * 存储key value数据，string类型
     */
    public static void setData(String key, String setValue) {
        PropertiesComponent.getInstance().setValue(key, setValue);
    }


}
