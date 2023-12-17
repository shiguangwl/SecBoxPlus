package com.xxhoz.danmuplayer


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.xxhoz.danmuplayer.config.DmSettingContext
import com.xxhoz.danmuplayer.popup.DmSetting
import com.xxhoz.danmuplayer.popup.DmSettingConfig
import com.xxhoz.danmuplayer.popup.VideoDanmuSettingPopup
import com.xxhoz.danmuplayer.popup.VideoEpisodePopup
import com.xxhoz.danmuplayer.popup.VideoSpeedPopup
import com.xxhoz.danmuplayer.view.MyDanmakuView
import com.xxhoz.secbox.module.detail.video.view.BottomControlView
import com.xxhoz.secbox.module.detail.video.view.ErrorView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.IDisplayer
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


    val videoDanmuSettingPopup: VideoDanmuSettingPopup by lazy {
        val popup = VideoDanmuSettingPopup(context)
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

                val dmSettingContext = DmSettingContext(context)
                val dmSetting = dmSettingContext.loadDmSetting()
                updateDmConfig(dmSetting)
                // 使用默认配置
                videoDanmuSettingPopup.setConfigValue(DmSettingConfig(), dmSetting)
                // 弹幕配置修改回调
                videoDanmuSettingPopup.setChangeCallback {
                    // 持久化配置
                    updateDmConfig(it)
                    dmSettingContext.saveDmSetting(it)
                }
                // 弹幕设置
                bottomControlView.setDanmuSettingBtnListener {

                    videoDanmuSettingPopup.showAtLocation(
                        activity.window.decorView,
                        Gravity.RIGHT,
                        0,
                        0
                    )
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
     * 更新弹幕配置
     */
    private fun updateDmConfig(danmuSetting: DmSetting) {
        val mContext = myDanmakuView.getmContext()
        // 设置弹幕的最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        // TYPE_SCROLL_RL 从右至左滚动弹幕
        // TYPE_SCROLL_LR 从左至右滚动弹幕
        // TYPE_FIX_TOP 顶端固定弹幕
        // TYPE_FIX_BOTTOM 底端固定弹幕
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = danmuSetting.maximumLines // 滚动弹幕最大显示行数
        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_LR] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_BOTTOM] = true

        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            .setDuplicateMergingEnabled(false) //是否启用合并重复弹幕
            .setScrollSpeedFactor(danmuSetting.scrollSpeed) //设置弹幕滚动速度系数,只对滚动弹幕有效
            .setScaleTextSize(danmuSetting.scaleTextSize) // 设置缩放字体大小
            .setMaximumLines(maxLinesPair) //设置最大显示行数
            .preventOverlapping(overlappingEnablePair) //设置防弹幕重叠，null为允许重叠

        //调用重绘
        myDanmakuView.invalidate()
    }


    override fun release() {
        myDanmakuView.release()
        super.release()
    }

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
