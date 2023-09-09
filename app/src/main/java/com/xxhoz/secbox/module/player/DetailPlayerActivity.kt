package com.xxhoz.secbox.module.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.setImageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.UnknownHostException


class DetailPlayerActivity() : BaseActivity<ActivityDetailPlayerBinding>() ,
    DanmuVideoPlayer.PlayerCallback {


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

    private lateinit var playInfoBean: PlayInfoBean
    private lateinit var spiderSource: IBaseSource

    private lateinit var videoPlayer: DanmuVideoPlayer

    // 详情信息
    private var videoDetailBean: VideoDetailBean? = null

    // 线路和剧集
    private var channelFlagsAndEpisodes: List<VideoDetailBean.ChannelEpisodes>? = null

    // 当前选择的线路
    private var currentChannel: Int = 0

    // 当前选择的剧集
    private var currentEpisode: Int = 0

    // 当前解析接口对象
    private lateinit var currentParseBean: ParseBean


    private lateinit var channelTab: TabLayout
    private lateinit var episodeTab: TabLayout

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystemBar()

        initView()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                viewBinding.promptView.showLoading()
                withContext(Dispatchers.IO) {
                    initData()
                }
            } catch (e: UnknownHostException) {
                Toaster.show("请检查网络连接")
                showErrorView()
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorView()
            }
        }


    }

    private fun initView() {

        viewBinding.button.setOnClickListener(){
            episodeTab.setScrollPosition(7, 0F, true);
        }
        videoPlayer = viewBinding.danmakuPlayer
        videoPlayer.setActionCallback(this)

        channelTab = viewBinding.channelTab
        episodeTab = viewBinding.episodeTab


        var parseBeanList: List<ParseBean> = SourceManger.getParseBeanList()
        // 只要嗅探和JSON
        parseBeanList = parseBeanList.filter { it.type == 1 || it.type == 0 }
        currentParseBean = parseBeanList.get(2)


        // 切换线路逻辑
        channelTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentChannel = tab.position
                renderEpisodesTab()

                // 如果切换的资源集数不够则播放最后一集
                val tabCount = episodeTab.tabCount
                if (currentEpisode >= tabCount) {
                    currentEpisode = tabCount - 1
                }
                episodeTab.getTabAt(currentEpisode)!!.select()
                Handler(Looper.getMainLooper()).postDelayed({
                    // FIXME 不加延迟不生效???
                    episodeTab.setScrollPosition(currentEpisode, 0F, true);
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
        currentEpisode = tab.position
        videoPlayer.setLoadingMsg("加载数据中...")

        val currentChannelData: VideoDetailBean.ChannelEpisodes = videoDetailBean!!.getChannelFlagsAndEpisodes().get(currentChannel)
        val currenSelectEposode: VideoDetailBean.Value =
            currentChannelData.episodes.get(currentEpisode)
        lifecycleScope.launch(Dispatchers.IO) {

            val playUrl:String = try {
                getPlayUrl(currentChannelData.channelFlag, currenSelectEposode, currentParseBean)
            } catch (e: GlobalException) {
                Toaster.show(e.message)
                return@launch
            } catch (e: Exception) {
                Toaster.show("获取播放链接失败,未知错误")
                e.printStackTrace()
                return@launch
            }

            LogUtils.d("最终播放链接:  ${playUrl}")
            val epsodeEntity: EpsodeEntity =
                EpsodeEntity(
                    currenSelectEposode.name,
                    playUrl
                )
            runOnUiThread(){
                startPlay(epsodeEntity)
            }
        }
    }

    @MainThread
    private fun startPlay(epsodeEntity: EpsodeEntity) {
        videoPlayer.setUp(epsodeEntity)
    }

    /**
     * @param currentChannelData 当前线路数据
     * @param currenSelectEposode 当前选中的剧集
     * @param parseBean 当前解析接口对象
     * @return 播放链接
     */
    @WorkerThread
    private fun getPlayUrl(
        channelFlag: String,
        currenSelectEposode: VideoDetailBean.Value,
        parseBean: ParseBean,
    ): String {
        val playLinkBean: PlayLinkBean =
            spiderSource.playInfo(channelFlag, currenSelectEposode.urlCode)
        LogUtils.i("影视播放数据: ${playLinkBean}")

        if (playLinkBean.parse == 1) {
            // 解析播放链接
            onStateVideoPlayerMsg("解析数据中...")

            val parseRsult = try {
                val jsonObject =
                    HttpUtil.get(parseBean.url + playLinkBean.url, JSONObject::class.java)
                jsonObject.getString("url")
            } catch (e: Exception) {
                e.printStackTrace()
                throw GlobalException.of("[${parseBean.name}] 解析失败")
            }
            playLinkBean.url = parseRsult

        } else if (playLinkBean.parse == 0) {
            // TODO 嗅探播放链接
            onStateVideoPlayerMsg("开始嗅探链接...")

        }

        return playLinkBean.url
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

    private suspend fun initData() {
        playInfoBean = intent.getSerializableExtra("playInfoBean") as PlayInfoBean
        // TODO 空指针检测
        spiderSource = SourceManger.getSpiderSource(playInfoBean.sourceKey)!!


        try {
            videoDetailBean = spiderSource.videoDetail(listOf(playInfoBean.videoBean.vod_id))
            LogUtils.i("影视详情数据: ${videoDetailBean}")

            channelFlagsAndEpisodes = videoDetailBean!!.getChannelFlagsAndEpisodes()
        } catch (e: Exception) {
            showErrorView()
            LogUtils.d("获取影视详情数据失败: ${e.message}")
            e.printStackTrace()
            return
        }

        if (videoDetailBean == null) {
            Toaster.showLong("数据为空")
            showErrorView()
            // TODO 处理自动切换源
            return
        }

        // 渲染详情视图
        withContext(Dispatchers.Main) {
            renderChanelsTab()
            renderEpisodesTab()

            videoDetailBean?.let {
                viewBinding.titleText.text= it.vod_name
                viewBinding.roundAngleImageView.setImageUrl(it.vod_pic)
                viewBinding.descText.text = removeHtmlAndWhitespace(it.vod_content)
                viewBinding.textView.text = "${it.vod_year?:"-"}  /  ${it.type_name?:"-"}  /  ${it.vod_director?:"-"}"
            }
            viewBinding.currentSourceText.text = BaseConfig.getCurrentSource()!!.sourceBean.name
            // 选择channel自动播放
            channelTab.getTabAt(currentEpisode)?.select()
            viewBinding.promptView.hide()
        }

    }

    @MainThread
    private fun renderChanelsTab() {
        channelTab.removeAllTabs()

        // 初始化 线路列表
        for (channel in channelFlagsAndEpisodes!!) {
            channelTab.addTab(channelTab.newTab().setText(channel.channelFlag),false)
        }
    }

    /**
     * 加载 当前线路的剧集列表
     */
    private fun renderEpisodesTab() {
        episodeTab.removeAllTabs()

        var channelEpisodes =
            channelFlagsAndEpisodes!!.find { it.channelFlag.equals(currentChannel) }
        if (channelEpisodes == null) {
            channelEpisodes = channelFlagsAndEpisodes!!.get(0)
        }

        for (episode in channelEpisodes.episodes) {
            val newTab = episodeTab.newTab()
            newTab.setCustomView(R.layout.item_tab_video_episodes)
            var textView: TextView = newTab.customView!!.findViewById(R.id.tab_video_episodes_tv)
            textView.text = episode.name
            episodeTab.addTab(newTab,false)
        }

        // 设置播放器控件选集数据
        val eposodeList = ArrayList<EpsodeEntity>()
        channelEpisodes.episodes.forEach {
            eposodeList.add(EpsodeEntity(it.name,""))
        }
        videoPlayer.episodes = eposodeList
    }

    private fun showErrorView() {
        runOnUiThread(){
            viewBinding.promptView.showNetworkError({
                lifecycleScope.launch(Dispatchers.IO) {
                    initData()
                }
            })
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


    fun onStateVideoPlayerMsg(msg: String) {
        runOnUiThread() {
            videoPlayer.setLoadingMsg(msg)
        }
    }

    /**
     * 点击下一集
     */
    override fun nextClick() {
        if (episodeTab.tabCount > currentEpisode + 1) {
            episodeTab.getTabAt(currentEpisode + 1)?.select()
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

    fun removeHtmlAndWhitespace(input: String): String {
        // 去除HTML标签
        val noHtmlTags = input.replace(Regex("<[^>]*>"), "")
        // 去除多个空白字符（包括空格、制表符、换行符等）替换为单个空格
        val noWhitespace = noHtmlTags.replace(Regex("\\s+"), " ")
        return noWhitespace.trim() // 去除字符串两端的空格
    }



}
