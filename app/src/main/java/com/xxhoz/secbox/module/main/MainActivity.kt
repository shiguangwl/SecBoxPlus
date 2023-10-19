package com.xxhoz.secbox.module.main

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Explode
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.umeng.analytics.MobclickAgent
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.Tab
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.TabId
import com.xxhoz.secbox.databinding.ActivityMainBinding
import com.xxhoz.secbox.module.home.TabHomeFragment
import com.xxhoz.secbox.module.mine.MineFragment
import com.xxhoz.secbox.module.sniffer.SnifferFragment
import com.xxhoz.secbox.util.GlobalActivityManager
import com.xxhoz.secbox.util.getActivity
import com.xxhoz.secbox.widget.NavigationView
import com.xxhoz.secbox.widget.TabIndicatorView

/**
 * 首页
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    // 当前选中的底栏ID
    @TabId
    private var currentTabId = TabId.HOME

    companion object{
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(context.getActivity()).toBundle()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystemBar()
        updateTitle()
        initTabs()
    }


    override fun setContentView(view: View?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.enterTransition = Explode()
        super.setContentView(view)
    }

    @PageName
    override fun getPageName() = PageName.MAIN

    /**
     * 禁用左滑返回
     */
    override fun swipeBackEnable() = false

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

    /**
     * 初始化底栏
     */
    private fun initTabs() {
        val tabs = listOf(
            Tab(TabId.HOME, getString(R.string.page_home), R.drawable.selector_btn_home, TabHomeFragment::class),
            Tab(TabId.SNIFFER, getString(R.string.page_sniffer), R.drawable.selector_btn_discovery, SnifferFragment::class),
//            Tab(TabId.ACGN, getString(R.string.page_acgn), R.drawable.selector_btn_acgn, AcgnFragment::class),
//          Tab(TabId.SMALL_VIDEO, getString(R.string.page_small_video), R.drawable.selector_btn_small_video, SmallVideoFragment::class),
//          Tab(TabId.GOLD, getString(R.string.page_gold), R.drawable.selector_btn_gold, GoldFragment::class),
//          Tab(TabId.DISCOVERY, getString(R.string.page_discovery), R.drawable.selector_btn_discovery, DiscoveryFragment::class),
            Tab(TabId.MINE, getString(R.string.page_mine), R.drawable.selector_btn_mine, MineFragment::class)
        )

        viewBinding.fragmentTabHost.run {
            setup(this@MainActivity, supportFragmentManager, viewBinding.fragmentContainer.id)
            tabs.forEach {
                val (id, title, icon, fragmentClz) = it
                val tabSpec = newTabSpec(id).apply {
                    setIndicator(TabIndicatorView(this@MainActivity).apply {
                        viewBinding.tabIcon.setImageResource(icon)
                        viewBinding.tabTitle.text = title
                    })
                }
                addTab(tabSpec, fragmentClz.java, null)
            }

            setOnTabChangedListener { tabId ->
                currentTabId = tabId
                updateTitle()
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            GlobalActivityManager.finishAll()
            MobclickAgent.onKillProcess(this)
            System.exit(0)
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toaster.show(getString(R.string.press_again_exit_app))

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
    /**
     * 更新标题
     */
    private fun updateTitle() {
        val title = when (currentTabId) {
            TabId.HOME -> getString(R.string.page_home)
            TabId.SMALL_VIDEO -> getString(R.string.page_small_video)
            TabId.ACGN -> getString(R.string.page_acgn)
            TabId.GOLD -> getString(R.string.page_gold)
            TabId.MINE -> getString(R.string.page_mine)
            TabId.SNIFFER -> getString(R.string.page_sniffer)
            TabId.DISCOVERY -> getString(R.string.page_discovery)
            else -> ""
        }

        viewBinding.navigationBar.setParameter(
            NavigationView.ParameterBuilder()
                .setShowBack(false)
                .setShowTitle(true)
                .setTitle(title)
        )
    }

    /**
     * 设置当前选中的TAB
     */
    private fun setCurrentTab(@TabId tabID: String) {
        viewBinding.fragmentTabHost.setCurrentTabByTag(tabID)
    }
}
