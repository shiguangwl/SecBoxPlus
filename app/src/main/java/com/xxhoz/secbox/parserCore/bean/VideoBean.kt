package com.xxhoz.secbox.parserCore.bean

import java.io.Serializable

data class VideoBean(
    val vod_id: String,
    val vod_name: String,
    val vod_pic: String,
    val vod_remarks: String
): Serializable
