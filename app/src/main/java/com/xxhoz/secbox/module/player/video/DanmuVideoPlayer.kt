package com.xxhoz.secbox.module.player.video;

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.hjq.toast.Toaster
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.module.player.popup.VideoEpisodePopup
import com.xxhoz.secbox.module.player.popup.VideoSpeedPopup
import com.xxhoz.secbox.module.player.video.view.BottomControlView
import com.xxhoz.secbox.module.player.video.view.ErrorView
import com.xxhoz.secbox.module.player.video.view.danma.SecDanmakuView

import xyz.doikki.videocontroller.component.CompleteView
import xyz.doikki.videocontroller.component.GestureView
import xyz.doikki.videocontroller.component.TopTitleView
import xyz.doikki.videoplayer.player.VideoView
import java.io.InputStream

/**
 * 包含弹幕的播放器
 */

class DanmuVideoPlayer : VideoView {

    lateinit var topTitleView: TopTitleView
    private lateinit var vDanmakuView : SecDanmakuView
    lateinit var standardVideoController: StandardVideoController
    lateinit var episodes: ArrayList<EpsodeEntity>

    val videoEpisodePopup: VideoEpisodePopup by lazy {
        val popup = VideoEpisodePopup(getContext(), episodes)
        popup.setEpisondeClickListener(object : VideoEpisodePopup.EpisodeClickListener {
            override fun onEpisodeClickListener(entity: EpsodeEntity, position: Int) {
                actionCallback.selectPartsClick(position)
            }
        })
        popup
    }

    val videoSpeedPopup by lazy {
        val popup = VideoSpeedPopup(getContext())
        popup.setSpeedChangeListener {
            speed = it
            Toaster.show("切换播放速度:${it}")
        }
        popup
    }


    private lateinit var actionCallback: PlayerCallback
    fun setActionCallback(actionCallback: PlayerCallback) {
        this.actionCallback = actionCallback
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initController(context)
    }

    private fun initController(context: Context) {
        val controller = object : StandardVideoController(context) {
            fun addDefaultControlComponent() {
                // 播放完成界面
                val completeView = CompleteView(getContext())
                // 错误页面
                val errorView: ErrorView = ErrorView(getContext())
                errorView.setOnRetryListener(){
                    actionCallback.retryClick()
                }
                // 顶部
                topTitleView = TopTitleView(getContext())
                // 底部
                val bottomControlView = BottomControlView(getContext())
                // 手势控制
                val gestureView = GestureView(getContext())
                // 弹幕组件
                vDanmakuView = SecDanmakuView(context)
                addControlComponent(completeView)
                addControlComponent(errorView)
    //                addControlComponent(prepareView)
                addControlComponent(topTitleView)
                addControlComponent(bottomControlView)
                addControlComponent(gestureView)
                addControlComponent(vDanmakuView)

                // 投屏
                topTitleView.setThrowingScreenListener() {
                    actionCallback.throwingScreenClick()
                }

                // 下一集
                bottomControlView.setNextVodListener() {
                    actionCallback.nextClick()
                }
                // 选集
                bottomControlView.setChangeEposdeListener() {
                    videoEpisodePopup.showAtLocation(
                        activity.getWindow().getDecorView(),
                        Gravity.RIGHT,
                        0,
                        0
                    )
                }

                // 速度
                bottomControlView.setChangeSpeedListener() {
                    videoSpeedPopup.showAtLocation(activity.getWindow().getDecorView(), Gravity.RIGHT, 0, 0)
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
    fun setUp(epsodeEntity: EpsodeEntity) {
        standardVideoController.stopShowBufferSpeed()
        release()
        topTitleView.setTitle(epsodeEntity.videoName)
        setUrl(epsodeEntity.videoUrl)
        startPlay()
        standardVideoController.startShowBufferSpeed()
    }

    /**
     * 显示loading信息
     */
    fun setLoadingMsg(msg:String){
        standardVideoController.stopShowBufferSpeed()
        if (currentPlayerState != STATE_IDLE){
            release()
        }
        standardVideoController.setLoadingMsg(msg)
    }

    /**
     * 加载弹幕数据
     */
    fun setDanmuStream(stream: InputStream){
        vDanmakuView.loadDanmuStream(stream)
    }

    interface PlayerCallback {
        fun nextClick()
        fun throwingScreenClick()
        fun selectPartsClick(position: Int)
        fun retryClick()
    }
}
