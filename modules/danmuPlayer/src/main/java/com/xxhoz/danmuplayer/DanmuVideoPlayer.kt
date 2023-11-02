package com.xxhoz.danmuplayer


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.xxhoz.danmuplayer.popup.VideoEpisodePopup
import com.xxhoz.danmuplayer.popup.VideoSpeedPopup
import com.xxhoz.danmuplayer.view.MyDanmakuView
import com.xxhoz.secbox.module.detail.video.view.BottomControlView
import com.xxhoz.secbox.module.detail.video.view.ErrorView
import xyz.doikki.videocontroller.component.CompleteView
import xyz.doikki.videocontroller.component.GestureView
import xyz.doikki.videocontroller.component.TopTitleView
import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory
import xyz.doikki.videoplayer.player.VideoView
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager
import java.io.File

/**
 * 包含弹幕的播放器
 */

class DanmuVideoPlayer : VideoView {

    companion object {
        init {
            // 初始化播放器内核
            VideoViewManager.setConfig(
                VideoViewConfig.newBuilder()
                    //使用MediaPlayer解码
                    .setPlayerFactory(ExoMediaPlayerFactory.create())
                    .build()
            )
        }
    }


    lateinit var topTitleView: TopTitleView

    private lateinit var myDanmakuView: MyDanmakuView

    lateinit var standardVideoController: StandardVideoController

    var episodes: ArrayList<EpsodeEntity>? = null

    private lateinit var bottomControlView: BottomControlView

    // 弹幕开启状态 默认True
    var danmuState = true
        set(value) {
            myDanmakuView.danmuState = value
            bottomControlView.changeDanmuState(value)

            field = value
        }


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
//            Toaster.show("切换播放速度:${it}")
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
                }
                // 顶部
                topTitleView = TopTitleView(getContext())
                // 底部
                bottomControlView =
                    BottomControlView(getContext(), danmuState)
                // 手势控制
                val gestureView = GestureView(getContext())
                // 弹幕组件
                myDanmakuView =
                    MyDanmakuView(
                        context
                    )
                addControlComponent(completeView)
                addControlComponent(errorView)
                //                addControlComponent(prepareView)
                addControlComponent(topTitleView)
                addControlComponent(bottomControlView)
                addControlComponent(gestureView)
                addControlComponent(myDanmakuView)

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
                    if (it) {
                        danmuState = true
                        // 加载弹幕
                        actionCallback.loadDanmaku {
                            myDanmakuView.loadDanmuStream(it)
                        }
                    } else {
                        danmuState = false
                        // 关闭弹幕
                        myDanmakuView.hide()
                        actionCallback.closeDanmuKu()
                    }
                }
            }
        }
        // 初始化默认控制器
        controller.addDefaultControlComponent()
        // 设置控制器
        setVideoController(controller)
//        setScreenScaleType(SCREEN_SCALE_MATCH_PARENT)

        standardVideoController = controller
    }


    /**
     * 设置播放数据
     */
    /**
     * 设置播放数据
     */
    fun setUp(epsodeEntity: EpsodeEntity, position: Long) {
        myDanmakuView.release()
        standardVideoController.stopShowBufferSpeed()
        release()
        topTitleView.setTitle(epsodeEntity.videoName)

        setUrl(epsodeEntity.videoUrl)
        mCurrentPosition = position
        startPlay()
        standardVideoController.startShowBufferSpeed()

        if (danmuState) {
            actionCallback.loadDanmaku {
                myDanmakuView.loadDanmuStream(it)
            }
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


    fun danmuIsPrepared(): Boolean {
        return myDanmakuView.isPrepared
    }

    fun danmuShow() {
        myDanmakuView.show()
    }

    fun danmuHide() {
        myDanmakuView.hide()
    }

    interface PlayerCallback {
        fun featureEnabled(): ViewState
        fun nextClick()
        fun throwingScreenClick()
        fun selectPartsClick(position: Int)

        // 重试回调
        fun retryClick()

        // 加载弹幕回调
        fun loadDanmaku(callBack: (File) -> Unit)

        // 关闭弹幕回调
        fun closeDanmuKu()
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
