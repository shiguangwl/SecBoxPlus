package com.xxhoz.constant

import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.bean.ConfigBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils

object BaseConfig {
    val CONFIG_JSON: String = "https://secbox.xxhoz.com/config.json"

    lateinit var CONFIG_BEAN: ConfigBean

    /**
     * 弹幕API
     */

    var DANMAKU_API = ""
    /**
     * 加载爬虫配置URL
     */
    var SOURCE_BASE_API = ""

    /**
     * 默认源
     */
     val DefualtSourceKey = "Qtv"

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

        var sourceKey: String = XKeyValue.getString(Key.CURRENT_SOURCE_KEY, DefualtSourceKey)

        var source: IBaseSource? = SourceManger.getSpiderSource(sourceKey)

        if (source == null){
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
