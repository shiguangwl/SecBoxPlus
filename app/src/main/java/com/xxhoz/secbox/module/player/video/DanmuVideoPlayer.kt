package com.xxhoz.secbox.module.player.video;

import android.content.Context
import android.util.AttributeSet
import com.hjq.toast.Toaster
import com.xxhoz.secbox.module.player.video.view.BottomControlView
import com.xxhoz.secbox.module.player.view.EpsodeEntity
import xyz.doikki.videocontroller.component.CompleteView
import xyz.doikki.videocontroller.component.ErrorView
import xyz.doikki.videocontroller.component.GestureView
import xyz.doikki.videocontroller.component.TopTitleView
import xyz.doikki.videoplayer.player.VideoView

/**
 * 包含弹幕的播放器
 */

class DanmuVideoPlayer : VideoView {

    lateinit var topTitleView: TopTitleView
    lateinit private var vDanmakuView : SecDanmakuView

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
                val errorView = ErrorView(getContext())
    //                // 加载UI
    //                val prepareView = PrepareView(getContext())
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
                    Toaster.show("投屏")
                }

                // 下一集
                bottomControlView.setNextVodListener() {
                    Toaster.show("下一集")
                }
                // 选集
                bottomControlView.setChangeEposdeListener() {
                    Toaster.show("选集")
                }
                // 速度
                bottomControlView.setChangeSpeedListener() {
                    Toaster.show("速度")
                }
            }
        }
        // 初始化默认控制器
        controller.addDefaultControlComponent()
        // 设置控制器
        setVideoController(controller)
        //        setScreenScaleType(SCREEN_SCALE_DEFAULT)
    }


    /**
     * 设置播放数据
     */
    fun setUp(epsodeEntity: EpsodeEntity) {
        release()
        topTitleView.setTitle(epsodeEntity.videoName)
        setUrl(epsodeEntity.videoUrl)
        start()
    }


//    fun setUp(epsodeEntity: EpsodeEntity,isChangeUrl: Boolean) {
//        release()
//        topTitleView.setTitle(epsodeEntity.videoName)
//        setUrl(epsodeEntity.videoUrl)
//        start()
//    }

    /**
     * 加载弹幕数据
     */
    fun loadDamu(){

    }

    interface PlayerCallback {
        fun nextClick()
        fun backClick()
        fun throwingScreenClick()
        fun selectPartsClick()
        fun speedClick()
    }
}
