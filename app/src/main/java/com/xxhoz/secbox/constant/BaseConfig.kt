package com.xxhoz.constant

import com.xxhoz.common.util.LogUtils
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.bean.ConfigBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.GlobalActivityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BaseConfig {

    /**
     * 是否第一次启动
     */
    val isFirstStart: Boolean by lazy {
        val boolean = XKeyValue.getBoolean(Key.FIRST_START, true)
        XKeyValue.putBoolean(Key.FIRST_START, false)
        return@lazy boolean
    }

    /**
     * 是否为debug版本
     */
    val DEBUG: Boolean by lazy {
        // 判断是否为debug版本
        val activity = GlobalActivityManager.getTopActivity()!!
        if (activity.packageName.contains(".dev")) {
            return@lazy true
        }
        false
    }

    val CONFIG_JSON: String by lazy {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
        val format = sdf.format(Date())
        val d = format.takeLast(2).toInt() / 15
//        "https://secbox.xxhoz.com/config.json?key=" + "${format.dropLast(2)}%02d".format(d)
        "https://secbox.xxhoz.com/config.json"
    }

    lateinit var CONFIG_BEAN: ConfigBean

    /**
     * 弹幕API
     */

    var DANMAKU_API = ""

    /**
     * 加载爬虫配置URL
     */
    var BASE_SOURCE_URL = ""

    /**
     * 默认源
     */
    var DefaultSourceKey = ""

    /**
     * 公告
     */
    var NOTION = "欢迎使用弹幕影视~"


    /**
     * 友盟配置
     */
    var UmengKey = "64f55fd25488fe7b3a04dfc8"
    val UmengChannel = "官方"

    /**
     * 获取当前源,如果没有则返回默认源,如果没有默认源则返回null
     */
    fun getCurrentSource(): IBaseSource? {
        val sourceBeanList: List<SourceBean> = SourceManger.getSourceBeanList()
        if (sourceBeanList.size == 0) {
            return null
        }

        var sourceKey: String = XKeyValue.getString(Key.CURRENT_SOURCE_KEY, DefaultSourceKey)

        var source: IBaseSource? = SourceManger.getSpiderSource(sourceKey)

        if (source == null) {
            LogUtils.i("设置的源未找到,默认使用第一个源")
            var firstSource: SourceBean = sourceBeanList.get(0)
            sourceKey = firstSource.key
            source = SourceManger.getSpiderSource(sourceKey)
            // 更新当前源key
            XKeyValue.putString(Key.CURRENT_SOURCE_KEY, firstSource.key)
        }

        LogUtils.i("站源数量:${sourceBeanList.size}  当前获取站源为:${sourceKey}")
        return source
    }
}
