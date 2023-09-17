package com.xxhoz.secbox.parserCore.engine

import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
            mWebView!!.set(WebView(activity))
            //mWebView = activity.findViewById(R.id.test_webview);
            // 超时回调
            val timer = Timer()
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        mWebView.get().removeAllViews()
                        mWebView.get().destroy()
                        // 超时回调
                        callback.timeOut(parseBean)
                    }
                }
            }
            timer.schedule(timerTask, timeout)
            mWebView.get().settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mWebView.get().settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            mWebView.get().settings.blockNetworkImage = false

            // WebView 配置
            val webSettings = mWebView.get().settings
            // js 相关
            webSettings.javaScriptEnabled = true // 支持 js。如果碰到后台无法释放 js 导致耗电，应在 onStop 和 onResume 里分别设成 false 和 true
            // 设置自适应屏幕，两者合用
            webSettings.useWideViewPort = true // 将图片调整到适合 WebView 的大小
            webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
            // 缩放操作
            webSettings.setSupportZoom(false) // 支持缩放，默认为 true
            webSettings.builtInZoomControls = false // 设置内置的缩放控件，若为 false，则该 WebView 不可缩放
            webSettings.displayZoomControls = false // 隐藏原生的缩放控件
            webSettings.domStorageEnabled = true
            webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36 Edg/106.0.1370.42")


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

//                override fun onReceivedSslError(
//                    view: WebView,
//                    handler: SslErrorHandler,
//                    error: SslError
//                ) {
////                super.onReceivedSslError(view, handler, error);
//                    isError.set(true)
//                    errorInfo.set("onReceivedSslError:" + error.url)
//                    Log.e(TAG, errorInfo.get())
//                }

                override fun onLoadResource(view: WebView, url: String) {
                    LogUtils.d("Webview加载资源:" + url)
                    if (url.contains(".m3u8") || url.contains(".mp4") || url.contains(".flv")) {
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
                            callback.failed(parseBean,errorInfo.get())
                            return
                        }
                        mWebView.get().removeAllViews()
                        mWebView.get().destroy()
                    }
                }
            })
            mWebView.get().loadUrl(parseBean.url + url)
        }

        while (true){
            if (mWebView.get() == null){
                Thread.sleep(50)
                continue
            }
            return object : SnifferJob {
                override fun cancel() {
                    val webView = mWebView!!.get()
                    if (webView != null) {
                        webView.removeAllViews()
                        webView.destroy()
                    }
                }
            }
        }
    }

    // 访问回调
    interface Callback {
        fun success(parseBean: ParseBean?, res: String?)
        fun failed(parseBean: ParseBean?,errorInfo: String?)
        fun timeOut(parseBean: ParseBean?)
    }

    interface SnifferJob {
        fun cancel()
    }
}
