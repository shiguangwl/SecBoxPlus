package com.xxhoz.secbox.module.player

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.hjq.toast.Toaster
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.App
import com.xxhoz.secbox.base.BaseViewModel
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.PageState
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.StringUtils
import org.json.JSONObject
import java.io.File
import java.net.SocketTimeoutException
import java.util.regex.Matcher
import java.util.regex.Pattern

class DetailPlayerViewModel : BaseViewModel() {

    val playInfoBean: MutableLiveData<PlayInfoBean> = MutableLiveData()

    var spiderSource: MutableLiveData<IBaseSource> = MutableLiveData()

    // 详情信息
    val videoDetailBean = MutableLiveData<VideoDetailBean>()

    // 线路和剧集
    val channelFlagsAndEpisodes = MutableLiveData<List<VideoDetailBean.ChannelEpisodes>>()

    // 当前选择的线路
    val currentChannel = MutableLiveData<Int>()

    // 当前选择的剧集
    val currentEpisode = MutableLiveData<Int>()

    // 当前解析接口对象
    val currentParseBean = MutableLiveData<ParseBean>()

    // 加载提示信息
    val stateVideoPlayerMsg = MutableLiveData<String>()

    // 弹幕文件
    val danmuFile = MutableLiveData<File>()

    // 播放链接
    val playEntity = MutableLiveData<EpsodeEntity>()

    // 页面状态
    val pageState = MutableLiveData<Int>()

    // 搜索job
    var getUrlJob: String = "GET_URL_JOB"

    var getDanmakuJob: String = "GET_DANMAKU_JOB"

    /**
     * 数据初始化
     */
    fun initData(infoBean: PlayInfoBean) {
        pageState.postValue(PageState.LOADING)
        // 默认选择第一个线路
        currentChannel.value = 0
        // 默认选择第一个剧集
        currentEpisode.value = 0
        // 默认选择第一个解析接口
        currentParseBean.postValue(SourceManger.getParseBeanList().get(0))

        Task {
            try {
                val sourceKey = infoBean.sourceKey
                val spiderSource = onIO { SourceManger.getSpiderSource(sourceKey) }
                    ?: throw Exception("获取数据源失败")

                val videoDetail = onIO { spiderSource.videoDetail(listOf(infoBean.videoBean.vod_id)) }
                    ?: throw GlobalException.of("获取详情失败")

                val channelFlagsAndEpisodes = videoDetail.getChannelFlagsAndEpisodes()

                this@DetailPlayerViewModel.playInfoBean.postValue(infoBean)
                this@DetailPlayerViewModel.spiderSource.postValue(spiderSource)
                this@DetailPlayerViewModel.videoDetailBean.postValue(videoDetail)
                this@DetailPlayerViewModel.channelFlagsAndEpisodes.postValue(channelFlagsAndEpisodes)

                pageState.postValue(PageState.NORMAL)
            }catch (e:GlobalException){
                LogUtils.d("获取数据失败: ${e.message}")
                e.printStackTrace()
                pageState.postValue(PageState.EMPTY)
            } catch (e: Exception) {
                LogUtils.d("获取数据失败: ${e.message}")
                e.printStackTrace()
                pageState.postValue(PageState.LOAD_ERROR)
            }
        }
    }


    /**
     * 获取播放链接
     */
    fun getPlayUrl() {
        Task(getUrlJob) {
            onStateVideoPlayerMsg("加载数据中...")

            val currentChannelData: VideoDetailBean.ChannelEpisodes =
                channelFlagsAndEpisodes.value!!.get(currentChannel.value!!)
            val currenSelectEposode: VideoDetailBean.Value =
                currentChannelData.episodes.get(currentEpisode.value!!)

            var playUrl: String = ""
            try {
                var parseBeanList: List<ParseBean> = SourceManger.getParseBeanList()
                onIO2 {
                    playUrl =
                        getPlayUrl(currentChannelData.channelFlag, currenSelectEposode, parseBeanList)
                }
            } catch (e: GlobalException) {
                Toaster.show(e.message)
                return@Task
            } catch (e: Exception) {
                Toaster.show("获取播放链接失败")
                e.printStackTrace()
                return@Task
            } finally {
                if (StringUtils.isEmpty(playUrl)) {
                    onStateVideoPlayerMsg("获取播放链接失败")
                }
            }
            if (StringUtils.isEmpty(playUrl)) {
                return@Task
            }

            LogUtils.d("最终播放链接:  ${playUrl}")

            val epsodeEntity: EpsodeEntity = EpsodeEntity(
                currenSelectEposode.name,
                playUrl
            )
            playEntity.postValue(epsodeEntity)

        }
    }


    fun loadDanmaku(){
        Task(getDanmakuJob) {
            val currentChannelData: VideoDetailBean.ChannelEpisodes =
                channelFlagsAndEpisodes.value!!.get(currentChannel.value!!)
            val currenSelectEposode: VideoDetailBean.Value =
                currentChannelData.episodes.get(currentEpisode.value!!)

            val danmu: File? = onIO { loadDanmu(currenSelectEposode.urlCode) }

            danmu?.run {
                danmuFile.postValue(this)
            }
        }
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
        parseBean: List<ParseBean>,
    ): String {
        val playLinkBean: PlayLinkBean =
            spiderSource.value!!.playInfo(channelFlag, currenSelectEposode.urlCode)
        LogUtils.i("影视播放数据: ${playLinkBean}")

        if (playLinkBean.parse == 1) {
            // 解析播放链接
            onStateVideoPlayerMsg("解析数据中...")

            var parseRsult: String = ""
            for (parseBean1 in parseBean) {
                try {
                    val jsonObject =
                        HttpUtil.get(parseBean1.url + playLinkBean.url, JSONObject::class.java)
                    parseRsult = jsonObject.getString("url")
                    if (parseRsult.isEmpty()) {
                        continue
                    }
                } catch (e: Exception) {
                    LogUtils.d("[${parseBean1.name}] 解析失败")
                    e.printStackTrace()
                    continue
                }

                if (parseRsult.isNotEmpty()) {
                    Toaster.show("[${parseBean1.name}] 当前来源")
                    break
                }
            }

            if (parseRsult.equals("")) {
                throw GlobalException.of("解析失败,请尝试切换播放源")
            }

            playLinkBean.url = parseRsult

        } else if (playLinkBean.parse == 0) {
            // TODO 嗅探播放链接
            onStateVideoPlayerMsg("嗅探资源占时无法播放,请切换源")
        }

        return playLinkBean.url
    }


    @WorkerThread
    private fun loadDanmu(urlCode:String) : File?{
        if (!isVideoPlatformURL(urlCode)) {
            return null
        }
        Toaster.show("后台加载弹幕资源中")
        try {
            val danmuFile: File =
                HttpUtil.downLoad(
                    BaseConfig.DANMAKU_API + urlCode,
                    App.instance.filesDir.absolutePath + "/danmu.xml"
                )
            Toaster.show("弹幕正在装载")
            return danmuFile
        } catch (e: SocketTimeoutException) {
            Toaster.show("加载弹幕超时,请重试")
        } catch (e: Exception) {
            Toaster.show("加载弹幕资源失败")
            e.printStackTrace()
        }
        return null
    }

    fun isVideoPlatformURL(url: String?): Boolean {
        // 定义匹配视频平台网址的正则表达式
        val regex =
            "https?://(www\\.)?(mgtv\\.com|bilibili\\.com|v\\.qq\\.com|v\\.youku\\.com|www\\.iqiyi\\.com)/.*"

        // 使用正则表达式匹配 URL
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(url)
        return matcher.matches()
    }

    private fun onStateVideoPlayerMsg(msg: String) {
        stateVideoPlayerMsg.postValue(msg)
    }


    @PageName
    override fun getPageName() = PageName.DETAIL_PLAYER

}
