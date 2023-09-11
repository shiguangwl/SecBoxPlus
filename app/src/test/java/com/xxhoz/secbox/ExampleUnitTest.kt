package com.xxhoz.secbox

import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.util.LogUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun httpTest() {
        val danmus: String = HttpUtil.get("http://localhost:3000/?url=https://www.bilibili.com/bangumi/play/ep762813")
//                    val danmus: String = Jsoup.connect().get().html()
        LogUtils.d("弹幕结果数据:" + danmus)
    }


}
