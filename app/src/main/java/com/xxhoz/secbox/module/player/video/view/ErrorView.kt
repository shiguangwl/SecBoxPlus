package com.xxhoz.secbox.module.player.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Animation
import android.widget.LinearLayout
import com.xxhoz.secbox.R
import xyz.doikki.videoplayer.controller.ControlWrapper
import xyz.doikki.videoplayer.controller.IControlComponent
import xyz.doikki.videoplayer.player.VideoView

class ErrorView : LinearLayout, IControlComponent {
    private var mDownX = 0f
    private var mDownY = 0f
    private var mControlWrapper: ControlWrapper? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        visibility = GONE
        LayoutInflater.from(context).inflate(R.layout.dkplayer_layout_error_view, this, true)
        isClickable = true
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View? {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean, anim: Animation) {}
    override fun onPlayStateChanged(playState: Int) {
        if (playState == VideoView.STATE_ERROR) {
            bringToFront()
            visibility = VISIBLE
        } else if (playState == VideoView.STATE_IDLE) {
            visibility = GONE
        }
    }

    override fun onPlayerStateChanged(playerState: Int) {}
    override fun setProgress(duration: Int, position: Int) {}
    override fun onLockStateChanged(isLock: Boolean) {}
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = ev.x
                mDownY = ev.y
                // True if the child does not want the parent to intercept touch events.
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val absDeltaX = Math.abs(ev.x - mDownX)
                val absDeltaY = Math.abs(ev.y - mDownY)
                if (absDeltaX > ViewConfiguration.get(context).scaledTouchSlop ||
                    absDeltaY > ViewConfiguration.get(context).scaledTouchSlop
                ) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }

            MotionEvent.ACTION_UP -> {}
        }
        return super.dispatchTouchEvent(ev)
    }


    /**
     * 从试逻辑
     */
    fun setOnRetryListener(listener: OnClickListener) {
        findViewById<View>(R.id.status_btn).setOnClickListener(){
            listener.onClick(it)
            visibility = GONE
        }
    }
}
