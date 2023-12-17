package com.xxhoz.m3u8library.utils

import android.util.Log
import java.io.File
import java.math.BigDecimal


/**
 * 解析操作m3u8文件
 */
class M3u8Client private constructor(val path: String, val baseUrl: String?) {

    private val TAG: String = "M3u8Client"

    // m3u8文件
    private val fileArray: ArrayList<String> = ArrayList()

    // ts片段列表
    private val tsList: ArrayList<tsItem> = ArrayList()

    // m3u8文件总时长
    private var durations: BigDecimal = BigDecimal(0)

    init {
        try {
            // 加载m3u8文件
            File(path).bufferedReader().useLines { lines ->
                fileArray.addAll(lines)
            }

            // 解析ts片段列表
            for (i in fileArray.indices) {
                val line = fileArray[i]
                if (line.startsWith("#EXTINF:")) {
                    val duration = line.substring(8, line.lastIndexOf(",")).toDouble()
                    var url = fileArray[i + 1]
                    // 相对路径转绝对路径
                    if (baseUrl != null && line.startsWith("./")) {
                        url = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1) + line.substring(2)
                        fileArray[i + 1] = url
                    }
                    Log.d(TAG, "TS片段:$url 时长:$duration")
                    tsList.add(tsItem(url, duration))
                    durations = durations.plus(BigDecimal.valueOf(duration))
                }
            }
            Log.i(TAG, "成功加载M3U8文件 TS片段数:${tsList.size}  时长:${durations.toDouble()}")
        } catch (e: Exception) {
            Log.e(TAG, "加载M3U8文件错误", e)
            throw e
        }
    }

    companion object {
        fun create(path: String, baseUrl: String? = null): M3u8Client {
            return M3u8Client(path, baseUrl)
        }
    }

    /**
     * 获取时长
     * @return 时长秒
     */
    fun getDuration(): Double {
        return durations.toDouble()
    }


    data class tsItem(val url: String, val duration: Double)
}
