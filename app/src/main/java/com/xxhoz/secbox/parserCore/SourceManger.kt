package com.xxhoz.parserCore

import android.content.Context
import com.github.catvod.crawler.JarLoader
import com.github.catvod.crawler.JsLoader
import com.github.catvod.crawler.Spider
import com.google.gson.JsonObject
import com.xxhoz.network.fastHttp.OkHttpUtils
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.parserCore.parserImpl.SpiderSource
import com.xxhoz.secbox.App
import com.xxhoz.secbox.parserCore.bean.ParseBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.util.LogUtils


import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object SourceManger {

    // 解析器配置列表
    private var parseBeanList: MutableList<ParseBean> = ArrayList()

    // spider源配置列表
    private var sourceBeanList: HashMap<String, SourceBean> = HashMap()
    private var sourceList: HashMap<String, IBaseSource> = HashMap()

    // jar包
    private var spider: String? = null

    private val jarLoader = JarLoader()
    private val jsLoader = JsLoader()

    /**
     * 初始化资源
     */
    fun initData(baseUrl: String): Boolean {
        parseBeanList = ArrayList()
        sourceBeanList = HashMap()
        sourceList = HashMap()

        if (!loadConfig(baseUrl)) {
            LogUtils.e("加载配置文件失败")
            return false
        }

        if (!loadJar()) {
            LogUtils.e("加载JAR包失败")
            return false
        }
        return true
    }


    /**
     * 加载配置文件
     * @param url 配置文件链接
     */
    private fun loadConfig(url: String): Boolean {

        val infoJson = OkHttpUtils.get(url, JsonObject::class.java)

        if (infoJson == null) {
            return false
        }

        // spider
        spider = safeJsonString(infoJson, "spider", "")

        // 远端站点源
        for (opt in infoJson.get("sites").getAsJsonArray()) {
            val obj = opt as JsonObject
            val sb = SourceBean()
            val siteKey = obj["key"].asString.trim()
            sb.key = siteKey
            sb.name = obj["name"].asString.trim()
            sb.type = obj["type"].asInt
            sb.api = obj["api"].asString.trim()
            sb.setSearchable(safeJsonInt(obj, "searchable", 1))
            sb.setQuickSearch(safeJsonInt(obj, "quickSearch", 1))
            sb.filterable = safeJsonInt(obj, "filterable", 1)
            sb.playerUrl = safeJsonString(obj, "playUrl", "")
            sb.ext = safeJsonString(obj, "ext", "")
            sb.categories = safeJsonStringList(obj, "categories")
            sourceBeanList[siteKey] = sb
        }

        // 设置解析flag
        val vipParseFlags = safeJsonStringList(infoJson, "flags")
        sourceBeanList.values.forEach() {
            it.flags = vipParseFlags
        }


        // 解析地址
        for (opt in infoJson["parses"].asJsonArray) {
            val obj = opt as JsonObject
            val pb = ParseBean()
            pb.name = obj["name"].asString.trim()
            pb.url = obj["url"].asString.trim()
            val ext = if (obj.has("ext")) obj["ext"].asJsonObject.toString() else ""
            pb.ext = ext
            pb.type = safeJsonInt(obj, "type", 0)
            parseBeanList.add(pb)
        }

        return true
    }

    private fun loadJar(): Boolean {
        val split = spider?.split(";md5;")
        val url = split?.get(0)
        val md5 = split?.get(1)

        if (url == null) {
            return false
        }

        val cacheFile: File = File(App.instance.getFilesDir(), "/csp.jar")
        if (md5!!.isEmpty() || !cacheFile.exists() || !(getFileMd5(cacheFile).equals(md5.toLowerCase()))) {
            // md5不存在,或文件不存在,或md5不匹配则 下载最新文件
            downloadJar(url, cacheFile)
        }
        return jarLoader.load(cacheFile.absolutePath)

    }


    private fun downloadJar(url: String, cache: File) {
        val inputStream = OkHttpUtils.getBytes(url)
        val fileOutputStream: FileOutputStream =
            App.instance.openFileOutput(cache.name, Context.MODE_PRIVATE)

        fileOutputStream.write(inputStream)

        fileOutputStream.close()
    }

    private fun safeJsonInt(obj: JsonObject, key: String?, defaultVal: Int): Int {
        try {
            return if (obj.has(key)) obj.getAsJsonPrimitive(key).asInt else defaultVal
        } catch (th: Throwable) {
        }
        return defaultVal
    }

    private fun safeJsonString(obj: JsonObject, key: String?, defaultVal: String?): String? {
        try {
            return if (obj.has(key)) obj.getAsJsonPrimitive(key).asString.trim { it <= ' ' } else defaultVal
        } catch (th: Throwable) {
        }
        return defaultVal
    }

    private fun safeJsonStringList(obj: JsonObject, key: String?): ArrayList<String>? {
        val result = ArrayList<String>()
        try {
            if (obj.has(key)) {
                if (obj[key].isJsonObject) {
                    result.add(obj[key].asString)
                } else {
                    for (opt in obj.getAsJsonArray(key)) {
                        result.add(opt.asString)
                    }
                }
            }
        } catch (th: Throwable) {
        }
        return result
    }


    /**
     * 获取指定文件md5值
     */
    private fun getFileMd5(cache: File): Any {
        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance("MD5")
            val fis = FileInputStream(cache)
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
            fis.close()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return ""
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
        val bigInt = BigInteger(1, digest.digest())
        return bigInt.toString(16)
    }


    /**
     * 获取所有soureBean列表
     */
    fun getSourceBeanList(): List<SourceBean> {
        return sourceBeanList.values.toList()
    }

    /**
     * 获取指定key的源,未找到则返回为null
     */
    fun getSource(key: String): IBaseSource? {
        // sourceList 获取指定key的value不存在则创建新的设置进并返回
        return sourceList.getOrPut(key) {
            val sourceBean = sourceBeanList.get(key)
            if (sourceBean == null) {
                return null
            }
            var spider: Spider? = null;
            if (sourceBean.api.contains(".js")) {
                spider = jsLoader.getSpider(sourceBean.key, sourceBean.api, sourceBean.ext,null)
            } else {
                spider = jarLoader.getSpider(sourceBean.key, sourceBean.api, sourceBean.ext)
            }

            return SpiderSource(sourceBean, spider)
        }
    }


    /**
     * 获取所有支持搜索的源
     */
    fun getSearchAbleList(): List<IBaseSource> {
        val list: MutableList<IBaseSource> = ArrayList()
        sourceBeanList.values.forEach() {
            if (it.isSearchable || it.isSearchable) {
                val source = getSource(it.key)
                source?.let {
                    list.add(source)
                }
            }
        }
        return list
    }
}
