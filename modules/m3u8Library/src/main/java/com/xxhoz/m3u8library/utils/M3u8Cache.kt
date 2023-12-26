package com.xxhoz.m3u8library.utils

import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.util.LogUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * 解析操作m3u8文件
 */
class M3u8Cache private constructor(var baseUrl: String, var path: String) {

    private val TAG: String = "M3u8Client"

    // m3u8文件数据,按行存储
    private val fileArray: ArrayList<String> = ArrayList()

    // ts片段列表
    private val tsList: ArrayList<tsItem> = ArrayList()

    // m3u8文件总时长
    private var durations: BigDecimal = BigDecimal(0)

    companion object {
        fun create(path: String, baseUrl: String): M3u8Cache {
            return M3u8Cache(baseUrl, path)
        }
    }

    init {
        try {
            loadM3u8FileToDataArray(baseUrl, path)
            // 判断是否为分P文件,及是否包含#EXT-X-STREAM-INF
            for ((index, line) in fileArray.withIndex()) {
                if (line.startsWith("#EXT-X-STREAM-INF")) {
                    LogUtils.i("开始处理分P文件...")
                    var url = fileArray[index + 1]
                    if (!url.startsWith("http")) {
                        url = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1) + url
                    }
                    loadM3u8FileToDataArray(url, path)
                    break
                }
            }

            // 解析ts片段列表
            for (i in fileArray.indices) {
                val line = fileArray[i]
                if (line.startsWith("#EXTINF:")) {
                    val duration = line.substring(8, line.lastIndexOf(",")).toDouble()
                    var urlLine = fileArray[i + 1]
                    // 相对路径转绝对路径
                    if (!(urlLine.startsWith("http:") || urlLine.startsWith("https:"))) {
                        if (urlLine.startsWith("./")) {
                            urlLine = urlLine.substring(2)
                        }
                        if (urlLine.startsWith("/")) {
                            urlLine = urlLine.substring(1)
                        }
                        urlLine = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1) + urlLine
                        fileArray[i + 1] = urlLine
                    }
//                    Log.d(TAG, "TS片段:$url 时长:$duration")
                    tsList.add(tsItem(urlLine, duration))
                    durations = durations.plus(BigDecimal.valueOf(duration))
                }
            }
            // 覆盖写入文件
            File(path).writeText(fileArray.joinToString("\n"))
//            removeAd()
            LogUtils.i("成功加载M3U8文件 TS片段数:${tsList.size}  时长:${durations.toDouble()}")

        } catch (e: Exception) {
            LogUtils.e("加载M3U8文件错误", e)
            throw e
        }
    }


    fun loadM3u8FileToDataArray(url: String, path: String) {
        fileArray.clear()
        // 下载文件
        val file = downLoadFile(url, path)
        // 加载m3u8文件
        file.bufferedReader().useLines { lines ->
            fileArray.addAll(lines)
        }
    }

    /**
     * 下载文件
     * @param url 下载地址
     * @param path 保存路径
     * @return 保存的文件
     */
    fun downLoadFile(url: String, path: String): File {
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "User-Agent",
                "ExoPlayerLib/2.18.1 (Linux; Android 13; Manufacturer Model Build/20231225)"
            )
            .addHeader("Accept-Encoding", "gzip")
            .build()

        // 设置超时时间为20秒
        val client = OkHttpClient.Builder()
            // 设置连接超时时间，单位为秒
            .connectTimeout(30, TimeUnit.SECONDS)
            // 设置读取超时时间，单位为秒
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpUtil.DeflateInterceptor())
            .build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            baseUrl = response.request.url.toString()
            LogUtils.i("重定向后的地址:$baseUrl")

            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            file.writeBytes(response.body?.bytes()!!)
            return file
        }
    }


    /**
     * 待修复
     * 去广告,找出fileArray中ts片段每个域名出现的次数,出现次数最多的域名为广告域名,删除该域名的ts片段,修改m3u8文件时长,覆盖写入文件path
     */
    fun removeAd() {
        val map = HashMap<String, Int>()
        for (item in tsList) {
            val url = item.url
            val domain = url.substring(0, url.lastIndexOf("/"))
            if (map.containsKey(domain)) {
                map[domain] = map[domain]!! + 1
            } else {
                map[domain] = 1
            }
        }
        var maxDomain = ""
        var maxCount = 0
        for (item in map) {
            if (item.value > maxCount) {
                maxDomain = item.key
                maxCount = item.value
            }
        }
        LogUtils.i("广告域名:$maxDomain 出现次数:$maxCount")
        // 删除广告域名的ts片段
        val newTsList = ArrayList<tsItem>()
        for (item in tsList) {
            val url = item.url
            val domain = url.substring(0, url.lastIndexOf("/"))
            if (domain != maxDomain) {
                newTsList.add(item)
            }
        }
        // 修改m3u8文件时长
        var newDurations = BigDecimal(0)
        for (item in newTsList) {
            newDurations = newDurations.plus(BigDecimal.valueOf(item.duration))
        }
        durations = newDurations

        // 重新写入m3u8文件
        val newFileArray = ArrayList<String>()
        for (line in fileArray) {
            if (line.startsWith(maxDomain)) {
                continue
            }
            if (line.startsWith("#EXTINF:")) {
                newFileArray.add("#EXTINF: " + durations.toDouble())
            } else {
                newFileArray.add(line)
            }
        }

        LogUtils.i("去广告成功")
    }

    /**
     * 获取时长
     * @return 时长秒
     */
    fun getDuration(): Double {
        return durations.toDouble()
    }

    /**
     * 获取ts片段列表
     * @return ts片段列表
     */
    fun getTsList(): Int {
        return tsList.size
    }

    fun getFinnalUrl(): String {
        return path
    }

    data class tsItem(val url: String, val duration: Double)
}
