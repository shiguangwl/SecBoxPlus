package com.xxhoz.secbox.parserCore.danmuParser

import com.xxhoz.common.util.LogUtils
import com.xxhoz.common.util.MD5Utils
import com.xxhoz.secbox.App
import com.xxhoz.secbox.base.CacheManger
import com.xxhoz.secbox.network.HttpUtil
import java.io.File


/**
 * 官源弹幕
 */
class DefaultDanmuImpl(private val danmuApi: String) : IDanmuInterface {

    // 缓存有效时间
    private val l = 1000L * 60 * 60 * 24

    /**
     * 加载弹幕
     * 大于500kb弹幕文件缓存24小时
     */
    override fun getDanmaku(videoUrl: String): File {
        val hashCode = MD5Utils.getStringMD5(videoUrl)
        val fileName = "${hashCode}.xml"

        var cacheFile = CacheManger.getCacheFile(fileName)
        if (cacheFile !=null){
            LogUtils.d("读取缓存弹幕成功  ${fileName}")
            return cacheFile
        }
        LogUtils.d("开始下载弹幕文件:${danmuApi + videoUrl}")
        cacheFile = HttpUtil.downLoad(
            danmuApi + videoUrl, App.instance.filesDir.absolutePath + "/temp_danmu.xml"
        )
        LogUtils.d("下载弹幕文件成功:${cacheFile.length() / 1024} kb")
        // 当cacheFile文件小于300kb则不缓存
        if ((cacheFile.length() / 1024) < 200){
            LogUtils.d("acheFile文件小于300kb不缓存")
            return cacheFile
        }
        return CacheManger.cacheFile(fileName, cacheFile, l)
    }
}
