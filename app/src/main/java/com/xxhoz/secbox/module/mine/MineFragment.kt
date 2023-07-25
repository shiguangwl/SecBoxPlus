package com.xxhoz.secbox.module.mine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentMineBinding
import com.xxhoz.secbox.module.about.AboutActivity
import com.xxhoz.secbox.persistence.XKeyValue

/**
 * 我的
 */
class MineFragment : BaseFragment<FragmentMineBinding>() {

    private val viewModel: MineViewModel by viewModels()
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentMineBinding
        get() = FragmentMineBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        // 历史记录
        viewBinding.itemHistory.setOnClickListener {

        }

        // 收藏
        viewBinding.itemCollection.setOnClickListener {

        }

        // 订阅
        viewBinding.itemSubscribe.setOnClickListener {

        }

        // 设置
        viewBinding.itemSetting.setOnClickListener {

        }

        // 关于
        viewBinding.itemAbout.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java);
            startActivity(intent)
        }

        // 清理缓存
        viewBinding.itemClearCache.setOnClickListener {
            Toast.makeText(activity, "清理缓存成功", Toast.LENGTH_SHORT).show()
            XKeyValue.clearAccountMMKV()

        }

    }

    @PageName
    override fun getPageName() = PageName.MINE

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
