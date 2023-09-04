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
import com.xxhoz.secbox.module.player.popup.view.EpsodeEntity
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer


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

    var currentItem = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystemBar()
        var playList = listOf(
            "https://vip.lz-cdn13.com/20230822/16734_2404dc45/index.m3u8",
            "https://v.cdnlz15.com/20230902/1041_77d2cda9/index.m3u8",
            "http://vjs.zencdn.net/v/oceans.mp4",
            "https://v.cdnlz3.com/20230904/21590_26953396/index.m3u8"
        )


        danmuVideoPlayer = viewBinding.danmakuPlayer
        var epsodeEntity = EpsodeEntity(playList[0],"我是标题")
        Thread{
            runOnUiThread(){
                danmuVideoPlayer.setLoadingMsg("加载数据中...")
            }
            Thread.sleep(2500)
            runOnUiThread(){
                danmuVideoPlayer.setUp(epsodeEntity)
            }
        }.start()


        viewBinding.nextBottom.setOnClickListener(){


            Thread{
                // 循环playList
                var play = playList[currentItem % playList.size]
                var epsodeEntity = EpsodeEntity(play, currentItem.toString())

                runOnUiThread(){
                    danmuVideoPlayer.setLoadingMsg("加载数据中...")
                }
                Thread.sleep(2500)
                runOnUiThread(){
                    danmuVideoPlayer.setUp(epsodeEntity)
                }
                currentItem++;
            }.start()

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
