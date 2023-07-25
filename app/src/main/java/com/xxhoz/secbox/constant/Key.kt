package com.xxhoz.constant

import androidx.annotation.StringDef

@StringDef(
    Key.BASE_API,
    Key.SourceKey
)
@Retention(AnnotationRetention.SOURCE)
annotation class Key {
    companion object {
//        // 用户无关，一般情况下无需清除，注意不能加上account_前缀，参考XKeyValue.from()
//        const val XXX = "xxx"
//
//        // 用户相关，需要清除，统一加上account_前缀，自动选择正确的mmkv对象，参考XKeyValue.from()
//        const val ACCOUNT_XXX = "account_xxx"

        const val BASE_API = "account_base_api"

        /**
         * 源
         */
        const val SourceKey = "account_source_key"
    }
}
