package com.xxhoz.danmuplayer.popup;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xxhoz.danmuplayer.R;
import com.xxhoz.danmuplayer.popup.view.DipAndPx;

import java.util.Timer;
import java.util.TimerTask;


public class VideoSpeedPopup extends PopupWindow implements View.OnClickListener {
    private static final int COMPLETED = 0;
    protected DismissTimerTask mDismissTimerTask;
    private final TextView speedOne;
    private final TextView speedTwo;
    private final TextView speedThree;
    private final TextView speedFour;
    private final TextView speedFive;
    private SpeedChangeListener speedChangeListener;
    private final Context mC;
    private final LayoutInflater inflater;
    private final View contentView;
    private Timer mDismissTimer;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                dismiss();

            }
        }
    };

    public VideoSpeedPopup(Context context) {
        super(context);
        mC = context;
        inflater = (LayoutInflater) mC.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.popup_video_speed, null);
        setContentView(contentView);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setWidth(DipAndPx.dip2px(context, 200));
        speedOne = contentView.findViewById(R.id.pop_speed_1);
        speedTwo = contentView.findViewById(R.id.pop_speed_1_25);
        speedThree = contentView.findViewById(R.id.pop_speed_1_5);
        speedFour = contentView.findViewById(R.id.pop_speed_1_75);
        speedFive = contentView.findViewById(R.id.pop_speed_2);
        setOutsideTouchable(true);
        //不设置该属性，弹窗于屏幕边框会有缝隙并且背景不是半透明
        setBackgroundDrawable(new BitmapDrawable());
        speedOne.setOnClickListener(this);
        speedTwo.setOnClickListener(this);
        speedThree.setOnClickListener(this);
        speedFour.setOnClickListener(this);
        speedFive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (speedChangeListener != null) {
            int id = v.getId();
            if (id == R.id.pop_speed_1) {
                speedOne.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedThree.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFour.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFive.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedChangeListener.speedChange(1f);
            } else if (id == R.id.pop_speed_1_25) {
                speedOne.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
                speedThree.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFour.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFive.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedChangeListener.speedChange(1.25f);
            } else if (id == R.id.pop_speed_1_5) {
                speedOne.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedThree.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
                speedFour.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFive.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedChangeListener.speedChange(1.5f);
            } else if (id == R.id.pop_speed_1_75) {
                speedOne.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedThree.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFour.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
                speedFive.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedChangeListener.speedChange(1.75f);
            } else if (id == R.id.pop_speed_2) {
                speedOne.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedTwo.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedThree.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFour.setTextColor(mC.getResources().getColor(R.color.colorWhite));
                speedFive.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
                speedChangeListener.speedChange(2f);
            }
        }
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

    public void startDismissTimer() {
        cancelDismissTimer();
        mDismissTimer = new Timer();
        mDismissTimerTask = new DismissTimerTask();
        mDismissTimer.schedule(mDismissTimerTask, 2500);
    }

    public void cancelDismissTimer() {
        if (mDismissTimer != null) {
            mDismissTimer.cancel();
        }
        if (mDismissTimerTask != null) {
            mDismissTimerTask.cancel();
        }

    }

    public SpeedChangeListener getSpeedChangeListener() {
        return speedChangeListener;
    }

    public void setSpeedChangeListener(SpeedChangeListener speedChangeListener) {
        this.speedChangeListener = speedChangeListener;
    }

    public interface SpeedChangeListener {
        void speedChange(float speed);
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
