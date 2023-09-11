package com.xxhoz.secbox.module.start

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.umeng.commonsdk.UMConfigure
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.ConfigBean
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityStartBinding
import com.xxhoz.secbox.module.main.MainActivity
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.NetworkHelper
import com.xxhoz.secbox.util.getActivity
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

        lifecycleScope.launch(Dispatchers.IO) {
            initData()
            // 初始化友盟  调试版不做记录
            if (!getPackageName().contains(".dev")) {
                umengInit()
            }
        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        if (!NetworkHelper.isNetworkConnect()) {
            Toaster.show("请检查网络连接")
            return
        }
        try {
            configInit()
            MainActivity.startActivity(getActivity()!!)
        }catch (e:Exception){
            Toaster.showLong(e.message)
            e.printStackTrace()
        }
    }

    /**
     * 加载配置文件
     */
    private fun configInit() {
        // 配置加载

        val configBean = try {
            HttpUtil.get(BaseConfig.CONFIG_JSON, ConfigBean::class.java)
        } catch (e: Exception) {
            throw GlobalException.of("加载配置失败"+e.message)
        }

        LogUtils.i("加载配置成功: $configBean")

        // 设置基本配置信息
        BaseConfig.SOURCE_BASE_API = configBean.configJsonUrl
        BaseConfig.DANMAKU_API = configBean.danmukuApi
        BaseConfig.NOTION = configBean.notice

        // 加载数据源配置
        SourceManger.loadSourceConfig(BaseConfig.SOURCE_BASE_API)
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
    private fun umengInit() {
        UMConfigure.preInit(this, BaseConfig.UmengKey, BaseConfig.UmengChannel)
        UMConfigure.init(this, BaseConfig.UmengKey, BaseConfig.UmengChannel, UMConfigure.DEVICE_TYPE_PHONE, "")
    }

    @PageName
    override fun getPageName() = PageName.START


}
