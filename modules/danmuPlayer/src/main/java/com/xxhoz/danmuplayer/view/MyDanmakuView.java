package com.xxhoz.danmuplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xxhoz.danmuplayer.BuildConfig;
import com.xxhoz.danmuplayer.view.danma.BiliDanmukuParser;
import com.xxhoz.danmuplayer.view.danma.CenteredImageSpan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * @author NanYu
 */
public class MyDanmakuView extends master.flame.danmaku.ui.widget.DanmakuView implements IControlComponent {
    // 弹幕是否显示
    private Boolean danmuState = true;

    public DanmakuContext getmContext() {
        return mContext;
    }

    private final DanmakuContext mContext;
    private ControlWrapper controlWrapper;
    private int playState;

    public MyDanmakuView(@NonNull Context context) {
        super(context);
    }

    public MyDanmakuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDanmakuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    {
        // 设置弹幕的最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        // TYPE_SCROLL_RL 从右至左滚动弹幕
        // TYPE_SCROLL_LR 从左至右滚动弹幕
        // TYPE_FIX_TOP 顶端固定弹幕
        // TYPE_FIX_BOTTOM 底端固定弹幕

        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 7); // 滚动弹幕最大显示行数

        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);

        mContext = DanmakuContext.create();
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false) //是否启用合并重复弹幕
                .setScrollSpeedFactor(1.8f)//设置弹幕滚动速度系数,只对滚动弹幕有效
                .setScaleTextSize(0.9f)  // 设置缩放字体大小
                .setMaximumLines(maxLinesPair) //设置最大显示行数
                .preventOverlapping(overlappingEnablePair); //设置防弹幕重叠，null为允许重叠

        setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                handler.post(() -> {
                    if (playState == VideoView.STATE_BUFFERED || playState == VideoView.STATE_PLAYING) {
                        seekTo(controlWrapper.getCurrentPosition());
                        show();
                    }
                });

            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                float speed = controlWrapper.getSpeed();
                if (speed != 1) {
                    // timer.add((long) (timer.lastInterval() * (speed - 1)));
                    handler.post(() -> {
                        timer.update(controlWrapper.getCurrentPosition());
                    });
                }
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        // 判断是否为debug模式，如果是则显示fps
        if (BuildConfig.DEBUG) {
            showFPS(true);
        }
        enableDanmakuDrawingCache(true);
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        this.controlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    @Override
    public void onPlayStateChanged(int playState) {
        // STATE_ERROR = -1; // 错误状态，表示播放出现问题
        // STATE_IDLE = 0; // 空闲状态，播放器未初始化或已释放
        // STATE_PREPARING = 1; // 准备中状态，播放器正在准备资源
        // STATE_PREPARED = 2; // 已准备状态，播放器已经准备好开始播放
        // STATE_PLAYING = 3; // 播放中状态，表示正在播放
        // STATE_PAUSED = 4; // 暂停状态，表示播放暂停
        // STATE_PLAYBACK_COMPLETED = 5; // 播放完成状态，表示播放已完成
        // STATE_BUFFERING = 6; // 缓冲中状态，表示正在缓冲数据
        // STATE_BUFFERED = 7; // 缓冲完成状态，表示缓冲已完成
        this.playState = playState;
        switch (playState) {
            case VideoView.STATE_IDLE:
                release();
                break;
            case VideoView.STATE_BUFFERING:
                hide();
                break;
            case VideoView.STATE_BUFFERED:
                if (isPrepared()) {
                    showDanmu();
                    seekTo(controlWrapper.getCurrentPosition());
                }
                break;
            case VideoView.STATE_PREPARING:
                break;
            case VideoView.STATE_PLAYING:
                showDanmu();
                if (isPrepared()) {
                    if (isPaused()) {
                        resume();
                    } else {
                        seekTo(controlWrapper.getCurrentPosition());
                    }
                }
                break;
            case VideoView.STATE_PAUSED:
                if (isPrepared()) {
                    pause();
                }
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                clear();
                clearDanmakusOnScreen();
                break;
        }
    }

    public void showDanmu() {
        if (danmuState) {
            show();
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {

    }

    @Override
    public void setProgress(int duration, int position) {

    }

    @Override
    public void onLockStateChanged(boolean isLocked) {

    }

    /**
     * 发送文字弹幕
     *
     * @param text   弹幕文字
     * @param isSelf 是不是自己发的
     */
    public void addDanmaku(String text, boolean isSelf) {
        mContext.setCacheStuffer(new SpannedCacheStuffer(), null);
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null) {
            return;
        }

        danmaku.text = text;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = false;
        danmaku.setTime(getCurrentTime() + 1200);
        danmaku.textSize = PlayerUtils.sp2px(getContext(), 12);
        danmaku.textColor = Color.WHITE;
        danmaku.textShadowColor = Color.GRAY;
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = isSelf ? Color.GREEN : Color.TRANSPARENT;
        addDanmaku(danmaku);
    }

//     /**
//      * 发送自定义弹幕
//      */
//     public void addDanmakuWithDrawable() {
//         mContext.setCacheStuffer(new BackgroundCacheStuffer(), null);
//         BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
//         if (danmaku == null) {
//             return;
//         }
//         // for(int i=0;i<100;i++){
//         // }
//         Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher_round);
//         int size = PlayerUtils.dp2px(getContext(), 20);
//         drawable.setBounds(0, 0, size, size);
//
// //        danmaku.text = "这是一条弹幕";
//         danmaku.text = createSpannable(drawable);
// //        danmaku.padding = 5;
//         danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
//         danmaku.isLive = false;
//         danmaku.setTime(getCurrentTime() + 1200);
//         danmaku.textSize = PlayerUtils.sp2px(getContext(), 12);
//         danmaku.textColor = Color.RED;
//         danmaku.textShadowColor = Color.WHITE;
//         // danmaku.underlineColor = Color.GREEN;
// //        danmaku.borderColor = Color.GREEN;
//         addDanmaku(danmaku);
//
//     }

    public void loadDanmuStream(File stream) throws IOException {
        if (isPrepared()) {
            release();
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
        try {
            loader.load(new FileInputStream(stream));
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        IDataSource<?> dataSource = loader.getDataSource();
        BaseDanmakuParser mParser = new BiliDanmukuParser();
        mParser.load(dataSource);
        prepare(mParser, mContext);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        CenteredImageSpan span = new CenteredImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" 这是一条自定义弹幕~");
        return spannableStringBuilder;
    }

    /**
     * 绘制背景(自定义弹幕样式)
     */
    private class BackgroundCacheStuffer extends SpannedCacheStuffer {


        // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
        final Paint paint = new Paint();

        @Override
        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
//            danmaku.padding = 5;  // 在背景绘制模式下增加padding
            super.measure(danmaku, paint, fromWorkerThread);
        }

        @Override
        public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor("#65777777"));//黑色 普通
            int radius = PlayerUtils.dp2px(getContext(), 10);
            canvas.drawRoundRect(new RectF(left, top, left + danmaku.paintWidth, top + danmaku.paintHeight), radius, radius, paint);
        }

        @Override
        public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
            // 禁用描边绘制
        }
    }

    public Boolean getDanmuState() {
        return danmuState;
    }

    public void setDanmuState(Boolean danmuState) {
        this.danmuState = danmuState;
    }


}
