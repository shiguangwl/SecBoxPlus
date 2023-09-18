package com.xxhoz.secbox.parserCore.engine

import android.net.http.SslError
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.xxhoz.constant.BaseConfig
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.util.LogUtils
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


/**
 * <XiuTanParser>
 * 通用嗅探引擎
 *
 * @author DengNanYu
 * @version 1.0_2022/11/26
 * @date 2022/11/26 15:21
</XiuTanParser> */
object SnifferEngine {
    private const val TAG = "XiuTanEngine"

    /**
     * 并发资源嗅探
     *
     * @param activity
     * @param url
     * @param timeout
     * @param callback
     */
    fun JX(
        activity: BaseActivity<*>,
        parseBean: ParseBean,
        url: String,
        timeout: Long,
        callback: Callback
    ): SnifferJob {
        val mWebView: AtomicReference<WebView> = AtomicReference()
        // 启动协程
        activity.runOnUiThread() {
            mWebView.set(WebView(activity))

            configWebViewSys(mWebView.get())
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mWebView.get().visibility = View.GONE
            activity.addContentView(mWebView.get(),layoutParams)

            // 超时回调
            val timer = Timer()
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        mWebView.get().removeAllViews()
                        mWebView.get().destroy()
                         //超时回调
                        callback.failed(parseBean,"解析超时")
                    }
                }
            }
            timer.schedule(timerTask, timeout)


            // 加载是否发生异常
            val isError = AtomicBoolean(false)
            val errorInfo = AtomicReference<String>()
            mWebView.get().webViewClient = object : WebViewClient() {
                // onPageFinished回调并不能代表网页加载成功了，是无法判断的，因为即使失败了也会调用onPageFinished，而且它和onError的调用顺序不固定，所以失败的判断条件有时候会出错
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    isError.set(true)
                    errorInfo.set("onReceivedError:" + error.errorCode + "    " + error.description + "    URL:" + request.url)
                    Log.e(TAG, errorInfo.get())
                }

                override fun onLoadResource(view: WebView, url: String) {
                    LogUtils.d("Webview加载资源:" + url)
                    if (isM3u8Url(url) || url.contains(".mp4") || url.contains(".flv")) {
                        if (url.contains(".m3u8") )
                        callback.success(parseBean, url)
                        timer.cancel()
                        mWebView.get().removeAllViews()
                        mWebView.get().destroy()
                    }
                    super.onLoadResource(view, url)
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler,
                    error: SslError?
                ) {
                    // 忽略 SSL 错误，继续加载页面
                    handler.proceed()
                }

            }

            mWebView.get().setWebChromeClient(object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress == 100) {
                        // 取消超时回调
                        timer.cancel()
                        if (isError.get()) {
                            // 异常错误回调
//                            callback.failed(parseBean,errorInfo.get())
                            return
                        }
//                        mWebView.get().removeAllViews()
//                        mWebView.get().destroy()
                    }
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    return false
                }

                override fun onJsAlert(
                    view: WebView,
                    url: String,
                    message: String,
                    result: JsResult
                ): Boolean {
                    return true
                }

                override fun onJsConfirm(
                    view: WebView,
                    url: String,
                    message: String,
                    result: JsResult
                ): Boolean {
                    return true
                }

                override fun onJsPrompt(
                    view: WebView,
                    url: String,
                    message: String,
                    defaultValue: String,
                    result: JsPromptResult
                ): Boolean {
                    return true
                }
            })
            mWebView.get().loadUrl(parseBean.url + url)
//            mWebView.get().loadUrl("https://www.ajeee.com/play/211517-3-1.html")

        }

        while (true){
            if (mWebView.get() == null){
                Thread.sleep(50)
                continue
            }
            return object : SnifferJob {
                override fun cancel() {
                    activity.runOnUiThread {
                        val webView = mWebView!!.get()
                        if (webView != null) {
                            webView.removeAllViews()
                            webView.destroy()
                        }
                    }
                }
            }
        }
    }

    fun isM3u8Url(url: String): Boolean {
        // 使用正则表达式来匹配m3u8链接的模式
        val m3u8Pattern = """^https?://.*\.m3u8(\?.*)?$""".toRegex()
        return m3u8Pattern.matches(url)
    }


    open fun configWebViewSys(webView: WebView) {
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false
        webView.clearFocus()
        webView.overScrollMode = View.OVER_SCROLL_ALWAYS

        /* 添加webView配置 */
        val settings = webView.settings
        settings.setNeedInitialFocus(false)
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccessFromFileURLs = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        if (BaseConfig.DEBUG) {
            settings.blockNetworkImage = false
        } else {
            settings.blockNetworkImage = true
        }
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(false)
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.setSupportZoom(false)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        /* 添加webView配置 */
        //设置编码
        settings.defaultTextEncodingName = "utf-8"
        settings.setUserAgentString(webView.settings.userAgentString)
//         settings.setUserAgentString(ANDROID_UA);
//        webView.setBackgroundColor(Color.BLACK)
    }

    // 访问回调
    interface Callback {
        fun success(parseBean: ParseBean?, res: String?)
        fun failed(parseBean: ParseBean?,errorInfo: String?)
    }

    interface SnifferJob {
        fun cancel()
    }
}
