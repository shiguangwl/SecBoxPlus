package com.xxhoz.secbox

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException


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
//        val danmus: String = HttpUtil.get("http://localhost:3000/?url=https://www.bilibili.com/bangumi/play/ep762813")
////                    val danmus: String = Jsoup.connect().get().html()
//        LogUtils.d("弹幕结果数据:" + danmus)
//////


    }

    @Test
    fun m3u8Test() {
        val urlToCheck = "" // 要检查的资源URL

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .head()
            .url(urlToCheck)
            .build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            val responseCode: Int = response.code
            if (responseCode == 200) {
                println("资源可用，HTTP响应代码为200 OK")
                println("响应内容:" + response.body?.bytes()?.size)
            } else {
                println("资源不可用，HTTP响应代码为 $responseCode")
            }
        }
    }

}
