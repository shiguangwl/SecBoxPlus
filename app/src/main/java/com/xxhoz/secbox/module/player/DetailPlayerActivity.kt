package com.xxhoz.secbox.module.player

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.MainThread
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.xxhoz.constant.Key
import com.xxhoz.danmuplayer.DanmuVideoPlayer
import com.xxhoz.danmuplayer.EpsodeEntity
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.PageState
import com.xxhoz.secbox.databinding.ActivityDetailPlayerBinding
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.persistence.CRUD
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.StringUtils
import com.xxhoz.secbox.util.getActivity
import com.xxhoz.secbox.util.setImageUrl
import com.xxhoz.secbox.widget.RoundAngleImageView
import java.io.File


class DetailPlayerActivity : BaseActivity<ActivityDetailPlayerBinding>(),
    DanmuVideoPlayer.PlayerCallback, View.OnClickListener {

    private lateinit var loadDanmuCallback: (File) -> Unit
    private val viewModel: DetailPlayerViewModel by viewModels()

    private lateinit var channelTab: TabLayout
    private lateinit var episodeTab: TabLayout

    private lateinit var videoPlayer: DanmuVideoPlayer

    var shareVodPicView: RoundAngleImageView? = null
    private var position = 0L
    override fun getPageName() = PageName.DETAIL_PLAYER
    override val inflater: (inflater: LayoutInflater) -> ActivityDetailPlayerBinding
        get() = ActivityDetailPlayerBinding::inflate

    val gson = GsonFactory.getSingletonGson()

    companion object {
        fun startActivity(
            context: Context,
            playInfoBean: PlayInfoBean,
            tvSharedElement: View? = null
        ) {
            val intent = Intent(context, DetailPlayerActivity::class.java)
            val bundle = Bundle()

            // 查找播放记录
            var playInfo = playInfoBean
            var playInfoBeans: ArrayList<PlayInfoBean>? =
                XKeyValue.getObjectList<PlayInfoBean>(Key.PLAY_History)
            if (playInfoBeans == null) {
                playInfoBeans = ArrayList()
            }
            for (i in playInfoBeans.indices) {
                if (
                    (playInfoBeans[i].videoBean.vod_id == playInfoBean.videoBean.vod_id) &&
                    (playInfoBeans[i].sourceKey == playInfoBean.sourceKey)
                )
                    playInfo = playInfoBeans[i]
                break
            }

            bundle.putSerializable("playInfoBean", playInfo)
//            if (tvSharedElement != null){
//                bundle.putSerializable("shareVodPicView",(tvSharedElement as RoundAngleImageView))
//            }
            intent.putExtras(bundle)
            if (tvSharedElement == null) {
                context.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(context.getActivity()).toBundle()
                )
            } else {
                context.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                        context.getActivity(),
                        tvSharedElement,
                        "shareVodPic"
                    ).toBundle()
                )
            }


        }
    }

    override fun setContentView(view: View?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.enterTransition = Fade()
        super.setContentView(view)
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
        position = playInfoBean.position
//        shareVodPicView= intent.getSerializableExtra("shareVodPicView",RoundAngleImageView::class.java)
    }


    private fun initView() {
        videoPlayer = viewBinding.danmakuPlayer
        videoPlayer.danmuState = XKeyValue.getBoolean(Key.DANMAKU_STATE, true)

        channelTab = viewBinding.channelTab
        episodeTab = viewBinding.episodeTab
        // 设置播放器回调
        videoPlayer.setActionCallback(this)

        viewBinding.cureentJxText.setOnClickListener(this)
        // 监听数据变化
        observeValueChange()

        // 切换线路逻辑
        channelTab.addOnTabSelectedListener(onChannelTabClick())

        // 选集逻辑
        episodeTab.addOnTabSelectedListener(onEpisodeTabClick())


    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cureent_jx_text -> {
                val parseBeanList = SourceManger.getParseBeanList()
                val indexOf = parseBeanList.indexOf(viewModel.currentParseBean.value)
                XPopup.Builder(this)
                    .isDestroyOnDismiss(true)
                    .asCenterList(
                        "选择首选接口", parseBeanList.map { it.name }.toTypedArray(),
                        null, indexOf
                    ) { position, text ->
                        val parseBean: ParseBean = parseBeanList.get(position)
                        viewModel.currentParseBean.value = parseBean
                        viewModel.preparePlayData()
                    }
                    .show()
            }
        }
    }

    private fun onEpisodeTabClick() = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            updateTabView(tab, true)
            tab.tag = System.currentTimeMillis()
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
            epTabItemClick(tab)
        }

        fun epTabItemClick(tab: TabLayout.Tab) {
            viewModel.currentEpisode.value = tab.position
            viewModel.preparePlayData()
        }
    }

    private fun onChannelTabClick() = object : TabLayout.OnTabSelectedListener {
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
                episodeTab.setScrollPosition(viewModel.currentEpisode.value!!, 0F, true)
            }, 400)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeValueChange() {
        // 详情变化
        viewModel.videoDetailBean.observe(this) {
            viewBinding.titleText.text = it.vod_name
            if (shareVodPicView == null) it.vod_pic.let {
                viewBinding.roundAngleImageView.setImageUrl(it)
            } else {
                viewBinding.roundAngleImageView.setImageDrawable(shareVodPicView!!.drawable)
            }

            viewBinding.descText.text = removeHtmlAndWhitespace(it.vod_content)
            "${strFormat(it.vod_year)}  /  ${strFormat(it.type_name)}  /  ${strFormat(it.vod_director)}".also {
                viewBinding.textView.text = it
            }
            viewBinding.currentSourceText.text = viewModel.spiderSource.value!!.sourceBean.name
        }

        // 线路和剧集变化
        viewModel.channelFlagsAndEpisodes.observe(this) {
            channelTab.removeAllTabs()
            // 初始化 线路列表
            for (channel in it) {
                channelTab.addTab(channelTab.newTab().setText(channel.channelFlag))
            }
        }

        // 当前解析接口
        viewModel.currentParseBean.observe(this) {
            if (it == null) {
                viewBinding.cureentJxText.visibility = View.GONE
            } else {
                viewBinding.cureentJxText.visibility = View.VISIBLE
                viewBinding.cureentJxText.text = getString(
                    R.string.formatted_source_text,
                    it.name,
                    getString(R.string.downward_triangle)
                )
            }
        }

        // 加载提示信息
        viewModel.stateVideoPlayerMsg.observe(this) {
            videoPlayer.setLoadingMsg(it)
        }

        // 播放链接变化
        viewModel.playEntity.observe(this) {
            videoPlayer.setUp(it, position)
            position = 0
        }

        viewModel.danmuFile.observe(this) {
            loadDanmuCallback(it!!)
        }

        viewModel.pageState.observe(this) {
            when (it) {
                PageState.LOADING -> {
                    viewBinding.promptView.showLoading()
                }

                PageState.EMPTY -> {
                    viewBinding.promptView.showEmpty()
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
            eposodeList.add(EpsodeEntity(it.name, ""))
        }
        videoPlayer.episodes = eposodeList

        for (episode in channelEpisodes.episodes) {
            val newTab = episodeTab.newTab()
            newTab.setCustomView(R.layout.item_tab_video_episodes)
            var textView: TextView = newTab.customView!!.findViewById(R.id.tab_video_episodes_tv)
            textView.text = episode.name
            episodeTab.addTab(newTab, false)
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
        saveHistory()
        videoPlayer.release()
        super.onDestroy()
    }

    /**
     * 保存播放历史记录
     */
    private fun saveHistory() {
        if (videoPlayer.currentPosition == 0L || viewModel.videoDetailBean.value == null) {
            return
        }
        val crud = CRUD<PlayInfoBean>(Key.PLAY_History)
        // 当前播放信息
        val item: PlayInfoBean = viewModel.playInfoBean.value!!
        item.position = videoPlayer.currentPosition
        item.preNum = viewModel.currentEpisode.value!!

        // 播放记录
        val playInfoBeans: ArrayList<PlayInfoBean> = crud.list()
        // 查找是否有当前记录 删除
        for (i in playInfoBeans.indices) {
            if ((playInfoBeans[i].videoBean.vod_id == item.videoBean.vod_id) && (playInfoBeans[i].sourceKey == item.sourceKey)) {
                crud.remove(playInfoBeans[i])
                break
            }
        }
        crud.add(item, 0)
    }


    override fun onBackPressed() {
        if (!videoPlayer.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun featureEnabled(): DanmuVideoPlayer.ViewState = DanmuVideoPlayer.ViewState()

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

    /**
     * 弹幕加载事件回调
     */
    override fun loadDanmaku(callBack: (File) -> Unit) {
        XKeyValue.putBoolean(Key.DANMAKU_STATE, true)
        this.loadDanmuCallback = callBack
        if (videoPlayer.danmuIsPrepared()) {
            videoPlayer.danmuShow()
        } else {
            viewModel.loadDanmaku()
        }
    }

    override fun closeDanmuKu() {
        XKeyValue.putBoolean(Key.DANMAKU_STATE, false)
    }

    fun removeHtmlAndWhitespace(input: String): String {
        // 去除HTML标签
        val noHtmlTags = input.replace(Regex("<[^>]*>"), "")
        // 去除多个空白字符（包括空格、制表符、换行符等）替换为单个空格
        val noWhitespace = noHtmlTags.replace(Regex("\\s+"), " ")
        return noWhitespace.trim() // 去除字符串两端的空格
    }

    fun strFormat(str: String?): String {
        if (str == null || StringUtils.isEmpty(str) || str.equals("null")) {
            return "-"
        }
        return str
    }
}
