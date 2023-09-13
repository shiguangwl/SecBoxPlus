package com.xxhoz.secbox.constant

import androidx.annotation.StringDef

@StringDef(
    TabId.HOME,
    TabId.ACGN,
    TabId.SMALL_VIDEO,
    TabId.GOLD,
    TabId.MINE,
    TabId.DISCOVERY,
    TabId.SNIFFER
)
@Retention(AnnotationRetention.SOURCE)
annotation class TabId {
    companion object {
        const val HOME = "home"
        const val ACGN = "acgn"
        const val SMALL_VIDEO = "small_video"
        const val GOLD = "gold"
        const val MINE = "mine"
        const val SNIFFER = "sniffer"
        const val DISCOVERY = "discovery"
    }
}
