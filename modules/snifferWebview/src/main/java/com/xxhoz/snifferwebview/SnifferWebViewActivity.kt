package com.xxhoz.secboxbrowser


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupPosition
import com.xxhoz.danmuplayer.DanmuVideoPlayer
import com.xxhoz.danmuplayer.EpsodeEntity
import com.xxhoz.danmuplayer.SimpleVideoCallback
import com.xxhoz.secboxbrowser.base.TaskManger
import com.xxhoz.secboxbrowser.popview.SnifferListPopupView
import com.xxhoz.snifferwebview.R
import kotlinx.coroutines.CoroutineScope
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder


class SnifferWebViewActivity : AppCompatActivity(), View.OnClickListener, TaskManger {

    private val TAG: String = "MainActivity"

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var textUrl: EditText
    private lateinit var webIcon: ImageView
    private lateinit var goBack: ImageView
    private lateinit var goForward: ImageView
    private lateinit var navSet: ImageView
    private lateinit var goHome: ImageView
    private lateinit var btnStart: ImageView
    private lateinit var mediaNum: TextView

    private var exitTime: Long = 0

    private lateinit var mContext: Context
    private lateinit var manager: InputMethodManager

    private val HTTP = "http://"
    private val HTTPS = "https://"
    private var PRESS_BACK_EXIT_GAP = 2000

    private var isAutoShow = true

    private var LatestUrl = ""
    override fun getScope(): CoroutineScope = lifecycleScope

    companion object {
        val PHONE_AGENT =
            "UCWEB/2.0 (MIDP-2.0; U; Adr 9.0.0) UCBrowser U2/1.0.0 Gecko/63.0 Firefox/63.0 iPhone/7.1 SearchCraft/2.8.2 baiduboxapp/3.2.5.10 BingWeb/9.1 ALiSearchApp/2.4"
        var PC_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51"


        private lateinit var homeUrl: String

        private lateinit var USER_AGENT: String

        private var isCustomSite: Boolean = false

        fun startActivity(
            context: Context,
            url: String,
            ua: String = PHONE_AGENT,
            isCustomSite: Boolean = false
        ) {
            this.homeUrl = url
            this.USER_AGENT = ua
            this.isCustomSite = isCustomSite
            val intent = Intent(context, SnifferWebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }

    }


    private val popupData: SnifferListPopupView by lazy {
        val snifferListPopupView = SnifferListPopupView(this)
        snifferListPopupView.callback = {
            startPlay(it)
        }
        snifferListPopupView
    }


    private val popupView: BasePopupView by lazy {
        XPopup.Builder(this)
            .atView(findViewById(R.id.bottomBar))
            .isViewMode(true)
            .popupPosition(PopupPosition.Top)
            .asCustom(popupData)
    }

    /**
     * 播放器初始化逻辑
     */
    val videoPlayer: DanmuVideoPlayer by lazy {
        val simpleVideoCallback = object : SimpleVideoCallback() {
            override fun featureEnabled(): DanmuVideoPlayer.ViewState =
                DanmuVideoPlayer.ViewState(false, false, false)
        }

        DanmuVideoPlayer(this, null, simpleVideoCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sniffer_web_view_activity)
        window.statusBarColor = Color.BLACK
        // 防止底部按钮上移
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        mContext = this
        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        // 绑定控件
        initView()
        textUrl.setText("加载中...")
        // 初始化 WebView
        initWeb()

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            quitPlay()
        }
    }


    override fun onDestroy() {
        clearTask()
        super.onDestroy()
    }

    /**
     * 绑定控件
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        webView = findViewById<WebView>(R.id.webView)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        textUrl = findViewById<EditText>(R.id.textUrl)
        webIcon = findViewById<ImageView>(R.id.webIcon)
        btnStart = findViewById<ImageView>(R.id.btnStart)
        goBack = findViewById<ImageView>(R.id.goBack)
        goForward = findViewById<ImageView>(R.id.goForward)
        navSet = findViewById<ImageView>(R.id.navSet)
        goHome = findViewById<ImageView>(R.id.goHome)
        mediaNum = findViewById<TextView>(R.id.mediaNum)


        // 绑定按钮点击事件
        btnStart.setOnClickListener(this)
        goBack.setOnClickListener(this)
        goForward.setOnClickListener(this)
        navSet.setOnClickListener(this)
        goHome.setOnClickListener(this)
        mediaNum.setOnClickListener(this)

        webView.setOnTouchListener { v, event ->
            // 当触摸事件发生时，让 EditText 失去焦点
            if (event.action == MotionEvent.ACTION_DOWN) {
                textUrl.clearFocus()
                if (manager.isActive) {
                    manager.hideSoftInputFromWindow(textUrl.applicationWindowToken, 0)
                }
            }
            return@setOnTouchListener false
        }
        // 地址输入栏获取与失去焦点处理
        textUrl.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                if (webView.url.equals(homeUrl)) {
                    textUrl.setText("")
                    return@OnFocusChangeListener
                }
                // 显示当前网址链接 TODO:搜索页面显示搜索词
                textUrl.setText(webView.url)
                // 光标置于末尾
                textUrl.setSelection(textUrl.text.length)
                // 显示因特网图标
                webIcon.setImageResource(R.drawable.internet)
                // 显示跳转按钮
                btnStart.setImageResource(R.drawable.go)
            } else {
                // 显示网站名
                textUrl.setText(webView.title)
                // 显示网站图标
                webIcon.setImageBitmap(webView.favicon)
                // 显示刷新按钮
                btnStart.setImageResource(R.drawable.refresh)
            }
        }

        // 监听键盘回车搜索
        textUrl.setOnKeyListener { view, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                // 执行搜索
                btnStart.callOnClick()
                textUrl.clearFocus()
            }
            false
        }
    }


    /**
     * 初始化 web
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWeb() {
        // 重写 WebViewClient
        webView.webViewClient = webViewClient()
        // 重写 WebChromeClient
        webView.webChromeClient = webChromeClient()
        val settings = webView.settings
        // 启用 js 功能
        settings.javaScriptEnabled = true
        // 设置浏览器 UserAgent
        settings.userAgentString = USER_AGENT

        // 将图片调整到适合 WebView 的大小
        settings.useWideViewPort = true
        // 缩放至屏幕的大小
        settings.loadWithOverviewMode = true

        // 支持缩放，默认为true。是下面那个的前提。
        settings.setSupportZoom(true)
        // 设置内置的缩放控件。若为false，则该 WebView 不可缩放
        settings.builtInZoomControls = true
        // 隐藏原生的缩放控件
        settings.displayZoomControls = false

        // 缓存
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        // 设置可以访问文件
        settings.allowFileAccess = true
        // 支持通过JS打开新窗口
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 支持自动加载图片
        settings.loadsImagesAutomatically = true
        // 设置默认编码格式
        settings.defaultTextEncodingName = "utf-8"
        // 本地存储
        settings.domStorageEnabled = true
        settings.pluginState = WebSettings.PluginState.ON

        // 资源混合模式
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // 加载首页
        webView.loadUrl(homeUrl)
    }

    private fun webChromeClient() = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            // 加载进度变动，刷新进度条
            progressBar.progress = newProgress
            if (newProgress > 0) {
                if (newProgress == 100) {
                    progressBar.visibility = View.INVISIBLE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            super.onReceivedIcon(view, icon)

            // 改变图标
            webIcon.setImageBitmap(icon)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)

            // 改变标题
            setTitle(title)
            // 显示页面标题
            textUrl.setText(title)
        }

    }

    private fun webViewClient() = object : WebViewClient() {
        /**
         * 加载资源
         */
        @SuppressLint("SetTextI18n")
        override fun onLoadResource(view: WebView, url: String) {
            if (isPlayUrlAble(url)) {
                popupData.addItem(url)
                mediaNum.text = (mediaNum.text.toString().toInt() + 1).toString()
                if (isAutoShow) {
                    popupView.show()
                    isAutoShow = false
                }
            }
            super.onLoadResource(view, url)
        }

        /**
         * 网页跳转
         */
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url: String = request.url.toString()
            // 设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
            if (url.length == 0) {
                // 返回true自己处理，返回false不处理
                return true
            }

            // 正常的内容，打开
            if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
                onLoadNewPage()
                view.loadUrl(url)
                return true
            }

            // 调用第三方应用，防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
//            Toast.makeText(mContext, "尝试调起应用,已拦截" + url, Toast.LENGTH_SHORT).show()
            return true
//            return try {
//                // TODO:弹窗提示用户，允许后再调用
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                startActivity(intent)
//                true
//            } catch (e: Exception) {
//                true
//            }
        }


        /**
         * 网页开始加载
         */
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            // 网页开始加载，显示进度条
            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE

            // 更新状态文字
            textUrl.setText("加载中...")

            // 切换默认网页图标
            webIcon.setImageResource(R.drawable.internet)
        }

        /**
         * 网页加载完毕
         */
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            // 网页加载完毕，隐藏进度条
            progressBar.visibility = View.INVISIBLE

            // 改变标题
            title = webView.title
            // 显示页面标题
            if (!textUrl.isFocused) {
                textUrl.setText(webView.title)
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if (error != null) {
                var error_info = ""
                if (error.errorCode == ERROR_HOST_LOOKUP) {
                    error_info = "网络环境异常"
                }

                if (!error_info.isEmpty()) {
                    Toast.makeText(mContext, error_info, Toast.LENGTH_SHORT).show()
                    view!!.loadDataWithBaseURL(
                        null,
                        "<html><body><h1>" + error_info + "</h1></body></html>",
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            }
        }
    }


    private fun startPlay(it: String) {
        LatestUrl = webView.url.toString()
        webView.loadUrl("about:blank")
        popupView.dismiss()
        // 处理播放逻辑
        videoPlayer.setUp(EpsodeEntity("", it), 0)
        videoPlayer.startFullScreen()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    private fun quitPlay() {
        webView.loadUrl(LatestUrl)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        videoPlayer.stopFullScreen()
        videoPlayer.release()
    }

    /**
     * 返回按钮处理
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (popupView.isShow) {
            return
        }

        if (videoPlayer.isFullScreen) {
            quitPlay()
            return
        }

        // 能够返回则返回上一页
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
//            if (System.currentTimeMillis() - exitTime > PRESS_BACK_EXIT_GAP) {
//                // 连点两次退出程序
//                Toast.makeText(
//                    mContext, "再按一次退出程序", Toast.LENGTH_SHORT
//                ).show()
//                exitTime = System.currentTimeMillis()
//            } else {
//
//            }

            super.onBackPressed()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnStart -> if (textUrl.hasFocus()) {
                // 隐藏软键盘
                if (manager.isActive) {
                    manager.hideSoftInputFromWindow(textUrl.applicationWindowToken, 0)
                }
                // 地址栏有焦点，是跳转
                var input = textUrl.text.toString()
                // URL 解码
                input = URLDecoder.decode(input, "utf-8")
                if (!isHttpUrl(input)) {
                    // 不是网址，加载搜索引擎处理
                    // URL 编码
                    input = URLEncoder.encode(input, "utf-8")
                    // 如果不为网址则搜索
                    input = getString(R.string.searchEngine) + input
                }

                webView.loadUrl(input)
                // 取消掉地址栏的焦点
                textUrl.clearFocus()
            } else {
                // 地址栏没焦点，是刷新
                webView.reload()
                onLoadNewPage()
            }

            R.id.goBack -> webView.goBack()
            R.id.goForward -> webView.goForward()
//            R.id.navSet -> Toast.makeText(mContext, "功能开发中", Toast.LENGTH_SHORT).show()
            R.id.mediaNum -> {
                if (popupData.getItemCount() > 0) {
                    popupView.show()
                } else {
                    Toast.makeText(mContext, "未找到资源", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.goHome -> webView.loadUrl(homeUrl)
            else -> {}
        }
    }

    /**
     * 当加载新的连接时
     */
    private fun onLoadNewPage() {
        isAutoShow = true
        popupData.clearItem()
        popupView.dismiss()
        mediaNum.text = "0"
    }

    override fun onPause() {
        super.onPause()
        try {
            webView.javaClass.getMethod("onPause").invoke(webView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            webView.javaClass.getMethod("onResume").invoke(webView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断字符串是否为URL（https://blog.csdn.net/bronna/article/details/77529145）
     *
     * @param urls 要勘定的字符串
     * @return true:是URL、false:不是URL
     */
    fun isHttpUrl(url: String): Boolean {
        try {
            val uri = URI(url)
            if (uri.scheme != null && (uri.scheme.equals(
                    "http",
                    ignoreCase = true
                ) || uri.scheme.equals("https", ignoreCase = true))
            ) {
                if (uri.host != null && uri.host.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}\$|^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"))) {
                    return true
                }
            }
        } catch (e: Exception) {
            // URL parsing error
        }
        return false
    }

    /**
     * 是否媒体资源
     */
    fun isPlayUrlAble(url: String): Boolean {
        // 使用正则表达式来匹配m3u8链接的模式
        val m3u8Pattern = """^https?://.*\.(m3u8|mp4|flv|avi|wmv|mov|mkv)(\?.*)?$""".toRegex()
        return m3u8Pattern.matches(url)
    }


    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return 当前版本名称
     */
    private fun getVerName(context: Context): String {
        var verName = "unKnow"
        try {
            verName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return verName
    }

    /**
     * 判断是否最新版本
     * @param currentVersion 当前版本
     * @param latestVersion 最新版本
     * @return 前版本大于或等于最新版本时返回true，当前版本小于最新版本时返回false
     */
    fun isLatestVersion(currentVersion: String, latestVersion: String): Boolean {
        Log.i("当前版本", currentVersion)
        Log.i("最新版本", latestVersion)
        val currentParts = currentVersion.split(".")
        val latestParts = latestVersion.split(".")

        val maxLength = maxOf(currentParts.size, latestParts.size)

        for (i in 0 until maxLength) {
            val currentPart = if (i < currentParts.size) currentParts[i] else "0"
            val latestPart = if (i < latestParts.size) latestParts[i] else "0"

            val currentInt = currentPart.toIntOrNull() ?: 0
            val latestInt = latestPart.toIntOrNull() ?: 0

            if (currentInt < latestInt) {
                return false
            } else if (currentInt > latestInt) {
                return true
            }
        }

        return true // Both versions are equal
    }

}
