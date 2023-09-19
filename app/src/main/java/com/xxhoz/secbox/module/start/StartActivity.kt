package com.xxhoz.secbox.module.start

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.annotation.WorkerThread
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.umeng.commonsdk.UMConfigure
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.ConfigBean
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityStartBinding
import com.xxhoz.secbox.module.main.MainActivity
import com.xxhoz.secbox.network.HttpUtil
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.NetworkHelper
import com.xxhoz.secbox.util.getActivity

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

        Task {
            onIO2 { initData() }
        }
    }

    /**
     * 初始化数据
     */
    @WorkerThread
    private fun initData() {
        if (!NetworkHelper.isNetworkConnect()) {
            Toaster.show("请检查网络连接")
            return
        }
        if(!BaseConfig.DEBUG){
            umengInit()
        }
        try {
            // 加载config.json
            configInit()
            // 检测更新
            if (checkUpdate(BaseConfig.CONFIG_BEAN)) {
                return
            }
            // 加载数据源配置
            try {
                SourceManger.loadSourceConfig(XKeyValue.getString(Key.CURRENT_SOURCE_URL,BaseConfig.BASE_SOURCE_URL))
            }catch (e:Exception){
                // 切换默认源
                Toaster.show("加载数据源配置失败,尝试切换默认源")
                XKeyValue.putString(Key.CURRENT_SOURCE_URL,BaseConfig.BASE_SOURCE_URL)
                SourceManger.loadSourceConfig(BaseConfig.BASE_SOURCE_URL)
            }

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
        BaseConfig.CONFIG_BEAN = configBean
        BaseConfig.BASE_SOURCE_URL = configBean.configJsonUrl
        BaseConfig.DANMAKU_API = configBean.danmukuApi
        BaseConfig.NOTION = configBean.notice
    }


    /**
     * 返回是否有新版本
     * @param configBean 配置信息
     * @return true 有新版本 false 没有新版本
     */
    private fun checkUpdate(configBean: ConfigBean): Boolean {
        val currentVersion =
            packageManager.getPackageInfo(packageName, 0).versionName
        // TODO 待优化
        if (!isLatestVersion(currentVersion,configBean.version)) {
            val popupView = XPopup.Builder(this).asConfirm("有新版本发布", configBean.msg, {
                    val uri = Uri.parse(configBean.downloadUrl)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
            })
            popupView.isHideCancel = true
            popupView.show();
            return true
        }
        return  false
    }

    /**
     * 初始化友盟配置
     */
    private fun umengInit() {
        UMConfigure.preInit(this, BaseConfig.UmengKey, BaseConfig.UmengChannel)
        UMConfigure.init(this, BaseConfig.UmengKey, BaseConfig.UmengChannel, UMConfigure.DEVICE_TYPE_PHONE, "")
    }


    /**
     * 判断是否最新版本
     * @param currentVersion 当前版本
     * @param latestVersion 最新版本
     * @return 前版本大于或等于最新版本时返回true，当前版本小于最新版本时返回false
     */
    fun isLatestVersion(currentVersion: String, latestVersion: String): Boolean {
        LogUtils.d("当前版本: $currentVersion")
        LogUtils.d("最新版本: $latestVersion")
        val currentParts = currentVersion.split(".")
        val latestParts = latestVersion.split(".")

        val maxLength = maxOf(currentParts.size, latestParts.size)

        for (i in 0 until maxLength) {
            val currentPart = if (i < currentParts.size) currentParts[i] else "0"
            val latestPart = if (i < latestParts.size) latestParts[i] else "0"

            val currentInt = currentPart.toIntOrNull() ?: 0
            val latestInt = latestPart.toIntOrNull() ?: 0

            if (currentInt < latestInt) {
                return false
            } else if (currentInt > latestInt) {
                return true
            }
        }

        return true // Both versions are equal
    }

    @PageName
    override fun getPageName() = PageName.START


}
