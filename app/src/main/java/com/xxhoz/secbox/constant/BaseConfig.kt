package com.xxhoz.constant

import android.widget.Toast
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils

object BaseConfig {

    /**
     * 加载配置API
     */
    var BASE_API = "https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt"

    /**
     * 默认源
     */
     val DefualtSourceKey = "肥猫"

    /**
     * 公告
     */
    var NOTION = "欢迎使用弹幕影视~"


    /**
     * 友盟配置
     */
    var UmengKey = "6394544aba6a5259c4cb5211"
    val UmengChannel = "官方"

    /**
     * 获取当前源,如果没有则返回默认源,如果没有默认源则返回null
     */
    public fun getCurrentSource(): IBaseSource? {
        val sourceBeanList: List<SourceBean> = SourceManger.getSourceBeanList()
        if (sourceBeanList.size == 0) {
            return null
        }

        var sourceKey: String = XKeyValue.getString(Key.SourceKey, DefualtSourceKey)

        var source: IBaseSource? = SourceManger.getSource(sourceKey)

        if (source == null){
            LogUtils.i("设置的源未找到,默认使用第一个源")
            var firstSource: SourceBean = sourceBeanList.get(0)
            sourceKey = firstSource.key
            source = SourceManger.getSource(sourceKey)
            // 更新当前源key
            XKeyValue.putString(Key.SourceKey, firstSource.key)
        }

        LogUtils.i("站源数量:${sourceBeanList.size}  当前获取站源为:${sourceKey}")
        return source
    }
}
