package com.xxhoz.secbox.parserCore.videoJxParser

import androidx.annotation.WorkerThread
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.sourceEngine.SnifferEngine
import com.xxhoz.secbox.util.GlobalActivityManager
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.StringUtils
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
                callback.failed("解析失败")
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
                    callback.success(parseBean, res)
                }

                override fun failed(parseBean: ParseBean?, errorInfo: String?) {
                    if (interrupt) {
                        return
                    }
                    LogUtils.d("接口: [${parseBean?.name}]  嗅探失败  ${errorInfo}")
                    next()
                }

            }
        )
        snifferJobs.add(snifferJob)
    }

    private fun jsonParser(parseBean: ParseBean) {
        try {
            val jsonObject = HttpUtil.get(parseBean.url + videoUrl, JSONObject::class.java)
            val result = jsonObject.getString("url")
            if (StringUtils.isEmpty(result)) {
                throw Exception("解析结果为空")
            }
            if (interrupt) {
                return
            }
            cancel()
            callback.success(parseBean, result)
        } catch (e: Exception) {
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


    interface Callback {
        fun success(parseBean: ParseBean?, res: String?)
        fun failed(errorInfo: String?)
    }
}
