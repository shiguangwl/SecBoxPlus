package com.xxhoz.secbox.module.home

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
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeTabBinding
import com.xxhoz.secbox.module.home.view.BottomSheetSource
import com.xxhoz.secbox.module.search.SearchActivity


/**
 * 首页
 */
class TabHomeFragment : BaseFragment<FragmentHomeTabBinding>() {

    private val activeColor = Color.parseColor("#ff678f")
    private val normalColor = Color.parseColor("#666666")

    private val activeSize = 18
    private val normalSize = 14

    private var mediator: TabLayoutMediator? = null

//    private val viewModel: HomeViewModel by viewModels()

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeTabBinding
        get() = FragmentHomeTabBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {

        viewBinding.searchBtn.setOnClickListener(){
            SearchActivity.startActivity(requireContext())
        }

        viewBinding.currentSourceText.setOnClickListener(){
            val bottomSheetFragment = BottomSheetSource(viewBinding.searchBtn)
            bottomSheetFragment.show(requireActivity().getSupportFragmentManager(), bottomSheetFragment.getTag())
        }

        viewBinding.promptView.showLoading()

        Thread{
            Thread.sleep(1500)
            getActivity()?.runOnUiThread(){
                val tabs =
                    arrayOf("推荐", "电影", "电视剧", "动漫", "综艺")
                //禁用预加载
//                viewBinding.viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                viewBinding.viewPager.offscreenPageLimit = 10

                //Adapter
                viewBinding.viewPager.adapter =
                    object : FragmentStateAdapter((getActivity()?.getSupportFragmentManager())!!, lifecycle) {
                        override fun createFragment(position: Int): Fragment {
                            //FragmentStateAdapter内部自己会管理已实例化的fragment对象。
                            // 所以不需要考虑复用的问题
                            if (position == 0) {
                                return HomeFragment();
                            }
                            return HomeFilterFragment(tabs[position])
                        }

                        override fun getItemCount(): Int {
                            return tabs.size
                        }
                    }
                //viewPager 页面切换监听监听
                viewBinding.viewPager.registerOnPageChangeCallback(changeCallback)

                mediator = TabLayoutMediator(
                    viewBinding.tabLayout, viewBinding.viewPager
                ) { tab, position -> //这里可以自定义TabView
                    val tabView = TextView(getActivity())
                    val states = arrayOfNulls<IntArray>(2)
                    states[0] = intArrayOf(R.attr.state_selected)
                    states[1] = intArrayOf()
                    val colors = intArrayOf(activeColor, normalColor)
                    val colorStateList = ColorStateList(states, colors)
                    tabView.text = tabs[position]
                    tabView.textSize = normalSize.toFloat()
                    tabView.setTextColor(colorStateList)
                    tab.customView = tabView
                }
                //要执行这一句才是真正将两者绑定起来
                mediator!!.attach()

                viewBinding.promptView.hide()
            }
        }.start()
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
