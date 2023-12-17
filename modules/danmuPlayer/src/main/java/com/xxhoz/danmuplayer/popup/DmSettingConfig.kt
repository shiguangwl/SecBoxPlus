package com.xxhoz.danmuplayer.popup

data class DmSettingConfig(

    val minScrollSpeed: Float = 1f,
    val maxScrollSpeed: Float = 5f,

    val minScaleTextSize: Float = 0.5f,
    val maxScaleTextSize: Float = 2f,

    val minMaximumLines: Int = 1,
    val maxMaximumLines: Int = 15
)
