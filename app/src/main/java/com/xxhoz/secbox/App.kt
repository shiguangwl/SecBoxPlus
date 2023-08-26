package com.xxhoz.secbox

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import android.view.Gravity.BOTTOM
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.GlobalActivityManager
import com.xxhoz.secbox.util.LogUtils


/**
 * Application
 */
class App : Application() {

    private var applicationStartTime = 0L

    override fun onCreate() {
        super.onCreate()
        initInMainProcess {
            instance = this
            XKeyValue.init(this)
            GlobalActivityManager.init(this)
            // 初始化 Toast 框架
            Toaster.init(this);
            Toaster.setGravity(BOTTOM, 0, 200);
            Toaster.setView(R.layout.toast_custom_view)
            // 设置 Json 解析容错监听
            GsonFactory.setJsonCallback { typeToken, fieldName, jsonToken ->
                // 上报到 Bugly 错误列表中
                LogUtils.e("GsonFactory类型解析异常：$typeToken#$fieldName，后台返回的类型为：$jsonToken")
            }
            // 异常处理
            CrashManager.getInstance(this).init()
            // Application生命周期监听
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                fun onCreate() {
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onStart() {
                    // 应用启动埋点
                    // reportAppStart(0)
                    // 应用返回前台，记录时间戳
                    applicationStartTime = System.currentTimeMillis()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onStop() {
                    // 应用进入后台埋点
                    // reportAppStayTime(applicationStartTime, System.currentTimeMillis())
                }
            })
        }
    }

    /**
     * 主进程初始化
     */
    private fun initInMainProcess(block: () -> Unit) {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myPId = Process.myPid()
        val mainProcessName = packageName
        activityManager.runningAppProcesses.forEach {
            if (it.pid == myPId && it.processName == mainProcessName) {
                block()
            }
        }
    }

    companion object {
        lateinit var instance: Application
    }
}
