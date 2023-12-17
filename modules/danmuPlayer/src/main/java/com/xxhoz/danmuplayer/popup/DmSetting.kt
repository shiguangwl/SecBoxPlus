package com.xxhoz.danmuplayer.popup

data class DmSetting(
        /**
         * 滚动速度
         */
        var scrollSpeed: Float = 1.8f,
        /**
         * 自提大小
         */
        var scaleTextSize: Float = 0.9f,
        /**
         * 最大行数
         */
        var maximumLines: Int = 7
)
