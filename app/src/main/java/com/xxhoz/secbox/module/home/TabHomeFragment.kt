package com.xxhoz.secbox.module.home

//import com.xxhoz.secbox.module.home.view.BottomSheetSource
import BottomSheetSource
import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.bean.exception.GlobalException
import com.xxhoz.secbox.constant.EventName
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeTabBinding
import com.xxhoz.secbox.eventbus.XEventBus
import com.xxhoz.secbox.module.history.HistoryActivity
import com.xxhoz.secbox.module.home.view.NotificationMsgPopup
import com.xxhoz.secbox.module.search.SearchActivity
import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * 首页
 */
class TabHomeFragment : BaseFragment<FragmentHomeTabBinding>() {

    private val activeColor = Color.parseColor("#ff678f")
    private val normalColor = Color.parseColor("#666666")

    private val activeSize = 18
    private val normalSize = 14

    private var mediator: TabLayoutMediator? = null

    private lateinit var categoryInfo: CategoryBean
    private lateinit var homeVideoList: List<VideoBean>

//    private val viewModel: HomeViewModel by viewModels()

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeTabBinding
        get() = FragmentHomeTabBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 基本事件监听
        initView()
        // 数据加载
        initData()
        // 监听数据源的变化
        XEventBus.observe(viewLifecycleOwner, EventName.SOURCE_CHANGE) { message: String ->
            LogUtils.i("监听到数据变化: " + message)
            initView()
            initData()
        }
    }

    /**
     * 首页顶部信息,公告
     */
    private fun showNotice() {
        val notificationMsgPopup = NotificationMsgPopup(requireContext())
        notificationMsgPopup.setMsg(BaseConfig.NOTION)
        XPopup.Builder(context)
            .isDestroyOnDismiss(true)
            .popupAnimation(PopupAnimation.TranslateFromTop)
            .asCustom(notificationMsgPopup)
            .show()
    }


    private fun initData() {
        Task {
            viewBinding.promptView.showLoading()
            val currentSource: IBaseSource? = withContext(Dispatchers.IO){ BaseConfig.getCurrentSource() }
            currentSource?.run {
                try {
                    // 获取首页数据
                    homeVideoList = onIO{ homeVideoList() } ?: throw GlobalException.of("首页数据为空")
                    // 获取分类数据
                    categoryInfo = onIO{ categoryInfo() }?: throw GlobalException.of("分类数据为空")

                } catch (e: GlobalException) {
                    LogUtils.d("数据源异常:" + e.message)
                    viewBinding.promptView.showEmpty()
                    return@Task
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toaster.show("数据源异常,请切换源")
                    viewBinding.promptView.showNetworkError({
                        initData()
                    })
                    return@Task
                }

                // 初始化tab选项卡
                initViewFragments()
                // 首页顶部信息
                showNotice()
                viewBinding.promptView.hide()
            }
        }
    }


    private fun initView() {
        // 搜索按钮
        viewBinding.searchBtn.setOnClickListener() {
            SearchActivity.startActivity(requireContext())
        }
        // 历史记录
        viewBinding.historyBtn.setOnClickListener() {
            HistoryActivity.startActivity(requireContext())
        }
        // 设置源显示
        val sourceName: String? = BaseConfig.getCurrentSource()?.sourceBean?.name
        viewBinding.currentSourceText.text = (sourceName + "  ▼")

        // 源选择
        viewBinding.currentSourceText.setOnClickListener() {
            XPopup.Builder(context)
                .atView(viewBinding.tabLayout)
                .hasShadowBg(false)
                .asCustom(BottomSheetSource(requireContext()))
                .show()
        }

    }

    private fun initViewFragments() {
        //Adapter
        viewBinding.viewPager.adapter =
            object :
                FragmentStateAdapter(requireActivity().getSupportFragmentManager(), lifecycle) {
                override fun createFragment(position: Int): Fragment {
                    if (position == 0) {
                        // 首页推荐
                        return HomeFragment(homeVideoList);
                    }

                    val classType: CategoryBean.ClassType = categoryInfo.`class`.get(position - 1)
                    val filters: List<CategoryBean.Filter>? =
                        categoryInfo.filters?.get(classType.type_id)
                    // 分类页
                    return HomeFilterFragment(classType, filters)
                }

                override fun getItemCount(): Int {
                    return categoryInfo.`class`.size + 1
                }
            }
        //viewPager 页面切换监听监听
        viewBinding.viewPager.registerOnPageChangeCallback(changeCallback)
        viewBinding.viewPager.offscreenPageLimit = 20
        // tab样式
        mediator = TabLayoutMediator(
            viewBinding.tabLayout, viewBinding.viewPager
        ) { tab, position -> //这里可以自定义TabView
            val tabView = TextView(getActivity())
            val states = arrayOfNulls<IntArray>(2)
            states[0] = intArrayOf(R.attr.state_selected)
            states[1] = intArrayOf()
            val colors = intArrayOf(activeColor, normalColor)
            val colorStateList = ColorStateList(states, colors)
            tabView.text = getCateGoryNameById(position)
            tabView.textSize = normalSize.toFloat()
            tabView.setTextColor(colorStateList)
            tab.customView = tabView
        }
        //要执行这一句才是真正将两者绑定起来
        mediator!!.attach()
    }

    private fun getCateGoryNameById(position: Int): String {
        if (position == 0) {
            return "首页"
        }
        return categoryInfo.`class`.get(position - 1).type_name
    }

    private val changeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            //可以来设置选中时tab的大小
            val tabCount = viewBinding.tabLayout.tabCount
            for (i in 0 until tabCount) {
                val tab = viewBinding.tabLayout.getTabAt(i)
                val tabView = tab!!.customView as TextView?
                if (tab.position == position) {
                    tabView!!.textSize = activeSize.toFloat()
                    tabView.typeface = Typeface.DEFAULT_BOLD
                } else {
                    tabView!!.textSize = normalSize.toFloat()
                    tabView.typeface = Typeface.DEFAULT
                }
            }
        }
    }

    override fun onDestroy() {
        mediator!!.detach()
        viewBinding.viewPager.unregisterOnPageChangeCallback(changeCallback)
        super.onDestroy()
    }

    @PageName
    override fun getPageName() = PageName.HOME

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
