package com.xxhoz.secbox.constant

import androidx.annotation.StringDef

@StringDef(EventName.REFRESH_HOME_LIST, EventName.TEST, EventName.SOURCE_CHANGE)
@Retention(AnnotationRetention.SOURCE)
annotation class EventName {
    companion object {
        const val REFRESH_HOME_LIST = "refresh_home_list"
        const val SOURCE_CHANGE = "source_change"
        const val TEST = "test"
    }
}
