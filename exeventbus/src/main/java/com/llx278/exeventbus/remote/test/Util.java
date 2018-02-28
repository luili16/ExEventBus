package com.llx278.exeventbus.remote.test;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import java.util.List;

/**
 * Created by llx on 2018/2/28.
 */

public class Util {
    public static String getProcessName(Context context) {

        ActivityManager systemService = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
                systemService.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo infp : runningAppProcesses) {
            if (infp.pid == Process.myPid()) {
                return infp.processName + "-" + Process.myPid();
            }
        }
        return null;
    }
}
