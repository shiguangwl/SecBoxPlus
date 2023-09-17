package com.xxhoz.secbox.module.player

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.hjq.toast.Toaster
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.App
import com.xxhoz.secbox.base.BaseActivity
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
import com.xxhoz.secbox.parserCore.engine.SnifferEngine
import com.xxhoz.secbox.util.GlobalActivityManager
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
    val currentParseBean = MutableLiveData<ParseBean?>()

    // 加载提示信息
    val stateVideoPlayerMsg = MutableLiveData<String>()

    // 弹幕文件
    val danmuFile = MutableLiveData<File>()

    // 播放链接
    val playEntity = MutableLiveData<EpsodeEntity>()

    // 页面状态
    val pageState = MutableLiveData<Int>()

    // SnifferJobs
    val snifferJobs: ArrayList<SnifferEngine.SnifferJob> = ArrayList<SnifferEngine.SnifferJob>()
    var snifferJobsCount: Int = 0

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
        val parseBeanList = SourceManger.getParseBeanList()
        if (parseBeanList.size > 0){
            currentParseBean.postValue(parseBeanList.get(0))
        }

        Task {
            try {
                val sourceKey = infoBean.sourceKey
                val spiderSource = onIO { SourceManger.getSpiderSource(sourceKey) }
                    ?: throw Exception("获取数据源失败")

                val videoDetail =
                    onIO { spiderSource.videoDetail(listOf(infoBean.videoBean.vod_id)) }
                        ?: throw GlobalException.of("获取详情失败")

                val channelFlagsAndEpisodes = videoDetail.getChannelFlagsAndEpisodes()

                this@DetailPlayerViewModel.playInfoBean.postValue(infoBean)
                this@DetailPlayerViewModel.spiderSource.postValue(spiderSource)
                this@DetailPlayerViewModel.videoDetailBean.postValue(videoDetail)
                this@DetailPlayerViewModel.channelFlagsAndEpisodes.postValue(channelFlagsAndEpisodes)

                pageState.postValue(PageState.NORMAL)
            } catch (e: GlobalException) {
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

            val parseBeanList: List<ParseBean> = SourceManger.getParseBeanList()
            onIO2 {
                getPlayUrl(
                    currentChannelData.channelFlag,
                    currenSelectEposode.urlCode,
                    parseBeanList,
                    object : SnifferEngine.Callback {
                        override fun success(parseBean: ParseBean?, res: String?) {
                            clearSnifferJobs()
                            LogUtils.d("最终播放链接:  ${res}")
                            val epsodeEntity: EpsodeEntity = EpsodeEntity(
                                currenSelectEposode.name,
                                res
                            )
                            playEntity.postValue(epsodeEntity)
                            currentParseBean.postValue(parseBean)
                        }

                        override fun failed(parseBean: ParseBean?, errorInfo: String?) {
                            LogUtils.d("[${parseBean?.type}]解析失败: ${errorInfo}")
                            snifferJobsCount -= 1
                            if (snifferJobsCount <= 0) {
                                onStateVideoPlayerMsg("获取播放链接失败")
                                clearSnifferJobs()
                            }
                        }

                        override fun timeOut(parseBean: ParseBean?) {
                            LogUtils.d("[${parseBean?.type}]解析超时")
                            snifferJobsCount -= 1
                            if (snifferJobsCount <= 0) {
                                onStateVideoPlayerMsg("获取播放链接失败")
                                clearSnifferJobs()
                            }
                        }
                    }
                )
            }
        }
    }

    fun loadDanmaku() {
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

    /** 解析接口
     * @param channelFlag 当前线路数
     * @param urlCode 当前选中的剧集
     * @param parseBeanList 当前解析接口对象
     * @param callback 回调
     */
    @WorkerThread
    private fun getPlayUrl(
        channelFlag: String,
        urlCode: String,
        parseBeanList: List<ParseBean>,
        callback: SnifferEngine.Callback
    ) {
        val playLinkBean: PlayLinkBean =
            spiderSource.value!!.playInfo(channelFlag, urlCode)
        LogUtils.i("影视播放数据: ${playLinkBean}")
        onStateVideoPlayerMsg("资源加载中...")
        if (playLinkBean.parse == 1) {
            // 解析嗅探播放链接
            snifferJobsCount = parseBeanList.count { it.type == 1 || it.type == 0 }
            for (parseBean in parseBeanList) {
                if (parseBean.type == 1) {
                    var parseRsult: String = ""
                    try {
                        val jsonObject =
                            HttpUtil.get(parseBean.url + playLinkBean.url, JSONObject::class.java)
                        parseRsult = jsonObject.getString("url")
                    } catch (e: Exception) {
                        LogUtils.d("[${parseBean.name}] 解析失败")
                        e.printStackTrace()
                    }

                    if (StringUtils.isNotEmpty(parseRsult)) {
                        callback.success(parseBean, parseRsult)
                        break
                    } else {
                        callback.failed(parseBean,"解析失败")
                    }
                } else if (parseBean.type == 0) {
                    val snifferJob: SnifferEngine.SnifferJob = SnifferEngine.JX(
                        GlobalActivityManager.getTopActivity() as BaseActivity<*>,
                        parseBean,
                        playLinkBean.url,
                        15000,
                        callback
                    )
                    snifferJobs.add(snifferJob)
                }
            }
        } else if (playLinkBean.parse == 0) {
            // 直接播放
            callback.success(null,playLinkBean.url)
        }
    }


    @WorkerThread
    private fun loadDanmu(urlCode: String): File? {
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

    override fun onCleared() {
        clearSnifferJobs()
        super.onCleared()
    }

    fun clearSnifferJobs() {
        snifferJobs.forEach {
            it.cancel()
        }
        snifferJobs.clear()
    }

    @PageName
    override fun getPageName() = PageName.DETAIL_PLAYER

}
