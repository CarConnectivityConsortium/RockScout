package com.carconnectivity.mlmediaplayer.utils;

import android.content.pm.ApplicationInfo;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class LogUtils {

    public static void setupLogcatLogs(ApplicationInfo appInfo) {
        if (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            File f = new File(Environment.getExternalStorageDirectory(), "rockscout_logs");
            if (!f.exists()) {
                f.mkdirs();
            }

            String filePath = Environment.getExternalStorageDirectory() + String.format("/rockscout_logs/logcat_%s.txt", Calendar.getInstance().getTime().toString());
            try {
                Runtime.getRuntime().exec(new String[]{"logcat", "-v", "time", "-f", filePath});
            } catch (IOException e) {
            }
        }
    }
}
