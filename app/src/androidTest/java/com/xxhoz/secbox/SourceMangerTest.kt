package com.xxhoz.secbox

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xxhoz.common.util.LogUtils
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.network.HttpUtil
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourceMangerTest {



    @Test
    fun fastHttpTest(){

        val json = HttpUtil.get("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt",String::class.java)
        println("响应结果${json}")

//
//        LogUtils.e("11111111111111111")
//        LogUtils.e("11111111111111111")
    }


    @Test
    fun SourceTest(){
        SourceManger.loadSourceConfig("https://jihulab.com/clear1/yingmi/-/raw/main/xh.txt")


        val sourceBeanList = SourceManger.getSourceBeanList()
        LogUtils.i("获取站源数量:${sourceBeanList.size}")
    }
}
