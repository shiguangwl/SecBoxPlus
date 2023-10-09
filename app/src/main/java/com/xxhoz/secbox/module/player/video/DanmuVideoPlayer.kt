package com.xxhoz.secbox.module.player.video

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.Gravity
import com.hjq.toast.Toaster
import com.xxhoz.constant.Key
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.module.player.popup.VideoEpisodePopup
import com.xxhoz.secbox.module.player.popup.VideoSpeedPopup
import com.xxhoz.secbox.module.player.video.view.BottomControlView
import com.xxhoz.secbox.module.player.video.view.ErrorView
import com.xxhoz.secbox.module.player.video.view.SecDanmakuView
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils

import xyz.doikki.videocontroller.component.CompleteView
import xyz.doikki.videocontroller.component.GestureView
import xyz.doikki.videocontroller.component.TopTitleView
import xyz.doikki.videoplayer.player.VideoView
import java.io.File

/**
 * 包含弹幕的播放器
 */

class DanmuVideoPlayer : VideoView {

    lateinit var topTitleView: TopTitleView

    private lateinit var vDanmakuView: SecDanmakuView

    lateinit var standardVideoController: StandardVideoController

    var episodes: ArrayList<EpsodeEntity>? = null

    private lateinit var bottomControlView: BottomControlView

    val videoEpisodePopup: VideoEpisodePopup by lazy {
        val popup = VideoEpisodePopup(context, episodes)
        popup.episondeClickListener = object : VideoEpisodePopup.EpisodeClickListener {
            override fun onEpisodeClickListener(entity: EpsodeEntity, position: Int) {
                actionCallback.selectPartsClick(position)
            }
        }
        popup
    }

    val videoSpeedPopup by lazy {
        val popup = VideoSpeedPopup(context)
        popup.setSpeedChangeListener {
            speed = it
            Toaster.show("切换播放速度:${it}")
        }
        popup
    }


    private lateinit var actionCallback: PlayerCallback

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initController(context)
    }

    constructor(context: Context, attrs: AttributeSet?, callback: PlayerCallback) : super(
        context,
        attrs
    ) {
        initController(context)
        setActionCallback(callback)
    }

    fun setActionCallback(actionCallback: PlayerCallback) {
        this.actionCallback = actionCallback
        // 初始化可视视图
        actionCallback.featureEnabled().let {
            bottomControlView.setViewVisible(it.next, it.danmu, it.episode)
        }
    }

    private fun initController(context: Context) {
        val controller = object : StandardVideoController(context) {
            fun addDefaultControlComponent() {
                // 播放完成界面
                val completeView = CompleteView(getContext())
                // 错误页面
                val errorView: ErrorView = ErrorView(getContext())
                errorView.setOnRetryListener {
                    actionCallback.retryClick()
                    if (actionCallback == null){
                        // 如果没设置回调则直接退出
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        stopFullScreen()
                        release()
                    }
                }
                // 顶部
                topTitleView = TopTitleView(getContext())
                // 底部
                bottomControlView =
                    BottomControlView(getContext(), XKeyValue.getBoolean(Key.DANMAKU_STATE, true))
                // 手势控制
                val gestureView = GestureView(getContext())
                // 弹幕组件
                vDanmakuView =
                    SecDanmakuView(
                        context
                    )
                addControlComponent(completeView)
                addControlComponent(errorView)
                //                addControlComponent(prepareView)
                addControlComponent(topTitleView)
                addControlComponent(bottomControlView)
                addControlComponent(gestureView)
                addControlComponent(vDanmakuView)

                setEnableInNormal(true)
                // 投屏
                topTitleView.setThrowingScreenListener {
                    actionCallback.throwingScreenClick()
                }

                // 下一集
                bottomControlView.setNextVodListener {
                    actionCallback.nextClick()
                }
                // 选集
                bottomControlView.setChangeEposdeListener {
                    if (episodes == null) {
                        return@setChangeEposdeListener
                    }

                    videoEpisodePopup.showAtLocation(
                        activity.window.decorView,
                        Gravity.RIGHT,
                        0,
                        0
                    )

                }

                // 速度
                bottomControlView.setChangeSpeedListener {
                    videoSpeedPopup.showAtLocation(
                        activity.window.decorView,
                        Gravity.RIGHT,
                        0,
                        0
                    )
                }

                // 弹幕
                bottomControlView.setDanmuBtnListener {
                    if (it){
                        XKeyValue.putBoolean(Key.DANMAKU_STATE, true)
                        if (vDanmakuView.isPrepared) {
                            vDanmakuView.show()
                            LogUtils.d("弹幕已加载,直接显示")
                        }else{
                            // 加载弹幕
                            LogUtils.d("执行加载弹幕回调逻辑")
                            actionCallback.loadDanmaku(@DanmuVideoPlayer ::setDanmuStream)
                        }
                    }else{
                        XKeyValue.putBoolean(Key.DANMAKU_STATE, false)
                        // 关闭弹幕
                        vDanmakuView.hide()
                    }
                }
            }
        }
        // 初始化默认控制器
        controller.addDefaultControlComponent()
        // 设置控制器
        setVideoController(controller)
        //        setScreenScaleType(SCREEN_SCALE_DEFAULT)

        standardVideoController = controller
    }


    /**
     * 设置播放数据
     */
    /**
     * 设置播放数据
     */
    fun setUp(epsodeEntity: EpsodeEntity,position: Long) {
        vDanmakuView.release()
        standardVideoController.stopShowBufferSpeed()
        release()
        topTitleView.setTitle(epsodeEntity.videoName)


        if (epsodeEntity.videoUrl.contains("bilivideo")) {
            // TODO 占时兼容B站源
            val headers = mapOf(
                "Referer" to "https://www.bilibili.com",
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36"
            )
            setUrl(epsodeEntity.videoUrl, headers)
        } else {
            setUrl(epsodeEntity.videoUrl)
        }
        mCurrentPosition = position
        startPlay()
        standardVideoController.startShowBufferSpeed()

        if (XKeyValue.getBoolean(Key.DANMAKU_STATE, true)) {
            actionCallback.loadDanmaku(this::setDanmuStream)
        }
    }

    /**
     * 显示loading信息
     */
    fun setLoadingMsg(msg: String) {
        standardVideoController.stopShowBufferSpeed()
        if (currentPlayerState != STATE_IDLE) {
            release()
        }
        standardVideoController.setLoadingMsg(msg)
    }

    /**
     * 加载弹幕数据
     */
    private fun setDanmuStream(stream: File) {
        vDanmakuView.loadDanmuStream(stream)
    }

    interface PlayerCallback {
        fun featureEnabled(): ViewState
        fun nextClick()
        fun throwingScreenClick()
        fun selectPartsClick(position: Int)
        fun retryClick()
        fun loadDanmaku(callBack: (File) -> Unit)
    }

    /**
     * 下一集,弹幕,选集,投屏
     */
    data class ViewState(
        val next: Boolean = true,
        val danmu: Boolean = true,
        val episode: Boolean = true
    )
}
