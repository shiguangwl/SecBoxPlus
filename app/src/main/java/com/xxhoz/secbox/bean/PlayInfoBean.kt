package com.xxhoz.secbox.bean

import com.xxhoz.secbox.parserCore.bean.VideoBean
import java.io.Serializable

data class PlayInfoBean(
    val sourceKey: String,
    val videoBean: VideoBean,
    val preNum: Int
): Serializable
