package com.xxhoz.secbox.module.sniffer

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupPosition
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.App
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityWebViewBinding
import com.xxhoz.secbox.databinding.ItemSnifferResultBinding
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.module.sniffer.view.CustomPartShadowPopupView
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.UniversalAdapter
import com.xxhoz.secbox.util.getActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.SocketTimeoutException


class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {


    lateinit var mWebView: WebView

    val videoPlayer: DanmuVideoPlayer by lazy { DanmuVideoPlayer(this, null) }

    var isShowBottom = true

    var dataList = mutableListOf<String>()

    var universalAdapter: UniversalAdapter<String>? = null
    override val inflater: (inflater: LayoutInflater) -> ActivityWebViewBinding
        get() = ActivityWebViewBinding::inflate

    private val popupView: BasePopupView by lazy {
        val customPartShadowPopupView = CustomPartShadowPopupView(this) {
            val recyclerView = it
            recyclerView.layoutManager = LinearLayoutManager(this)
            universalAdapter = UniversalAdapter(
                dataList,
                R.layout.item_sniffer_result,
                object : UniversalAdapter.DataViewBind<String> {
                    override fun exec(data: String, view: View) {
                        ItemSnifferResultBinding.bind(view).run {
                            tvUrlText.text = data
                            startPlay.tag = data
                            startPlay.setOnClickListener {
                                startPlay(it.tag as String)
                            }
                            tvUrlText.setOnClickListener {
                                var confirm = XPopup.Builder(getActivity()).asInputConfirm(
                                    "编辑结果", ""
                                ) { text ->
                                    (it as TextView).text = text
                                }
                                confirm.show()
                                Handler(Looper.myLooper()!!).postDelayed({
                                    confirm?.editText?.setText((it as TextView).text)
                                }, 400)

                            }
                        }
                    }
                })
            recyclerView.adapter = universalAdapter
        }
        return@lazy XPopup.Builder(this)
            .atView(viewBinding.bottomBar)
            .isViewMode(true)
            .popupPosition(PopupPosition.Top)
            .asCustom(
                customPartShadowPopupView
            )
    }

    companion object {
        val PHONE_AGENT =
            "UCWEB/2.0 (MIDP-2.0; U; Adr 9.0.0) UCBrowser U2/1.0.0 Gecko/63.0 Firefox/63.0 iPhone/7.1 SearchCraft/2.8.2 baiduboxapp/3.2.5.10 BingWeb/9.1 ALiSearchApp/2.4"
        var USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51"

        var isCustomSite: Boolean = false

        var homeUrl = ""
        fun startActivity(
            context: Context,
            url: String,
            ua: String = USER_AGENT,
            isCustomSite: Boolean = false
        ) {
            this.homeUrl = url
            this.USER_AGENT = ua
            this.isCustomSite = isCustomSite
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
        initView()
    }
    private fun webviewConfig() {
        // WebView 配置
        val webSettings: WebSettings = mWebView.getSettings()
        // 开启 JavaScript
        webSettings.javaScriptEnabled = true
        // 设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true
        // 将图片调整到适合 WebView 的大小
        webSettings.loadWithOverviewMode = true
        // 支持缩放，默认为 true
        webSettings.setSupportZoom(true)
        // 设置内置的缩放控件，若为 false，则该 WebView 不可缩放
        webSettings.builtInZoomControls = true
        // 隐藏原生的缩放控件
        webSettings.displayZoomControls = false
        // 设置UA
        webSettings.setUserAgentString(USER_AGENT)
        // 加快网页加载完成的速度，等页面完成再加载图片
        webSettings.loadsImagesAutomatically = true
        // 本地 DOM 存储（解决加载某些网页出现白板现象）
        webSettings.domStorageEnabled = true
        // 解决 Android 5.0 上 WebView 默认不允许加载 Http 与 Https 混合内容
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        //LOAD_DEFAULT：默认的缓存模式，根据缓存策略决定是否从网络加载数据。
        //LOAD_CACHE_ELSE_NETWORK：优先使用缓存，如果缓存中没有数据则从网络加载。
        //LOAD_NO_CACHE：不使用缓存，直接从网络加载。
        //LOAD_CACHE_ONLY：只使用缓存，不从网络加载。
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    private fun initView() {
        mWebView = viewBinding.webview
        webviewConfig()

        mWebView.webViewClient = webViewClient

        mWebView.webChromeClient = webChromeClient

        val url = intent.getStringExtra("url")!!
        mWebView.loadUrl(url)


        viewBinding.fastPlay.setOnClickListener {
            if (isCustomSite) {
                if (dataList.size > 0){
                    showBottom()
                }else{
                    Toaster.showLong("未找到资源")
                }
                return@setOnClickListener
            }

            btnClick(mWebView.url!!)
        }


        viewBinding.returnImage.setOnClickListener() {
            finish()
        }

        viewBinding.homeBtn.setOnClickListener {
            mWebView.loadUrl(homeUrl)
        }

        viewBinding.refresh.setOnClickListener {
            mWebView.reload()
        }
    }


    private val webViewClient = object : WebViewClient() {
        override fun onLoadResource(view: WebView, url: String) {
            LogUtils.d("Webview加载资源:" + url)
            if (isCustomSite && isM3u8Url(url) || url.contains(".mp4") || url.contains(".flv")) {
                addItem(url)
                if (isCustomSite && isShowBottom){
                    showBottom()
                }
                isShowBottom = false
            }
            super.onLoadResource(view, url)
        }

        /**
         * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
         */
        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
        }

        /**
         * 开始加载网页
         */
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            isShowBottom = true
            removeAll()
            viewBinding.pbBrowserProgress.visibility = View.VISIBLE
        }

        /**
         * 完成加载网页
         */
        override fun onPageFinished(view: WebView, url: String) {
            viewBinding.pbBrowserProgress.visibility = View.GONE
        }
    }

    private fun addItem(url: String) {
        dataList.add(url)
        universalAdapter?.notifyDataSetChanged()
    }


    private fun removeAll() {
        dataList.clear()
        universalAdapter?.notifyDataSetChanged()
    }

    private val webChromeClient = object : WebChromeClient() {
        // 加载进度回调
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            viewBinding.pbBrowserProgress.progress = newProgress
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            // 收到网页标题的回调
            viewBinding.titleText.text = title
        }
    }

    /**
     * 显示嗅探结果
     */
    private fun showBottom() {
        popupView.show()
    }


    private fun startPlay(parseRsult: String) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        videoPlayer.startFullScreen()
        videoPlayer.setUp(EpsodeEntity("", parseRsult), 0)
    }

    private fun btnClick(currentUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            startJsonParser(currentUrl)
        }
    }

    /**
     * 点击解析按钮
     */
    @WorkerThread
    private suspend fun startJsonParser(url: String) {
        LogUtils.d("当前连接是:" + url)
        val parseBeanList = SourceManger.getParseBeanList()
        var parseRsult: String? = null
        for (parseBean in parseBeanList) {
            try {
                val jsonObject = HttpUtil.get(parseBean.url + url, JSONObject::class.java)
                parseRsult = jsonObject.getString("url")
                if (parseRsult.isEmpty()) {
                    continue
                }
                break
            } catch (e: Exception) {
                Toaster.show("[${parseBean.name}] 解析失败,尝试切换解析")
                e.printStackTrace()
                continue
            }
        }

        if (parseRsult == null || parseRsult.isEmpty()) {
            Toaster.showLong("解析资源失败")
            return
        }
        withContext(Dispatchers.Main) {
            startPlay(parseRsult)
        }

        if (isCustomSite) {
            return
        }
        // 加载弹幕
        Toaster.show("后台加载弹幕资源中")
        try {
            val danmuFile: File =
                HttpUtil.downLoad(
                    BaseConfig.DANMAKU_API + url,
                    App.instance.filesDir.absolutePath + "/danmu.xml"
                )
            withContext(Dispatchers.Main) {
                videoPlayer.setDanmuStream(danmuFile)
            }
            Toaster.show("弹幕正在装载")
        } catch (e: SocketTimeoutException) {
            Toaster.show("加载弹幕超时,请重试")
        } catch (e: Exception) {
            Toaster.show("加载弹幕资源失败")
            e.printStackTrace()
        }
    }

    fun isM3u8Url(url: String): Boolean {
        // 使用正则表达式来匹配m3u8链接的模式
        val m3u8Pattern = """^https?://.*\.m3u8(\?.*)?$""".toRegex()
        return m3u8Pattern.matches(url)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (popupView.isShow) {
            return true
        }

        if (videoPlayer.isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            videoPlayer.stopFullScreen()
            videoPlayer.release()
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        mWebView.clearCache(true);
        mWebView.removeAllViews()
        mWebView.destroy()
        videoPlayer.release()
        super.onDestroy()
    }

    override fun getPageName() = PageName.SNIFFER
}
