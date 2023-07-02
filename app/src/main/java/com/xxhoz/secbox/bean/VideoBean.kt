package com.xxhoz.secbox.bean

import com.xxhoz.secbox.constant.VideoType

data class VideoBean(
    val id: String,
    val title: String,
    val coverImg: String,
    val authorId: String,
    val authorName: String,
    val playCount: Long,
    @VideoType
    val type: Int,
)
