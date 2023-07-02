package com.xxhoz.secbox.module.start

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.gyf.immersionbar.ktx.immersionBar
import com.xxhoz.constant.BaseConfig
import com.xxhoz.network.fastHttp.OkHttpUtils
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.App
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityStartBinding
import com.xxhoz.secbox.module.main.MainActivity
import com.xxhoz.secbox.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartActivity : BaseActivity<ActivityStartBinding>() {


    private val viewModel: StartViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivityStartBinding
        get() = ActivityStartBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }

        initData()
    }

    private fun initData() {
        // 初始化友盟
        youmengInit()
        // 加载配置

        configInit {
            if (it.length>0) {
                // 加载配置文件失败,或有更新
                Toast.makeText(App.instance, it, Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }

    }

    /**
     * 加载配置文件
     */
    private fun configInit(callback: (String) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonObject =
                OkHttpUtils.get("https://shiguang.cachefly.net/config.json", JsonObject::class.java)

            if (jsonObject == null) {
                callback.invoke("加载配置信息失败")
                return@launch
            }

            LogUtils.i("加载配置信息成功: $jsonObject")
            val version = jsonObject["version"].asString
            val downUrl = jsonObject["downloadUrl"].asString
            val notice = jsonObject["notice"].asString
            val baseApi = jsonObject["configJsonUrl"].asString

            // 设置基本配置信息
            BaseConfig.BASE_API = baseApi
            BaseConfig.NOTION = notice

            // 加载数据源配置
            SourceManger.initData(BaseConfig.BASE_API)
            // 检测更新状态
            val checkUpdate = checkUpdate(version, downUrl)
            if (checkUpdate){
                callback.invoke("有新版本发布");
                return@launch
            }

            callback.invoke("")
        }
    }


    /**
     * 返回是否有新版本
     */
    private fun checkUpdate(version: String, downUrl: String): Boolean {
        val currentVersion =
            packageManager.getPackageInfo(packageName, 0).versionName.replace(".", "").toInt()
        val latestVersion = version.replace(".", "").toInt()

        if (currentVersion < latestVersion) {
            LogUtils.i("有新版本发布 最新版本:" + latestVersion + "当前版本:" + currentVersion)
            return true
        } else {
            return false
        }
    }

    /**
     * 初始化友盟配置
     */
    private fun youmengInit() {

    }

    @PageName
    override fun getPageName() = PageName.MAIN


}
