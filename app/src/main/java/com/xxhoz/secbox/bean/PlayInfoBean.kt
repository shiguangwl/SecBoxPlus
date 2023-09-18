package com.xxhoz.secbox.bean

import com.xxhoz.secbox.parserCore.bean.VideoBean
import java.io.Serializable

data class PlayInfoBean(
    val sourceKey: String,
    val videoBean: VideoBean,
    var preNum: Int,
    var position: Long = 0,
    var subscription: String = ""
): Serializable
