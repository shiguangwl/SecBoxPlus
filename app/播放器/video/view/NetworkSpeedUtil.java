package com.xxhoz.secbox.module.detail.video.view;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public class NetworkSpeedUtil {

    private Context context;
    private long lastUpdateTime;
    private long lastTotalRxBytes;
    private Handler handler;
    private NetworkSpeedListener listener;

    public NetworkSpeedUtil(Context context, NetworkSpeedListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        this.lastUpdateTime = SystemClock.elapsedRealtime();
        this.lastTotalRxBytes = TrafficStats.getTotalRxBytes();
    }

    public void startMonitoring() {
        handler.post(updateSpeedsRunnable);
    }

    public void stopMonitoring() {
        handler.removeCallbacks(updateSpeedsRunnable);
    }

    private final Runnable updateSpeedsRunnable = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.elapsedRealtime();
            long timeDelta = now - lastUpdateTime;

            if (timeDelta > 0) {
                long totalRxBytes = TrafficStats.getTotalRxBytes();
                long rxSpeed = (totalRxBytes - lastTotalRxBytes) / timeDelta;

                lastTotalRxBytes = totalRxBytes;
                lastUpdateTime = now;

                if (listener != null) {
                    listener.onSpeedUpdate(rxSpeed);
                }
            }

            handler.postDelayed(this, 800); // 每秒更新一次
        }
    };

    public interface NetworkSpeedListener {
        void onSpeedUpdate(long speedInBytesPerSecond);
    }
}
