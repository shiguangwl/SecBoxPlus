package com.xxhoz.secbox.util

import java.security.MessageDigest

object MD5Utils {

    /**
     * 计算字符串的 MD5 哈希值
     *
     * @param input 要计算哈希值的字符串
     * @return 字符串的 MD5 哈希值的十六进制表示
     */
    fun getStringMD5(input: String): String {
        try {
            val messageDigest = MessageDigest.getInstance("MD5")
            val digest = messageDigest.digest(input.toByteArray())
            val hexString = StringBuilder()

            for (byte in digest) {
                hexString.append(String.format("%02x", byte))
            }

            return hexString.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

}
