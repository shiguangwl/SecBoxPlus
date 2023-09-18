package com.xxhoz.parserCore


import android.content.Context
import com.github.catvod.crawler.JarLoader
import com.github.catvod.crawler.JsLoader
import com.github.catvod.crawler.Spider
import com.google.gson.JsonObject
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.parserCore.parserImpl.SpiderSource
import com.xxhoz.secbox.App
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.network.HttpUtil
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
    fun loadSourceConfig(baseUrl: String) {
        parseBeanList = ArrayList()
        sourceBeanList = HashMap()
        sourceList = HashMap()

        // 加载站源配置
        loadConfig(baseUrl)
        // 加载Jar包
        loadJar()
    }


    /**
     * 加载配置文件
     * @param url 配置文件链接
     */
    private fun loadConfig(url: String) {
        val infoJson = try {
            HttpUtil.get(url, JsonObject::class.java)
        } catch (e: Exception) {
            throw GlobalException.of("加载站源配置失败")
        }


        try {// spider
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
        } catch (e: Exception) {
            e.printStackTrace()
            throw GlobalException.of("解析站源配置失败")
        }

    }

    private fun loadJar() {
        val split = spider?.split(";md5;")
        val url = split?.get(0)
        val md5 = split?.get(1)

        if (url == null) {
            LogUtils.i("配置文件中Jar路径为空")
            throw GlobalException.of("配置文件中Jar路径为空")
        }

        val jarFile = File(App.instance.filesDir, "/csp.jar")
        // md5不存在,或文件不存在,或md5不匹配则 下载最新文件

        if (isDownLoadJar(md5, jarFile)) {
            LogUtils.i("开始下载Jar包......")
            downloadJar(url, jarFile)
        }else{
            LogUtils.i("使用缓存Jar包......")
        }
        val load = jarLoader.load(jarFile.absolutePath)
        if (!load){
            throw GlobalException.of("加载Jar包失败")
        }
    }

    private fun isDownLoadJar(md5: String?, jarFile: File) =
        md5!!.isEmpty() || !jarFile.exists() || !(getFileMd5(jarFile).equals(md5.toLowerCase()))


    private fun downloadJar(url: String, cache: File) {
        val inputStream = HttpUtil.getBytes(url)
        val fileOutputStream: FileOutputStream = App.instance.openFileOutput(cache.name, Context.MODE_PRIVATE)
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
        val md5 = bigInt.toString(16)
        // 如果md5不满32位开头补0
        return if (md5.length < 32) {
            String.format("%0${32 - md5.length}d", 0) + md5
        } else md5
    }

    /**
     * 获取解析接口列表
     */
    fun getParseBeanList(): MutableList<ParseBean> {
        return parseBeanList
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
    fun getSpiderSource(key: String): IBaseSource? {
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
            if (it.isSearchable) {
                val source = getSpiderSource(it.key)
                source?.let {
                    list.add(source)
                }
            }
        }
        return list
    }
}
