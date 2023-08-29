package com.xxhoz.secbox.module.player

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.gyf.immersionbar.ktx.immersionBar
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.listener.LockClickListener
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.module.player.video.DanmakuVideoPlayer
import java.io.File
import java.io.FileOutputStream

class DetailPlayerActivity : BaseActivity<ActivityDetailPlayerBinding>() {

    private var isPlay = false
    private var isPause = false
    private var isDestory = false
    private var lastClidkTime:Long = -1;
    private var orientationUtils: OrientationUtils? = null

    override fun getPageName() = PageName.DETAIL_PLAYER
    override val inflater: (inflater: LayoutInflater) -> ActivityDetailPlayerBinding
        get() = ActivityDetailPlayerBinding::inflate

    companion object{
        fun startActivity(context: Context) {
            val intent = Intent(context, DetailPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystemBar()


        //使用自定义的全屏切换图片，!!!注意xml布局中也需要设置为一样的
//        //必须在setUp之前设置
//        viewBinding.danmakuPlayer.setShrinkImageRes(R.drawable.custom_shrink)
//        viewBinding.danmakuPlayer.setEnlargeImageRes(R.drawable.custom_enlarge)

        //String url = "https://res.exexm.com/cw_145225549855002";

        //String url = "https://res.exexm.com/cw_145225549855002";
        val url = "https://vip.lz-cdn13.com/20230822/16734_2404dc45/index.m3u8"
        //String url = "https://res.exexm.com/cw_145225549855002";
        //String url = "https://res.exexm.com/cw_145225549855002";
        viewBinding.danmakuPlayer.setUp(url, true, null, "测试视频")

        //增加封面
//        val imageView = ImageView(this)
//        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//        imageView.setImageResource(R.mipmap.xxx1)
//        viewBinding.danmakuPlayer.setThumbImageView(imageView)

        resolveNormalVideoUI()

        //外部辅助的旋转，帮助全屏
        orientationUtils = OrientationUtils(this, viewBinding.danmakuPlayer)
        //初始化不打开外部的旋转
        orientationUtils!!.setEnable(false)

        viewBinding.danmakuPlayer.setIsTouchWiget(true)
        //关闭自动旋转
        viewBinding.danmakuPlayer.setRotateViewAuto(false)
        viewBinding.danmakuPlayer.setLockLand(false)
        viewBinding.danmakuPlayer.setShowFullAnimation(false)
        viewBinding.danmakuPlayer.setNeedLockFull(true)
        viewBinding.danmakuPlayer.setReleaseWhenLossAudio(false)

//        viewBinding.danmakuPlayer.setOpenPreView(true);

        //detailPlayer.setOpenPreView(true);
        viewBinding.danmakuPlayer.getFullscreenButton().setOnClickListener(View.OnClickListener { //直接横屏
            // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
            // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
            orientationUtils!!.resolveByClick()

            //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
            viewBinding.danmakuPlayer.startWindowFullscreen(this, true, true)
        })

        viewBinding.danmakuPlayer.currentState
        viewBinding.danmakuPlayer.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onPrepared(url: String, vararg objects: Any) {
                super.onPrepared(url, *objects)
                //开始播放了才能旋转和全屏
                orientationUtils!!.setEnable(viewBinding.danmakuPlayer.isRotateWithSystem())
                isPlay = true
                getDanmu()
            }

            override fun onAutoComplete(url: String, vararg objects: Any) {
                super.onAutoComplete(url, *objects)
            }

            override fun onClickStartError(url: String, vararg objects: Any) {
                super.onClickStartError(url, *objects)
            }

            override fun onQuitFullscreen(url: String, vararg objects: Any) {
                super.onQuitFullscreen(url, *objects)

                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils?.backToProtVideo()
            }
        })

        viewBinding.danmakuPlayer.setLockClickListener(LockClickListener { view, lock ->
            orientationUtils?.setEnable(!lock)
        })


        viewBinding.danmakuPlayer.startPlayLogic()

        viewBinding.nextBottom.setOnClickListener(View.OnClickListener {

            // 判断当前时间和上次点击间隔是否超过300ms
            if (System.currentTimeMillis() - lastClidkTime < 1000) {
                return@OnClickListener
            }
            lastClidkTime = System.currentTimeMillis()
//            viewBinding.danmakuPlayer.release()
//            viewBinding.danmakuPlayer.danmakuView.release()
            viewBinding.danmakuPlayer.setUp("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", true, null, "测试视频11")
            viewBinding.danmakuPlayer.startPlayLogic()
        })

    }



    /**
     * 状态栏导航栏初始化
     */
    private fun initSystemBar() {
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
    }


    override fun onBackPressed() {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        orientationUtils?.backToProtVideo()
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }


    override fun onPause() {
        getCurPlay().onVideoPause()
        super.onPause()
        isPause = true
    }

    override fun onResume() {
        getCurPlay().onVideoResume()
        super.onResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlay) {
            getCurPlay().release()
        }
        //GSYPreViewManager.instance().releaseMediaPlayer();
        orientationUtils?.releaseListener()
        isDestory = true
    }


    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            viewBinding.danmakuPlayer.onConfigurationChanged(
                this,
                newConfig,
                orientationUtils,
                true,
                true
            )
        }
    }


    private fun getDanmu() {
        (viewBinding.danmakuPlayer.getCurrentPlayer() as DanmakuVideoPlayer).setDanmaKuStream(getRawFileAsFile(this , R.raw.comments, "comments.xml"))
    }

    fun getRawFileAsFile(context: Context, resourceId: Int, fileName: String): File? {
        val outputFile = File(context.filesDir, fileName)

        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return outputFile
    }

    /**
     * 非全屏状态的播放器UI
     */
    private fun resolveNormalVideoUI() {
        //增加title
        viewBinding.danmakuPlayer.getTitleTextView().setVisibility(View.GONE)
        viewBinding.danmakuPlayer.getBackButton().setVisibility(View.GONE)
    }

    private fun getCurPlay(): GSYVideoPlayer {
        return if (viewBinding.danmakuPlayer.getFullWindowPlayer() != null) {
            viewBinding.danmakuPlayer.getFullWindowPlayer()
        } else viewBinding.danmakuPlayer
    }
}
