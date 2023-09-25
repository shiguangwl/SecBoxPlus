package com.xxhoz.secbox.parserCore.danmuParser

import java.io.File


/**
 * 弹幕接口
 */
interface IDanmuInterface {

    /**
     * 根据URL获取弹幕数据
     */
    fun getDanmaku(videoUrl: String): File
}
