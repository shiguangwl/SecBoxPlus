package com.xxhoz.secbox.module.detail.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import com.xxhoz.danmuplayer.R
import xyz.doikki.videoplayer.controller.ControlWrapper
import xyz.doikki.videoplayer.controller.IControlComponent
import xyz.doikki.videoplayer.player.VideoView
import xyz.doikki.videoplayer.player.VideoViewManager

/**
 * 准备播放界面
 */
class PrepareView : FrameLayout, IControlComponent {
    private lateinit var mControlWrapper: ControlWrapper
    private var mThumb: ImageView

    //    private var bufferingState: TextView
    private var mStartPlay: ImageView
    private var mLoading: View
    private var mNetWarning: FrameLayout

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.dkplayer_layout_prepare_view, this, true)
        mThumb = findViewById(R.id.thumb)
        mStartPlay = findViewById(R.id.start_play)
        mLoading = findViewById(R.id.loading)
//        bufferingState = findViewById(R.id.buffering_state)
        mNetWarning = findViewById(R.id.net_warning_layout)
        findViewById<View>(R.id.status_btn).setOnClickListener {
            mNetWarning.visibility = GONE
            VideoViewManager.instance().setPlayOnMobileNetwork(true)
            mControlWrapper.start()
        }
    }

    /**
     * 设置点击此界面开始播放
     */
    fun setClickStart() {
        setOnClickListener { mControlWrapper.start() }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean, anim: Animation) {}
    override fun onPlayStateChanged(playState: Int) {
        when (playState) {
            VideoView.STATE_PREPARING -> {
                bringToFront()
                visibility = VISIBLE
                mStartPlay.visibility = GONE
                mNetWarning.visibility = GONE
                mLoading.visibility = VISIBLE
//                bufferingState.visibility = VISIBLE
            }

            VideoView.STATE_PLAYING, VideoView.STATE_PAUSED, VideoView.STATE_ERROR, VideoView.STATE_BUFFERING, VideoView.STATE_BUFFERED, VideoView.STATE_PLAYBACK_COMPLETED -> visibility =
                GONE

            VideoView.STATE_IDLE -> {
                visibility = VISIBLE
                bringToFront()
                mLoading.visibility = GONE
//                bufferingState.visibility = GONE
                mNetWarning.visibility = GONE
                mStartPlay.visibility = VISIBLE
                mThumb.visibility = VISIBLE
            }

            VideoView.STATE_START_ABORT -> {
                visibility = VISIBLE
                mNetWarning.visibility = VISIBLE
                mNetWarning.bringToFront()
            }
        }
    }

    override fun onPlayerStateChanged(playerState: Int) {}
    override fun setProgress(duration: Int, position: Int) {}
    override fun onLockStateChanged(isLocked: Boolean) {}
}
