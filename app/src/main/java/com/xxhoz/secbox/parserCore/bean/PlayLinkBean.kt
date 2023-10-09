package com.xxhoz.secbox.parserCore.bean

data class PlayLinkBean(
    val parse: Int,
    val jx: Int,
    val header: String,
    val playUrl: String,
    var url: String,
    // TODO
    var danmaku: String
)
