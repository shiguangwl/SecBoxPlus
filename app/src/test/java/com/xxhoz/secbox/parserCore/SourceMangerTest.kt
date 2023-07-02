package com.xxhoz.secbox.parserCore

import com.alibaba.fastjson.JSON
import com.xxhoz.network.fastHttp.OkHttpUtils
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.parserCore.bean.ConfigBeanList
import com.xxhoz.secbox.util.LogUtils
import org.junit.Test

internal class SourceMangerTest {

    @Test
    fun loadConfigUrl() {

        SourceManger.initData("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt")
    }

    @Test
    fun fastHttpTest(){
//        var json: String = OkHttpUtils.get("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt")

//        var parseObject: ConfigBeanList = JSON.parseObject(json, ConfigBeanList::class.java)
//
//        println(parseObject)

    }
}
