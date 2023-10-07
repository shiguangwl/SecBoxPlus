package com.xxhoz.secbox;

import android.content.Context;
import android.os.Looper;

import com.hjq.toast.Toaster;
import com.umeng.commonsdk.internal.crash.UMCrashManager;
import com.xxhoz.constant.BaseConfig;
import com.xxhoz.secbox.util.GlobalActivityManager;
import com.xxhoz.secbox.util.LogUtils;

import java.util.Objects;

public class CrashManager {

    private static CrashManager mInstance;
    private static Context mContext;


    private CrashManager() {

    }

    public static CrashManager getInstance(Context context) {
        if (mInstance == null) {
            mContext = context.getApplicationContext();
            mInstance = new CrashManager();
        }
        return mInstance;
    }

    public void init() {
        //crach 防护
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            handleFileException(e);
            if (t == Looper.getMainLooper().getThread()) {
                handleMainThread(e);
            }

            // // 直接退出
            // GlobalActivityManager.INSTANCE.finishAll();
            // Process.killProcess(Process.myPid());
            // System.exit(0);
        });
    }

    /**
     * 异常处理
     *
     * @param e
     */
    private void handleFileException(Throwable e) {
        if (BaseConfig.INSTANCE.getDEBUG()){
            Toaster.showLong("发生未知错误:" + e.getMessage());
        }
        // 异常上报
        LogUtils.INSTANCE.e("未知错误,错误已上报:", e);
        UMCrashManager.reportCrash(App.instance, e);
    }


    private void handleMainThread(Throwable e) {
        while (true) {
            try {
                Objects.requireNonNull(GlobalActivityManager.INSTANCE.getTopActivity()).finish();
                Looper.loop();
            } catch (Throwable e1) {
                handleFileException(e1);
            }
        }

    }
}
