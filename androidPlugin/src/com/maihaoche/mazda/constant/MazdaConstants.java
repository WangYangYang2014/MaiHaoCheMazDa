package com.maihaoche.mazda.constant;

/**
 * Created by yang on 17/4/10.
 */
public class MazdaConstants {

    public static final String MAZDA_PREFIX = "com.maihaoche.mazda";


    //存储切换到单个module的任务的名字
    public static final String TO_SINGLE_MODULE_TASK_NAME = MAZDA_PREFIX + "to_single_module_task_name";
    //存储切换到单个module的任务的property的key，该key指定了那个module作为主要的module
    public static final String TO_SINGLE_MODULE_KEY_MAIN = MAZDA_PREFIX + "to_single_module_key_main";
    //存储切换到单个module的任务的property的key，该key指定了主module依赖哪些子module的aar文件
    public static final String TO_SINGLE_MODULE_KEY_AAR = MAZDA_PREFIX + "to_single_module_key_aar";

    //存储的之前切换的数据
    public static final String SAVED_SINGLE_MODULE_SETTING = MAZDA_PREFIX + "saved_single_module_setting";

    //存储切换到所有的module的任务的名字
    public static final String TO_ALL_MODULE_TASK_NAME = MAZDA_PREFIX + "to_all_module_task_name";


    //-------------------------action的一些路径-------------------

    public static final String ACTION_TOOLS_MENU = "ToolsMenu";
    public static final String ACTION_ANDROID = "Android";
    public static final String ACTION_ANDROID_SYNC = "Sync Project with Gradle Files";
    public static final String ACTION_MAZDA = "Mazda";
    public static final String ACTION_MAZDA_CONFIGURE = "Configure";
}
