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

//        SourceManger.initData("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt")
//
//
//        val sourceBeanList = SourceManger.getSourceBeanList()
//        LogUtils.i("获取站源数量:${sourceBeanList.size}")


//        sourceBeanList.forEach {
//            LogUtils.e("================${it.name} Start=================")
//            val source = SourceManger.getSource(it.key)
//            LogUtils.e("站源:${it.key},homeVideoList:${source.homeVideoList()},categoryInfo:${source.categoryInfo()}")
//            LogUtils.e("================${it.name} End=================")
//        }
    }

    @Test
    fun fastHttpTest(){
//        var json: String = OkHttpUtils.get("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt")

//        var parseObject: ConfigBeanList = JSON.parseObject(json, ConfigBeanList::class.java)
//
//        println(parseObject)

    }
}
