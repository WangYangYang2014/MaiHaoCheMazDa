package com.maihaoche.mazda.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.util.PlatformUtils.getPlatformPrefix;

/**
 * Created by yang on 17/2/10.
 */
public class PlatformUtils {

    public static boolean isAndroidStudio() {
        return "AndroidStudio".equals(getPlatformPrefix());
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * 判断，如果某个进程正在执行，则销毁掉。
     */
    public static boolean destroyProcessIfRunning(Process p) {
        if (p != null && p.isAlive()) {
            p.destroyForcibly();
            try {
                p.waitFor();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
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
     * 涉及到Project修改的操作。
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


}
