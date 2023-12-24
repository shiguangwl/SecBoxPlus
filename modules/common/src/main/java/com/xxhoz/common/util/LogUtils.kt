package com.xxhoz.secbox.util

import android.util.Log
import com.xxhoz.common.BuildConfig

import java.util.Objects

object LogUtils {
    var className //类名
            : String? = null

    var methodName //方法名
            : String? = null

    var lineNumber //行数
            = 0

    private val isUnitTestEnvironment =
        Objects.requireNonNull(System.getProperty("java.class.path")).contains("UnitTest")

    private val isDebuggable: Boolean
        get() = BuildConfig.DEBUG

    private fun createLog(log: String): String {
        val buffer = StringBuffer()
//        buffer.append(methodName)
        buffer.append("(").append(className).append(":").append(lineNumber).append(")-")
        buffer.append(log)
        return buffer.toString()
    }

    private fun getMethodNames(sElements: Array<StackTraceElement>) {
        className = sElements[1].fileName
        methodName = sElements[1].methodName
        lineNumber = sElements[1].lineNumber
    }

    fun e(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.e(className, createLog(message))
    }

    fun e(message: String, e: Throwable) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.e(className, createLog(message), e)
    }

    fun i(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.i(className, createLog(message))
    }

    fun d(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.d(className, createLog(message))
    }

    fun v(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.v(className, createLog(message))
    }

    fun w(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.w(className, createLog(message))
    }

    fun wtf(message: String) {
        if (!isDebuggable) {
            return
        }
        getMethodNames(Throwable().stackTrace)
        if (isUnitTestEnvironment) {
            println(createLog(message))
            return
        }
        Log.wtf(className, createLog(message))
    }
}
