package com.xxhoz.secbox.module.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.MainThread
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.PageState
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.util.setImageUrl


class DetailPlayerActivity() : BaseActivity<ActivityDetailPlayerBinding>() ,
    DanmuVideoPlayer.PlayerCallback {

    private val viewModel: DetailPlayerViewModel by viewModels()

    private lateinit var channelTab: TabLayout
    private lateinit var episodeTab: TabLayout

    private lateinit var videoPlayer: DanmuVideoPlayer

    override fun getPageName() = PageName.DETAIL_PLAYER
    override val inflater: (inflater: LayoutInflater) -> ActivityDetailPlayerBinding
        get() = ActivityDetailPlayerBinding::inflate

    val gson = GsonFactory.getSingletonGson()

    companion object {
        fun startActivity(context: Context, playInfoBean: PlayInfoBean) {
            val intent = Intent(context, DetailPlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("playInfoBean", playInfoBean)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
        // 初始化视图逻辑
        initView()
        // 初始化数据
        initData()
    }

    private fun initData() {
        val playInfoBean = intent.getSerializableExtra("playInfoBean") as PlayInfoBean
        viewModel.initData(playInfoBean)
    }

    private fun initView() {

        viewBinding.button.setOnClickListener(){
            episodeTab.setScrollPosition(7, 0F, true);
        }
        videoPlayer = viewBinding.danmakuPlayer
        videoPlayer.actionCallback = this

        channelTab = viewBinding.channelTab
        episodeTab = viewBinding.episodeTab


        // 详情变化
        viewModel.videoDetailBean.observe(this) {
            viewBinding.titleText.text= it.vod_name
            viewBinding.roundAngleImageView.setImageUrl(it.vod_pic)
            viewBinding.descText.text = removeHtmlAndWhitespace(it.vod_content)
            viewBinding.textView.text = "${it.vod_year?:"-"}  /  ${it.type_name?:"-"}  /  ${it.vod_director?:"-"}"
            viewBinding.currentSourceText.text = viewModel.spiderSource.value!!.sourceBean.name
        }

        // 线路和剧集变化
        viewModel.channelFlagsAndEpisodes.observe(this){
            renderChannelsTab(it)
//            renderEpisodesTab(it)
        }

//        // 选择线路
//        viewModel.currentChannel.observe(this){
//
//        }
//
//        // 选择剧集
//        viewModel.currentEpisode.observe(this){
//
//        }

        // 当前解析接口
        viewModel.currentParseBean.observe(this){
//            Toaster.show("当前解析接口切换:" + it.name)
        }

        // 加载提示信息
        viewModel.stateVideoPlayerMsg.observe(this){
            videoPlayer.setLoadingMsg(it)
        }

        // 播放链接变化
        viewModel.playEntity.observe(this){
            videoPlayer.setUp(it)
        }

        viewModel.danmuStream.observe(this){
            videoPlayer.setDanmuStream(it)
        }

        viewModel.pageState.observe(this){
            when(it){
                PageState.LOADING -> {
                    viewBinding.promptView.showLoading()
                }
                PageState.NORMAL -> {
                    viewBinding.promptView.hide()
                }
                PageState.LOAD_ERROR -> {
                    viewBinding.promptView.showNetworkError({
                        initData()
                    })
                }
            }
        }

        // 切换线路逻辑
        channelTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.currentChannel.value = tab.position
                // 渲染剧集列表
                renderEpisodesTab(viewModel.channelFlagsAndEpisodes.value!!)
                // 如果切换的资源集数不够则播放最后一集
                val tabCount = episodeTab.tabCount
                if (viewModel.currentEpisode.value != 0 && viewModel.currentEpisode.value!! >= tabCount) {
                    viewModel.currentEpisode.value = tabCount - 1
                }
                episodeTab.getTabAt(viewModel.currentEpisode.value!!)!!.select()
                Handler(Looper.getMainLooper()).postDelayed({
                    episodeTab.setScrollPosition(viewModel.currentEpisode.value!!, 0F, true);
                },400)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        // 选集逻辑
        episodeTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabView(tab, true)
                tab.setTag(System.currentTimeMillis())
                epTabItemClick(tab)
                videoPlayer.videoEpisodePopup.setPlayNum(tab.position + 1)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabView(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 点击间隔限制2秒
                if ((System.currentTimeMillis() - tab.tag as Long) < 2000) {
                    return
                }
                // 再次选择当前选项卡 行为:刷新重新加载
                epTabItemClick(tab);
            }
        })

    }


    /**
     * 剧集列表点击事件
     */
    private fun epTabItemClick(tab: TabLayout.Tab) {
        viewModel.currentEpisode.value = tab.position
        viewModel.getPlayUrl()
    }



    /**
     * 用来改变tabLayout选中后的字体大小及颜色
     *
     * @param tab
     * @param isSelect
     */
    private fun updateTabView(tab: TabLayout.Tab, isSelect: Boolean) {
        //找到自定义视图的控件ID
        val tv_tab = tab.customView!!.findViewById<TextView>(R.id.tab_video_episodes_tv)
        if (isSelect) {
            //设置标签选中
            tv_tab.isSelected = true
            //选中后字体
            tv_tab.setTextColor(resources.getColor(R.color.ThemeColor))
        } else {
            //设置标签取消选中
            tv_tab.isSelected = false
            //恢复为默认字体
            tv_tab.setTextColor(resources.getColor(R.color.font_color))
        }
    }


    @MainThread
    private fun renderChannelsTab(channelFlagsAndEpisodes : List<VideoDetailBean.ChannelEpisodes>) {
        channelTab.removeAllTabs()
        // 初始化 线路列表
        for (channel in channelFlagsAndEpisodes) {
            channelTab.addTab(channelTab.newTab().setText(channel.channelFlag))
        }
    }

    /**
     * 加载 当前线路的剧集列表
     */
    @MainThread
    private fun renderEpisodesTab(channelFlagsAndEpisodes: List<VideoDetailBean.ChannelEpisodes>) {
        episodeTab.removeAllTabs()
        var channelEpisodes = channelFlagsAndEpisodes.get(viewModel.currentChannel.value!!)

        // 设置播放器控件选集数据
        val eposodeList = ArrayList<EpsodeEntity>()
        channelEpisodes.episodes.forEach {
            eposodeList.add(EpsodeEntity(it.name,""))
        }
        videoPlayer.episodes = eposodeList

        for (episode in channelEpisodes.episodes) {
            val newTab = episodeTab.newTab()
            newTab.setCustomView(R.layout.item_tab_video_episodes)
            var textView: TextView = newTab.customView!!.findViewById(R.id.tab_video_episodes_tv)
            textView.text = episode.name
            episodeTab.addTab(newTab,false)
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

    /**
     * 点击下一集
     */
    override fun nextClick() {
        if (episodeTab.tabCount > viewModel.currentEpisode.value!! + 1) {
            episodeTab.getTabAt(viewModel.currentEpisode.value!! + 1)?.select()
        } else {
            Toaster.show("已经是最后一集了")
        }
    }

    /**
     * 点击投屏
     */
    override fun throwingScreenClick() {
        Toaster.show("待开发")
    }

    /**
     * 选集点击
     */
    override fun selectPartsClick(position: Int) {
        episodeTab.getTabAt(position)?.select()
    }

    /**
     * 播放错误从试按钮
     */
    override fun retryClick() {
        episodeTab.getTabAt(viewModel.currentEpisode.value!!)!!.select()
    }

    fun removeHtmlAndWhitespace(input: String): String {
        // 去除HTML标签
        val noHtmlTags = input.replace(Regex("<[^>]*>"), "")
        // 去除多个空白字符（包括空格、制表符、换行符等）替换为单个空格
        val noWhitespace = noHtmlTags.replace(Regex("\\s+"), " ")
        return noWhitespace.trim() // 去除字符串两端的空格
    }

}
