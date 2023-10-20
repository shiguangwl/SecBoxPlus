package com.xxhoz.danmuplayer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xxhoz.danmuplayer.view.NetworkSpeedUtil;

import xyz.doikki.videoplayer.controller.GestureVideoController;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 直播/点播控制器
 * 注意：此控制器仅做一个参考，如果想定制ui，你可以直接继承GestureVideoController或者BaseVideoController实现
 * 你自己的控制器
 * Created by Doikki on 2017/4/7.
 */

public class StandardVideoController extends GestureVideoController implements View.OnClickListener {

    private final String TAG = "StandardVideoController";
    protected ImageView mLockButton;

    protected View mLoadingView;

    private TextView loadSpeed;
    private boolean isBuffering;

    private View fullscreenView;

    public StandardVideoController(@NonNull Context context) {
        this(context, null);
    }

    public StandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dkplayer_layout_standard_controller;
    }

    private final boolean isLongPress = false;

    @Override
    protected void initView() {
        super.initView();
        mLockButton = findViewById(R.id.lock);
        mLockButton.setOnClickListener(this);
        mLoadingView = findViewById(R.id.loading);
        loadSpeed = findViewById(R.id.load_speed);

        fullscreenView = findViewById(R.id.fullscreenView);

        // fullscreenView.setOnTouchListener((v, event) -> {
        //     switch (event.getAction()) {
        //         case MotionEvent.ACTION_DOWN:
        //             isLongPress = true;
        //             new Handler(Looper.getMainLooper()).postDelayed(()->{
        //                 if (isLongPress == true){
        //                     mControlWrapper.setSpeed(3f);
        //                 }
        //             },3000);
        //             break;
        //         case MotionEvent.ACTION_UP:
        //             isLongPress = false;
        //             Log.d(TAG, "onTouch: ACTION_UP");
        //             break;
        //     }
        //     return false;
        // });
    }


    /**
     * 快速添加各个组件
     */
    // public void addDefaultControlComponent(String title, boolean isLive) {
    //     CompleteView completeView = new CompleteView(getContext());
    //     ErrorView errorView = new ErrorView(getContext());
    //     PrepareView prepareView = new PrepareView(getContext());
    //     prepareView.setClickStart();
    //     TitleView titleView = new TitleView(getContext());
    //     titleView.setTitle(title);
    //     addControlComponent(completeView, errorView, prepareView, titleView);
    //     if (isLive) {
    //         addControlComponent(new LiveControlView(getContext()));
    //     } else {
    //         addControlComponent(new VodControlView(getContext()));
    //     }
    //     addControlComponent(new GestureView(getContext()));
    //     setCanChangePosition(!isLive);
    // }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.lock) {
            mControlWrapper.toggleLockState();
        }
    }

    @Override
    protected void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            mLockButton.setSelected(true);
            Toast.makeText(getContext(), R.string.dkplayer_locked, Toast.LENGTH_SHORT).show();
        } else {
            mLockButton.setSelected(false);
            Toast.makeText(getContext(), R.string.dkplayer_unlocked, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mControlWrapper.isFullScreen()) {
            if (isVisible) {
                if (mLockButton.getVisibility() == GONE) {
                    mLockButton.setVisibility(VISIBLE);
                    if (anim != null) {
                        mLockButton.startAnimation(anim);
                    }
                }
            } else {
                mLockButton.setVisibility(GONE);
                if (anim != null) {
                    mLockButton.startAnimation(anim);
                }
            }
        }
    }

    @Override
    protected void onPlayerStateChanged(int playerState) {
        super.onPlayerStateChanged(playerState);
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                setLayoutParams(new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                mLockButton.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                if (isShowing()) {
                    mLockButton.setVisibility(VISIBLE);
                } else {
                    mLockButton.setVisibility(GONE);
                }
                break;
        }

        if (mActivity != null && hasCutout()) {
            int orientation = mActivity.getRequestedOrientation();
            int dp24 = PlayerUtils.dp2px(getContext(), 24);
            int cutoutHeight = getCutoutHeight();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                LayoutParams lblp = (LayoutParams) mLockButton.getLayoutParams();
                lblp.setMargins(dp24, 0, dp24, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                LayoutParams layoutParams = (LayoutParams) mLockButton.getLayoutParams();
                layoutParams.setMargins(dp24 + cutoutHeight, 0, dp24 + cutoutHeight, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                LayoutParams layoutParams = (LayoutParams) mLockButton.getLayoutParams();
                layoutParams.setMargins(dp24, 0, dp24, 0);
            }
        }

    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        switch (playState) {
            //调用release方法会回到此状态
            case VideoView.STATE_IDLE:
                mLockButton.setSelected(false);
                mLoadingView.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
                mLoadingView.setVisibility(GONE);
                break;
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERED:
                if (playState == VideoView.STATE_BUFFERED) {
                    isBuffering = false;
                }
                if (!isBuffering) {
                    mLoadingView.setVisibility(GONE);
                }
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                mLoadingView.setVisibility(VISIBLE);
                if (playState == VideoView.STATE_BUFFERING) {
                    isBuffering = true;
                }
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mLoadingView.setVisibility(GONE);
                mLockButton.setVisibility(GONE);
                mLockButton.setSelected(false);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isLocked()) {
            show();
            Toast.makeText(getContext(), R.string.dkplayer_lock_tip, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (mControlWrapper.isFullScreen()) {
            return stopFullScreen();
        }
        return super.onBackPressed();
    }

    @Override
    public void startProgress() {
        super.startProgress();
        startShowBufferSpeed();
    }

    @Override
    public void stopProgress() {
        super.stopProgress();
        stopShowBufferSpeed();
    }

    Handler handler = new Handler(Looper.getMainLooper());
    private NetworkSpeedUtil networkSpeedUtil;

    public void startShowBufferSpeed() {
        if (networkSpeedUtil == null) {
            networkSpeedUtil = new NetworkSpeedUtil(getContext(), speedInBytesPerSecond -> {
                handler.post(() -> {
                    loadSpeed.setText((speedInBytesPerSecond) + "kb/s");
                });
            });
        }
        networkSpeedUtil.stopMonitoring();
        networkSpeedUtil.startMonitoring();
    }

    public void stopShowBufferSpeed() {
        if (networkSpeedUtil != null) {
            networkSpeedUtil.stopMonitoring();
        }
    }


    public void setLoadingMsg(String msg) {
        mLoadingView.setVisibility(VISIBLE);
        stopShowBufferSpeed();
        loadSpeed.setText(msg);
    }
}
