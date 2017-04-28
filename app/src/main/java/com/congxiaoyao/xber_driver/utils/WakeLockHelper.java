package com.congxiaoyao.xber_driver.utils;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.congxiaoyao.xber_driver.TAG;

/**
 * Created by congxiaoyao on 2017/1/31.
 */

public class WakeLockHelper {

    private static PowerManager.WakeLock sCpuWakeLock;

    public static void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }
        Log.v(TAG.ME, "acquireCpuWakeLock");
        sCpuWakeLock = createPartialWakeLock(context);
        sCpuWakeLock.acquire();
    }

    private static PowerManager.WakeLock createPartialWakeLock(Context context) {
        Log.v(TAG.ME, "createPartialWakeLock");
        String flag = TAG.ME;
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, flag);
    }

    public static void releaseCpuLock() {
        if (sCpuWakeLock != null) {
            Log.v(TAG.ME, "releaseCpuLock");
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
