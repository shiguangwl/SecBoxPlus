package com.xxhoz.secbox.constant

import androidx.annotation.StringDef

@StringDef(
    PageName.START,
    PageName.MAIN,
    PageName.HOME,
    PageName.ACGN,
    PageName.SMALL_VIDEO,
    PageName.GOLD,
    PageName.MINE,
    PageName.ABOUT,
    PageName.DISCOVERY,
    PageName.DETAIL_PLAYER,
    PageName.SEARCH,
    PageName.FILTER,
    PageName.SNIFFER
)
@Retention(AnnotationRetention.SOURCE)
annotation class PageName {
    companion object {
        const val START = "start"
        const val MAIN = "main"
        const val HOME = "home"
        const val ACGN = "acgn"
        const val SMALL_VIDEO = "small_video"
        const val GOLD = "gold"
        const val MINE = "mine"
        const val ABOUT = "about"
        const val DISCOVERY = "discovery"
        const val DETAIL_PLAYER = "detail_player"
        const val SEARCH = "search"
        const val FILTER = "filter"
        const val SNIFFER = "sniffer"
    }
}
