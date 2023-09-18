package com.xxhoz.secbox.module.mine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.xxhoz.constant.BaseConfig
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentMineBinding
import com.xxhoz.secbox.module.about.AboutActivity
import com.xxhoz.secbox.module.history.HistoryActivity
import com.xxhoz.secbox.module.home.view.NotificationMsgPopup
import java.io.File


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
            HistoryActivity.startActivity(requireContext())
        }

        // 收藏
        viewBinding.itemCollection.setOnClickListener {
            showNotice()
        }

        // 订阅
        viewBinding.itemSubscribe.setOnClickListener {
            showNotice()
        }

        // 设置
        viewBinding.itemSetting.setOnClickListener {
            showNotice()
        }

        // 关于
        viewBinding.itemAbout.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java);
            startActivity(intent)
        }

        // 清理缓存
        viewBinding.itemClearCache.setOnClickListener {
            // 清理Android缓存
            Toaster.show("清理缓存成功")
        }

    }


    // 清除应用缓存
    fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.isDirectory) {
                val children = cacheDir.list()
                for (child in children) {
                    deleteDir(File(cacheDir, child))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 递归删除文件夹及其内容
    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
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
    @PageName
    override fun getPageName() = PageName.MINE

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
