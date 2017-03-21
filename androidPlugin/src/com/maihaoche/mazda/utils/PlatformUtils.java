package com.maihaoche.mazda.utils;

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


}
