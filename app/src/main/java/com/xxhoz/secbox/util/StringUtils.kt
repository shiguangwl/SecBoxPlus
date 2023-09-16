package com.xxhoz.secbox.util

object StringUtils {
    /**
     * 判断字符串是否为空或NULL
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.trim { it <= ' ' }.isEmpty()
    }
}
