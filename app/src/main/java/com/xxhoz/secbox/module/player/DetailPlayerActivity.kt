package com.xxhoz.secbox.module.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.gyf.immersionbar.ktx.immersionBar
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.module.player.view.EpsodeEntity

class DetailPlayerActivity : BaseActivity<ActivityDetailPlayerBinding>() {


    override fun getPageName() = PageName.DETAIL_PLAYER
    override val inflater: (inflater: LayoutInflater) -> ActivityDetailPlayerBinding
        get() = ActivityDetailPlayerBinding::inflate

    companion object{
        fun startActivity(context: Context) {
            val intent = Intent(context, DetailPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var danmuVideoPlayer : DanmuVideoPlayer


    /**
     * 状态栏导航栏初始化
     */
    private fun initSystemBar() {
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
    }

    var currentItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystemBar()

        danmuVideoPlayer = viewBinding.danmakuPlayer
        var epsodeEntity = EpsodeEntity("https://vip.lz-cdn13.com/20230822/16734_2404dc45/index.m3u8","我是标题")
        danmuVideoPlayer.setUp(epsodeEntity)

        var playList = listOf("https://asf-doc.mushroomtrack.com/hls/lO5nTQ2PXLyAc6u9QZfdWQ/1693326049/35000/35494/35494.m3u8","http://vjs.zencdn.net/v/oceans.mp4",
            "https://asf-doc.mushroomtrack.com/hls/wGZULMKTXlFH25SdCEoXYA/1693326121/35000/35547/35547.m3u8","https://adoda-smart-coin.mushroomtrack.com/hls/kVYUrIVVdFD3WyDjt7DISw/1693326132/35000/35218/35218.m3u8",
        "https://hot-box-gen.mushroomtrack.com/hls/teyq4eqx-nJDUEzihmUt2w/1693326135/34000/34571/34571.m3u8","https://asf-doc.mushroomtrack.com/hls/cFLTX9IImZnbE6hqFMbHow/1693326133/35000/35484/35484.m3u8")


        viewBinding.nextBottom.setOnClickListener(){
            // 循环playList

            var play = playList[currentItem]
            var epsodeEntity = EpsodeEntity(play, currentItem.toString())
            danmuVideoPlayer.setUp(epsodeEntity)
            currentItem++;
        }
    }



    override fun onResume() {
        super.onResume()
        danmuVideoPlayer.resume()
    }


    override fun onPause() {
        super.onPause()
        danmuVideoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        danmuVideoPlayer.release()
    }

    override fun onBackPressed() {
        if (!danmuVideoPlayer.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
