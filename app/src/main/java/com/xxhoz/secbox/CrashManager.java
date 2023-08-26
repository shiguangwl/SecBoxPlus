package com.xxhoz.secbox;

import android.content.Context;
import android.os.Looper;

import com.hjq.toast.Toaster;
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
                LogUtils.INSTANCE.e("uncaughtException-->" + e.toString());
                handleFileException(e);
                if (t == Looper.getMainLooper().getThread()) {
                    handleMainThread(e);
                }
            }
        });
    }

    /**
     * 异常处理
     * @param e
     */
    private void handleFileException(Throwable e) {
        e.printStackTrace();
        Toaster.showLong("未知错误:" + e.toString());
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
