package com.xxhoz.constant

import androidx.annotation.StringDef

@StringDef(
    Key.SUBSCRIBE_LIST,
    Key.CURRENT_SOURCE_URL,
    Key.CURRENT_SOURCE_KEY,
    Key.SEARCH_HISTORY,
    Key.DANMAKU_STATE,
    Key.PLAY_History,
    Key.CUSTOM_SITE,
    Key.FIRST_START,
    Key.CACHE_FILE_LIST
)
@Retention(AnnotationRetention.SOURCE)
annotation class Key {
    companion object {
//        // 用户无关，一般情况下无需清除，注意不能加上account_前缀，参考XKeyValue.from()
//        const val XXX = "xxx"
//
//        // 用户相关，需要清除，统一加上account_前缀，自动选择正确的mmkv对象，参考XKeyValue.from()
//        const val ACCOUNT_XXX = "account_xxx"

        /**
         * 订阅列表
         */
        const val SUBSCRIBE_LIST = "account_subscribe_list"

        /**
         * 当前订阅链接
         */
        const val CURRENT_SOURCE_URL = "account_current_source_url"
        /**
        * 当前源key
         */
        const val CURRENT_SOURCE_KEY = "account_source_key"

        /**
         * 搜索历史
         */
        const val SEARCH_HISTORY = "account_search_history"

        /**
         * 弹幕开启状态
         */
        const val DANMAKU_STATE = "account_danmaku_state"

        /**
         * 播放记录
         */
        const val PLAY_History = "account_play_history"

        /**
         * 自定义站点
         */
        const val CUSTOM_SITE = "account_custom_site"

        /**
         * 是否第一次启动
         */
        const val FIRST_START: String = "first_start"

        /**
         * 缓存文件记录
         */
        const val CACHE_FILE_LIST: String = "cache_file_list"
    }
}
