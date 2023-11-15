package com.xxhoz.secbox.parserCore.videoJxParser

import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.xxhoz.m3u8library.utils.M3u8Client
import com.xxhoz.secbox.App
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.sourceEngine.SnifferEngine
import com.xxhoz.secbox.util.GlobalActivityManager
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.StringUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DefaultVideoParserImpl {

    private var current = 0

    private lateinit var parsers: List<ParseBean>

    private lateinit var callback: Callback

    private lateinit var videoUrl: String

    private var interrupt: Boolean = false

    val snifferJobs = ArrayList<SnifferEngine.SnifferJob>()
    fun JX(videoUrl: String, parsers: List<ParseBean>, callback: Callback) {
        this.interrupt = false
        this.videoUrl = videoUrl
        this.parsers = parsers
        this.callback = callback

        this.current = 0
        getVideo()
    }

    /**
     * 取消任务
     */
    fun cancel() {
        interrupt = true
        for (snifferJob in snifferJobs) {
            snifferJob.cancel()
        }
        snifferJobs.clear()
    }

    @WorkerThread
    private fun getVideo() {
        if (current >= parsers.size) {
            if (!interrupt){
                callback.failed(null, "解析失败")
            }
            return
        }
        val parseBean = parsers.get(current)
        if (parseBean.type == 1) {
            // JSON
            jsonParser(parseBean)
        } else if (parseBean.type == 0) {
            // 嗅探
            snifferParser(parseBean)
        }
    }

    private fun snifferParser(parseBean: ParseBean) {
        callback.notifyChange(parseBean)
        val snifferJob: SnifferEngine.SnifferJob = SnifferEngine.JX(
            GlobalActivityManager.getTopActivity() as BaseActivity<*>,
            parseBean,
            videoUrl,
            15000,
            object : SnifferEngine.Callback {
                override fun success(parseBean: ParseBean?, res: String?) {
                    if (interrupt) {
                        return
                    }
                    cancel()
                    reportSuccess(parseBean, res)
                }

                override fun failed(parseBean: ParseBean?, errorInfo: String?) {
                    if (interrupt) {
                        return
                    }
                    callback.failed(parseBean, "解析失败")
                    LogUtils.d("接口: [${parseBean?.name}]  嗅探失败  ${errorInfo}")
                    next()
                }

            }
        )
        snifferJobs.add(snifferJob)
    }

    private fun jsonParser(parseBean: ParseBean) {
        try {
            callback.notifyChange(parseBean)
            val jsonObject = HttpUtil.get(parseBean.url + videoUrl, JSONObject::class.java)
            val result = jsonObject.getString("url")
            if (StringUtils.isEmpty(result)) {
                throw Exception("解析结果为空")
            }
            if (interrupt) {
                return
            }
            cancel()
            reportSuccess(parseBean, result)
        } catch (e: Exception) {
            callback.failed(parseBean, "解析失败")
            LogUtils.d("接口: [${parseBean.name}]  解析失败")
            e.printStackTrace()
            if (interrupt) {
                return
            }
            next()
        }
    }

    /**
     * 执行下一个解析
     */
    private fun next() {
        current++
        getVideo()
    }


    /*
     * 解析成功
     */
    private fun reportSuccess(parseBean: ParseBean?, res: String?) {

        try {
            val activity = GlobalActivityManager.getTopActivity() as BaseActivity<*>
            activity.SingleTask("temp_m3u8", activity.lifecycleScope.launch(IO) {
                // 下载m3u8文件
                val cacheFile = HttpUtil.downLoad(
                    res!!, App.instance.filesDir.absolutePath + "/temp.m3u8"
                ).absolutePath

                withContext(Main) {
                    val duration: Double = M3u8Client.create(cacheFile, res).getDuration()
                    if (duration <= 180) {
                        // 解析结果小于3分钟大概率为失败广告
                        callback.failed(parseBean!!, "解析失败 Code: 1001")
                    } else {
                        callback.success(parseBean!!, cacheFile)
                    }

                }
            })
        } catch (e: Exception) {
            LogUtils.e("解析失败 Code: 1002", e)
            callback.failed(parseBean!!, "解析失败 Code: 1002")
        }
    }

    interface Callback {
        /**
         * 解析成功
         */
        fun success(parseBean: ParseBean?, res: String)

        /**
         * 解析失败
         */
        fun failed(parseBean: ParseBean?, errorInfo: String)

        /**
         * 解析通知
         */
        fun notifyChange(parseBean: ParseBean)
    }
}
