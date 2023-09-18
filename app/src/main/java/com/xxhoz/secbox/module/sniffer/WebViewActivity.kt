package com.xxhoz.secbox.module.sniffer

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.hjq.toast.Toaster
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.EpsodeEntity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityWebViewBinding
import com.xxhoz.secbox.module.player.video.DanmuVideoPlayer
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.StandardPlatformLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {
//    val USER_AGENT =
//        "UCWEB/2.0 (MIDP-2.0; U; Adr 9.0.0) UCBrowser U2/1.0.0 Gecko/63.0 Firefox/63.0 iPhone/7.1 SearchCraft/2.8.2 baiduboxapp/3.2.5.10 BingWeb/9.1 ALiSearchApp/2.4"
    val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51"

    lateinit var mWebView: WebView

    val videoPlayer: DanmuVideoPlayer by lazy { DanmuVideoPlayer(this, null) }

    companion object {
        fun startActivity(context: Context, url: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }

    override val inflater: (inflater: LayoutInflater) -> ActivityWebViewBinding
        get() = ActivityWebViewBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        initView()
    }

    private fun initView() {
        mWebView = viewBinding.webview
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


        // 加载指定网站
        mWebView.webViewClient = object : WebViewClient() {


            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if (url.startsWith("bilibili://")) {
                    // 忽略处理bilibili的URL scheme
                    true
                } else {
                    // 处理其他URL
                    view.loadUrl(url)
                    false
                }
            }

            /**
             * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
             */
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            }

            /**
             * 开始加载网页
             */
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                viewBinding.pbBrowserProgress.visibility = View.VISIBLE
            }

            /**
             * 完成加载网页
             */
            override fun onPageFinished(view: WebView, url: String) {
                viewBinding.pbBrowserProgress.visibility = View.GONE
            }
        }

        mWebView.webChromeClient = object : WebChromeClient() {
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

        val url = intent.getStringExtra("url")!!
        mWebView.loadUrl(url)


        viewBinding.fastPlay.setOnClickListener {
            var currentUrl = mWebView.url!!

//            // 优酷链接处理
//            if (currentUrl.contains("youku.com")) {
//                mWebView.evaluateJavascript("__INITIAL_DATA__.videoMap.videoId") { value: String ->
//
//                    if ("\"null\"".equals(value)){
//                        return@evaluateJavascript
//                    }
//                    val tag = "https://v.youku.com/v_show/id_+$value.html"
//                    butnClick(tag)
//                }
//                return@setOnClickListener
//            }

            btnClick(currentUrl)
        }


        viewBinding.returnImage.setOnClickListener(){
            finish()
        }

    }

    private fun btnClick(currentUrl: String) {
        var currentUrl1 = currentUrl
        lifecycleScope.launch(Dispatchers.IO) {
            // 标准化URL
//            currentUrl1 = formatUrl(currentUrl1!!)
            startSniffer(currentUrl1)
        }
    }

    @WorkerThread
    private suspend fun formatUrl(currentUrl: String): String {
        var resultUrl = ""
        if (currentUrl.contains("bilibili.com") || currentUrl.contains("tv/ep")) {
            var JsonObject = try {
                StandardPlatformLink.biUrlFormat(currentUrl, true).getAsJsonObject()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return ""
            }

            val id = JsonObject!!["id"].asString
            resultUrl = "https://www.bilibili.com/bangumi/play/ep$id"
        }


        if (currentUrl.contains("v.qq.com")) {
            try {
                resultUrl = StandardPlatformLink.txUrlFormat(currentUrl)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        return resultUrl
    }


    /**
     * 点击解析按钮
     */
    @WorkerThread
    private suspend fun startSniffer(url: String) {
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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            videoPlayer.startFullScreen()
            videoPlayer.setUp(EpsodeEntity("", parseRsult),0)
        }

//        // 加载弹幕
//        Toaster.show("后台加载弹幕资源中")
//        try {
//            val danmuFile: File =
//                HttpUtil.downLoad(
//                    BaseConfig.DANMAKU_API + url,
//                    App.instance.filesDir.absolutePath + "/danmu.xml"
//                )
//            withContext(Dispatchers.Main) {
//                videoPlayer.setDanmuStream(danmuFile)
//            }
//            Toaster.show("弹幕正在装载")
//        } catch (e: SocketTimeoutException) {
//            Toaster.show("加载弹幕超时,请重试")
//        } catch (e: Exception) {
//            Toaster.show("加载弹幕资源失败")
//            e.printStackTrace()
//        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
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
        videoPlayer.release()
        super.onDestroy()
    }

    override fun getPageName() = PageName.SNIFFER
}