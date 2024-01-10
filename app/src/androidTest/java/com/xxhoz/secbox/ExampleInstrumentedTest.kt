package com.xxhoz.secbox

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xxhoz.common.util.LogUtils
import com.xxhoz.m3u8library.utils.M3u8Cache
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.nan.xarch", appContext.packageName)

        LogUtils.i("下载文件:" + App.instance.filesDir.absolutePath + "/temp.m3u8")
        val m3U8Cache = M3u8Cache.create(
            App.instance.filesDir.absolutePath + "/temp.m3u8",
            "https://vip.ffzy-play1.com/20221117/18247_d528a6ca/index.m3u8"
        )

        println(m3U8Cache.getDuration())

    }
}
