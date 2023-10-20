package xyz.doikki.videocontroller.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.xxhoz.secbox.R
import xyz.doikki.videoplayer.controller.ControlWrapper
import xyz.doikki.videoplayer.controller.IControlComponent
import xyz.doikki.videoplayer.player.VideoView
import xyz.doikki.videoplayer.util.PlayerUtils

/**
 * 播放器顶部标题栏
 */
class TopTitleView : FrameLayout, IControlComponent {
    private var mControlWrapper: ControlWrapper? = null
    private var mTitleContainer: LinearLayout? = null
    private var mTitle: TextView? = null
    private var mSysTime: TextView? = null //系统当前时间
    private var mBatteryReceiver: BatteryReceiver? = null
    private var mIsRegister = false //是否注册BatteryReceiver
    lateinit private var throwingScreen: ImageView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        visibility = GONE
        LayoutInflater.from(context).inflate(R.layout.dkplayer_layout_title_view, this, true)
        mTitleContainer = findViewById(R.id.title_container)
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            val activity = PlayerUtils.scanForActivity(context)
            if (activity != null && mControlWrapper!!.isFullScreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mControlWrapper!!.stopFullScreen()
            }
        }
        mTitle = findViewById(R.id.title)
        mSysTime = findViewById(R.id.sys_time)
        throwingScreen = findViewById(R.id.throwing_screen)
        //电量
        val batteryLevel = findViewById<ImageView>(R.id.iv_battery)
        mBatteryReceiver = BatteryReceiver(batteryLevel)
    }

    fun setTitle(title: String?) {
        mTitle!!.text = title
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mIsRegister) {
            context.unregisterReceiver(mBatteryReceiver)
            mIsRegister = false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!mIsRegister) {
            context.registerReceiver(mBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            mIsRegister = true
        }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View? {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean, anim: Animation) {
        //只在全屏时才有效
//        if (!mControlWrapper!!.isFullScreen) return
        if (isVisible) {
            if (visibility == GONE) {
                mSysTime!!.text = PlayerUtils.getCurrentSystemTime()
                visibility = VISIBLE
                if (anim != null) {
                    startAnimation(anim)
                }
            }
        } else {
            if (visibility == VISIBLE) {
                visibility = GONE
                if (anim != null) {
                    startAnimation(anim)
                }
            }
        }
    }

    override fun onPlayStateChanged(playState: Int) {
        when (playState) {
            VideoView.STATE_IDLE, VideoView.STATE_START_ABORT, VideoView.STATE_PREPARING, VideoView.STATE_PREPARED, VideoView.STATE_ERROR, VideoView.STATE_PLAYBACK_COMPLETED -> visibility =
                GONE
        }
    }

    override fun onPlayerStateChanged(playerState: Int) {
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            if (mControlWrapper!!.isShowing && !mControlWrapper!!.isLocked) {
                visibility = VISIBLE
                mSysTime!!.text = PlayerUtils.getCurrentSystemTime()
            }
            mTitle!!.isSelected = true
        } else {
            visibility = GONE
            mTitle!!.isSelected = false
        }
        val activity = PlayerUtils.scanForActivity(context)
        if (activity != null && mControlWrapper!!.hasCutout()) {
            val orientation = activity.requestedOrientation
            val cutoutHeight = mControlWrapper!!.cutoutHeight
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mTitleContainer!!.setPadding(0, 0, 0, 0)
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mTitleContainer!!.setPadding(cutoutHeight, 0, 0, 0)
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mTitleContainer!!.setPadding(0, 0, cutoutHeight, 0)
            }
        }
    }

    override fun setProgress(duration: Int, position: Int) {}
    override fun onLockStateChanged(isLocked: Boolean) {
        if (isLocked) {
            visibility = GONE
        } else {
            visibility = VISIBLE
            mSysTime!!.text = PlayerUtils.getCurrentSystemTime()
        }
    }

    private class BatteryReceiver(private val pow: ImageView) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras ?: return
            val current = extras.getInt("level") // 获得当前电量
            val total = extras.getInt("scale") // 获得总电量
            val percent = current * 100 / total
            pow.drawable.level = percent
        }
    }

    fun setThrowingScreenListener(listener: OnClickListener) {
        throwingScreen.setOnClickListener(listener)
    }
}
