package com.maihaoche.mazda.utils.gradle;

import com.intellij.openapi.project.Project;
import com.maihaoche.mazda.utils.NotificationUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by yang on 17/3/21.
 */
public class GradleKiller {

    private GradleKiller() {
    }

    public static boolean killGradleProcess(Project project) {
        String[] pids;
        try {
            pids = getPids();
        } catch (UnsupportedOperationException e) {
            NotificationUtils.error("无法获取进程数据");
            return false;
        }

        if (pids.length == 0) {
            NotificationUtils.info("没有正在执行的gradle进程");
        } else {
            boolean result = true;
            for (String pid : pids) {
                result = result & killProcess(pid);
            }
            if (result) {
                NotificationUtils.info("gradle进程已杀死");
                return true;
            } else {
                NotificationUtils.error("无法杀死gradle进程");
            }
        }
        return false;
    }

    /**
     * 返回是否有gradle进程正在执行
     *
     * @return
     * @throws Exception
     */
    public static boolean isGradleRunning() {
        String[] pids;
        try {
            pids = getPids();
        } catch (UnsupportedOperationException e) {
            NotificationUtils.info("gradle进程已杀死");
            return false;
        }
        return !(pids.length == 0);
    }


    private static String[] getPids() throws UnsupportedOperationException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return getPidsOnWindows();
        } else {
            return getPidsOnUnix();
        }
    }

    /**
     * @param pid The PID of the process to kill
     * @return true if killed, false if not
     */
    private static boolean killProcess(String pid) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return killProcessOnWindows(pid);
        } else {
            return killProcessOnUnix(pid);
        }
    }

    private static String[] getPidsOnUnix() throws UnsupportedOperationException {
        ArrayList<String> pids = new ArrayList<String>();
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("pgrep -f gradle-launcher");
            p.waitFor();

            if (p.exitValue() != 0 && p.exitValue() != 1) { //OK found, OK not found
                throw new UnsupportedOperationException("pgrep returned info value!");
            } else {
                BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                while ((line = b.readLine()) != null) {
                    pids.add(line);
                }

                b.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("pgrep parsing failed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("pgrep parsing failed!");
        }
        return pids.toArray(new String[pids.size()]);
    }

    private static String[] getPidsOnWindows() throws UnsupportedOperationException {
        ArrayList<String> pids = new ArrayList<String>();
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("wmic process where \"commandline like '%gradle-launcher%' and name like '%java%'\" get processid");
            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = b.readLine()) != null) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(line.trim());
                    pids.add(line.trim());
                } catch (NumberFormatException e) {
                    //Don't add it, it's a string!
                }
            }

            b.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("wmic parsing failed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("wmic parsing failed!");
        }
        return pids.toArray(new String[pids.size()]);
    }

    private static boolean killProcessOnWindows(String pid) {
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("taskkill /F /PID " + pid);
            p.waitFor();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean killProcessOnUnix(String pid) {
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            p = r.exec("kill -9 " + pid);
            p.waitFor();

            return p.exitValue() == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
