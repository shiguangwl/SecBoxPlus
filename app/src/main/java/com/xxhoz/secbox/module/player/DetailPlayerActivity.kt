package com.xxhoz.secbox.module.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.module.player.popup.view.EpsodeEntity
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException


class DetailPlayerActivity() : BaseActivity<ActivityDetailPlayerBinding>() {


    override fun getPageName() = PageName.DETAIL_PLAYER
    override val inflater: (inflater: LayoutInflater) -> ActivityDetailPlayerBinding
        get() = ActivityDetailPlayerBinding::inflate
    val gson = GsonFactory.getSingletonGson()
    companion object{
        fun startActivity(context: Context, playInfoBean: PlayInfoBean) {
            val intent = Intent(context, DetailPlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("playInfoBean",playInfoBean)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    private lateinit var videoPlayer : DanmuVideoPlayer


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

        videoPlayer = viewBinding.danmakuPlayer
        lifecycleScope.launch(Dispatchers.Main){
            try {
                viewBinding.promptView.showLoading()
                withContext(Dispatchers.IO){
                    initData()
                }
                viewBinding.promptView.hide()
            }catch (e: UnknownHostException){
                Toaster.show("请检查网络连接")
                showErrorView()
            }catch (e:Exception){
                e.printStackTrace()
                showErrorView()
            }
        }

//        var playList = listOf(
//            "https://vip.lz-cdn13.com/20230822/16734_2404dc45/index.m3u8",
//            "https://v.cdnlz15.com/20230902/1041_77d2cda9/index.m3u8",
//            "http://vjs.zencdn.net/v/oceans.mp4",
//            "https://v.cdnlz3.com/20230904/21590_26953396/index.m3u8"
//        )
//
//        danmuVideoPlayer = viewBinding.danmakuPlayer
//        var epsodeEntity = EpsodeEntity(playList[0],"我是标题")
//
//        danmuVideoPlayer.setLoadingMsg("加载数据中...")
//        danmuVideoPlayer.setUp(epsodeEntity)
//
//
//
//        viewBinding.nextBottom.setOnClickListener(){
//
//
//            Thread{
//                // 循环playList
//                var play = playList[currentItem % playList.size]
//                var epsodeEntity = EpsodeEntity(play, currentItem.toString())
//
//                runOnUiThread(){
//                    danmuVideoPlayer.setLoadingMsg("加载数据中...")
//                }
//                Thread.sleep(2500)
//                runOnUiThread(){
//                    danmuVideoPlayer.setUp(epsodeEntity)
//                }
//                currentItem++;
//            }.start()
//
//        }
    }

    private fun showErrorView() {
        viewBinding.promptView.showNetworkError({
            lifecycleScope.launch(Dispatchers.IO) {
                initData()
            }
        })
    }

    private suspend fun initData() {
        val playInfoBean: PlayInfoBean = intent.getSerializableExtra("playInfoBean") as PlayInfoBean
        val spiderSource = SourceManger.getSpiderSource(playInfoBean.sourceKey)

        if (spiderSource == null){
            Toaster.showLong("未找到播放源")
            return
        }

        var videoDetailBean:VideoDetailBean = spiderSource.videoDetail(listOf(playInfoBean.videoBean.vod_id))
        LogUtils.i("影视详情数据: ${videoDetailBean}")

        val first: VideoDetailBean.ChannelEpisodes = videoDetailBean.getChannelFlagsAndEpisodes().get(0)
        val playLinkBean: PlayLinkBean = spiderSource.playInfo(first.channel, first.episodes.get(0).urlCode)
        LogUtils.i("影视播放数据: ${playLinkBean}")


        var name = first.episodes.get(0).name
        val epsodeEntity: EpsodeEntity = EpsodeEntity(name, playLinkBean.url)

        withContext(Dispatchers.Main){
            videoPlayer.setLoadingMsg("加载数据中...")
            videoPlayer.setUp(epsodeEntity)
        }
    }


    override fun onResume() {
        super.onResume()
        videoPlayer.resume()
    }


    override fun onPause() {
        super.onPause()
        videoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.release()
    }

    override fun onBackPressed() {
        if (!videoPlayer.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
