package com.xxhoz.secbox.module.sniffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentSnifferBinding

/**
 * 自定义嗅探
 */
class SnifferFragment : BaseFragment<FragmentSnifferBinding>() {

    private val viewModel: SnifferViewModel by viewModels()
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentSnifferBinding
        get() = FragmentSnifferBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        viewBinding.run {
            bilibiliLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "https://m.bilibili.com/channel/v/anime")
            }
            tenxunLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "https://m.v.qq.com")
            }
            aiqiyiLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "http://m.iqiyi.com")
            }
            youkuLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "https://www.youku.com")
            }
        }

    }

    @PageName
    override fun getPageName() = PageName.SNIFFER

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
