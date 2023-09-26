package com.xxhoz.secbox.module.player

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hjq.toast.Toaster
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.base.BaseViewModel
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.PageState
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.parserCore.danmuParser.DefaultDanmuImpl
import com.xxhoz.secbox.parserCore.videoJxParser.DefaultVideoParserImpl
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.SocketTimeoutException
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

class DetailPlayerViewModel : BaseViewModel() {

    val danmuSource = DefaultDanmuImpl(BaseConfig.DANMAKU_API)

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

    // Sniffer
    var VideoParser: DefaultVideoParserImpl? = null

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
        currentEpisode.value = infoBean.preNum
        // 默认选择第一个解析接口
        val parseBeanList = SourceManger.getParseBeanList()


        SingleTask(viewModelScope.launch {
            try {
                val sourceKey = infoBean.sourceKey
                val spiderSource =
                    withContext(Dispatchers.IO) { SourceManger.getSpiderSource(sourceKey) }
                        ?: throw Exception("获取数据源失败")

                val videoDetail =
                    withContext(Dispatchers.IO) { spiderSource.videoDetail(listOf(infoBean.videoBean.vod_id)) }
                        ?: throw GlobalException.of("获取详情失败")

                val channelFlagsAndEpisodes = videoDetail.getChannelFlagsAndEpisodes()
                LogUtils.d("播放线路和链接:" + videoDetail.getChannelFlagsAndEpisodes())

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
        })
    }


    /**
     * 获取播放链接
     */
    fun getPlayData() {
        SingleTask(getUrlJob, viewModelScope.launch {
            onStateVideoPlayerMsg("资源加载中...")
            val currentChannelData = channelFlagsAndEpisodes.value!!.get(currentChannel.value!!)
            val episodes = currentChannelData.episodes
            val currenSelectEposode: VideoDetailBean.Value =
                episodes.get(min(currentEpisode.value!!, episodes.size))

            val parseBeanList = SourceManger.getParseBeanList()
            // 将currentParseBean移到第一项
            if (currentParseBean.value != null) {
                val index = parseBeanList.indexOf(currentParseBean.value!!)
                if (index > 0) {
                    parseBeanList.removeAt(index)
                    parseBeanList.add(0, currentParseBean.value!!)
                }
            }

            withContext(Dispatchers.IO) {
                getPlayUrl(
                    currentChannelData.channelFlag,
                    currenSelectEposode.urlCode,
                    parseBeanList,
                    object : DefaultVideoParserImpl.Callback {
                        override fun success(parseBean: ParseBean?, res: String?) {
                            parseBean?.let {
                                LogUtils.d("[${it.name}] 解析成功")
                            }
                            LogUtils.d("最终播放链接:  ${res}")
                            val epsodeEntity: EpsodeEntity = EpsodeEntity(
                                currenSelectEposode.name, res
                            )
                            onStateVideoPlayerMsg("资源准备中...")
                            playEntity.postValue(epsodeEntity)
                            currentParseBean.postValue(parseBean)
                        }

                        override fun failed(errorInfo: String?) {
                            LogUtils.d("解析失败: ${errorInfo}")
                            onStateVideoPlayerMsg("获取播放链接失败")
                        }
                    })
            }
        })
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
        callback: DefaultVideoParserImpl.Callback
    ) {
        val playLinkBean: PlayLinkBean? = spiderSource.value!!.playInfo(channelFlag, urlCode)
        if (playLinkBean == null) {
            callback.failed("获取播放链接失败")
            return
        }
        LogUtils.i("影视播放数据: ${playLinkBean}")
        onStateVideoPlayerMsg("资源加载中...")
        if (playLinkBean.parse == 1) {
            // 解析嗅探播放链接
            VideoParser?.cancel()
            VideoParser = DefaultVideoParserImpl()
            VideoParser!!.JX(
                playLinkBean.url,
                parseBeanList,
                object : DefaultVideoParserImpl.Callback {
                    override fun success(parseBean: ParseBean?, res: String?) {
                        callback.success(parseBean,res)
                    }

                    override fun failed(errorInfo: String?) {
                        callback.failed(errorInfo)
                    }
                })
        } else if (playLinkBean.parse == 0) {
            // 直接播放
            callback.success(null, playLinkBean.url)
        }
    }


    /**
     * 装载当前剧集弹幕
     */
    fun loadDanmaku() {
        SingleTask(getDanmakuJob, viewModelScope.launch {
            // 当前线路的所有剧集
            val currentChannelData = channelFlagsAndEpisodes.value!!.get(currentChannel.value!!)
            // 当前选择的剧集的URL
            val urlCode = currentChannelData.episodes.get(currentEpisode.value!!).urlCode
            // 加载弹幕数据
            loadDanmukuData(urlCode)
        })
    }

    private suspend fun CoroutineScope.loadDanmukuData(urlCode: String) {
        val danmu: File? = withContext(Dispatchers.IO) {
            if (!isVideoPlatformURL(urlCode)) {
                return@withContext null
            }
            Toaster.show("后台加载弹幕资源中")
            var errorInfo = ""
            for (i in 0..3) {
                try {
                    return@withContext danmuSource.getDanmaku(urlCode)
                } catch (e: SocketTimeoutException) {
                    LogUtils.d("弹幕加载超时,尝试从试")
                    errorInfo = "加载弹幕超时,请重试"
                } catch (e: Exception) {
                    LogUtils.d("弹幕加载失败,尝试从试")
                    errorInfo = "加载弹幕资源失败"
                    e.printStackTrace()
                }
            }
            if (StringUtils.isNotEmpty(errorInfo)) {
                Toaster.show(errorInfo)
            }
            return@withContext null
        }
        if (danmu != null && isActive) {
            Toaster.show("弹幕正在装载")
            danmuFile.postValue(danmu!!)
        }
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
        VideoParser?.cancel()
        super.onCleared()
    }


    @PageName
    override fun getPageName() = PageName.DETAIL_PLAYER

}
