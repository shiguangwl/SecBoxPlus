package com.xxhoz.secbox;

import android.content.Context;
import android.os.Looper;

import com.hjq.toast.Toaster;
import com.umeng.commonsdk.internal.crash.UMCrashManager;
import com.xxhoz.constant.BaseConfig;
import com.xxhoz.secbox.util.GlobalActivityManager;
import com.xxhoz.secbox.util.LogUtils;

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
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                // LogUtils.INSTANCE.e("uncaughtException-->" + e.toString());
                handleFileException(e);
                if (t == Looper.getMainLooper().getThread()) {
                    LogUtils.INSTANCE.e("未知错误,进程终止,错误已上报");
                    // 主线程异常直接退出
                    GlobalActivityManager.INSTANCE.finishAll();
                    // handleMainThread(e);
                }
            }
        });
    }

    /**
     * 异常处理
     * @param e
     */
    private void handleFileException(Throwable e) {
        if (BaseConfig.INSTANCE.getDEBUG()){
            Toaster.showLong("未知错误:" + e.toString());
        }
        // 异常上报
        UMCrashManager.reportCrash(App.instance,e);
        e.printStackTrace();
    }

    private void handleMainThread(Throwable e) {
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e1) {
                handleFileException(e1);
            }
        }
    }
}
