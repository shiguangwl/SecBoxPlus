package com.xxhoz.secbox.module.about

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.gyf.immersionbar.ktx.immersionBar
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.EventName
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityAboutBinding
import com.xxhoz.secbox.eventbus.XEventBus

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    private val viewModel: AboutViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivityAboutBinding
        get() = ActivityAboutBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        initSystemBar()

        viewBinding.tvAbout.setOnClickListener {
            XEventBus.post(EventName.TEST, "来自关于页面的消息")
        }
    }

    /**
     * 状态栏导航栏初始化
     */
    private fun initSystemBar() {
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
    }

    @PageName
    override fun getPageName() = PageName.ABOUT
}
