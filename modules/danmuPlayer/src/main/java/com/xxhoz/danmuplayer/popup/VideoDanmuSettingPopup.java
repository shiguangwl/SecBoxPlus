package com.xxhoz.danmuplayer.popup;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import com.xxhoz.danmuplayer.R;
import com.xxhoz.danmuplayer.popup.view.DipAndPx;

import java.util.Timer;
import java.util.TimerTask;


public class VideoDanmuSettingPopup extends PopupWindow {
    private final Context mC;
    protected DismissTimerTask mDismissTimerTask;
    private Timer mDismissTimer;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                dismiss();
            }
        }
    };
    private static final int COMPLETED = 0;
    private final LayoutInflater inflater;
    private final View contentView;
    private ChangeClickListener changeCallback;

    private final SeekBar dmMaxLine;
    private final SeekBar dmSpeed;
    private final SeekBar dmSize;

    private DmSettingConfig dmSettingConfig;
    private DmSetting dmSetting;

    public VideoDanmuSettingPopup(Context context) {
        super(context);
        mC = context;
        inflater = (LayoutInflater) mC.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.popup_video_danmu_setting, null);
        setContentView(contentView);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setWidth(DipAndPx.dip2px(context, 320));
        setOutsideTouchable(true);
        //不设置该属性，弹窗于屏幕边框会有缝隙并且背景不是半透明
        setBackgroundDrawable(new BitmapDrawable());

        dmMaxLine = contentView.findViewById(R.id.dm_maxLine);
        dmSpeed = contentView.findViewById(R.id.dm_speed);
        dmSize = contentView.findViewById(R.id.dm_size);

        dmMaxLine.setOnSeekBarChangeListener(seekBarChangeListener());
        dmSpeed.setOnSeekBarChangeListener(seekBarChangeListener());
        dmSize.setOnSeekBarChangeListener(seekBarChangeListener());

        setConfigValue(new DmSettingConfig(), new DmSetting());
    }


    private SeekBar.OnSeekBarChangeListener seekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startDismissTimer();
                float maxLine = interpolateValue(dmSettingConfig.getMinMaximumLines(), dmSettingConfig.getMaxMaximumLines(), dmMaxLine.getProgress());
                float speed = interpolateValue(dmSettingConfig.getMinScrollSpeed(), dmSettingConfig.getMaxScrollSpeed(), dmSpeed.getProgress());
                float size = interpolateValue(dmSettingConfig.getMinScaleTextSize(), dmSettingConfig.getMaxScaleTextSize(), dmSize.getProgress());

                Toast.makeText(mC, "最大行数：" + maxLine + " 速度：" + (dmSettingConfig.getMaxScrollSpeed() + dmSettingConfig.getMinScrollSpeed() - speed) + " 字体大小：" + size, Toast.LENGTH_SHORT).show();
                if (changeCallback != null) {
                    changeCallback.onConfigChangeListener(new DmSetting((dmSettingConfig.getMaxScrollSpeed() + dmSettingConfig.getMinScrollSpeed() - speed), size, (int) maxLine));
                }
            }
        };
    }


    public void setConfigValue(DmSettingConfig dmSettingConfig, DmSetting dmSetting) {
        this.dmSettingConfig = dmSettingConfig;
        this.dmSetting = dmSetting;

        dmMaxLine.setProgress(calculatePercentage(dmSettingConfig.getMinMaximumLines(), dmSettingConfig.getMaxMaximumLines(), dmSetting.getMaximumLines()));
        dmSpeed.setProgress(calculatePercentage(dmSettingConfig.getMinScrollSpeed(), dmSettingConfig.getMaxScrollSpeed(), dmSetting.getScrollSpeed()));
        dmSize.setProgress(calculatePercentage(dmSettingConfig.getMinScaleTextSize(), dmSettingConfig.getMaxScaleTextSize(), dmSetting.getScaleTextSize()));
    }


    public static Float interpolateValue(float minValue, float maxValue, int progressPercentage) {
        return minValue + (maxValue - minValue) * (progressPercentage / 100f);
    }

    public static Integer calculatePercentage(float minValue, float maxValue, float targetValue) {
        return Math.round((targetValue - minValue) / (maxValue - minValue) * 100);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        startDismissTimer();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        cancelDismissTimer();
    }


    public void setChangeCallback(ChangeClickListener changeCallback) {
        this.changeCallback = changeCallback;
    }

    public void startDismissTimer() {
        cancelDismissTimer();
        mDismissTimer = new Timer();
        mDismissTimerTask = new DismissTimerTask();
        mDismissTimer.schedule(mDismissTimerTask, 3000);
    }

    public void cancelDismissTimer() {
        if (mDismissTimer != null) {
            mDismissTimer.cancel();
        }
        if (mDismissTimerTask != null) {
            mDismissTimerTask.cancel();
        }

    }

    public interface ChangeClickListener {
        /**
         * 弹幕配置变更
         */
        void onConfigChangeListener(DmSetting danmuSetting);
    }

    public class DismissTimerTask extends TimerTask {

        @Override
        public void run() {
            Message message = new Message();
            message.what = COMPLETED;
            handler.sendMessage(message);
        }
    }
}
