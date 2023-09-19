package com.xxhoz.secbox.module.sniffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.bean.CustomSiteBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentSnifferBinding
import com.xxhoz.secbox.databinding.ItemCustomBtnBinding
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.StringUtils


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
                WebViewActivity.startActivity(requireContext(), "http://www.bilibili.com/anime")
            }
            tenxunLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "http://v.qq.com")
            }
            aiqiyiLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "http://iqiyi.com")
            }
            youkuLogo.setOnClickListener {
                WebViewActivity.startActivity(requireContext(), "http://www.youku.com")
            }

            floatBtn.setOnClickListener() {
                XPopup.Builder(context).asInputConfirm(
                    "添加站点", "使用@分隔站点和域名\n如: 哔哩哔哩@http://bilibili.com"
                ) { text ->
                    if (StringUtils.isEmpty(text)) {
                        return@asInputConfirm
                    }
                    addCustomClick(text)
                }.show()
            }

        }

        // 获取自定义站点
        loadCustomSite()
    }

    /**
     * 使用@分隔站点和域名
     */
    private fun addCustomClick(text: String) {
        text.split("@").let {
            if (it.size == 2 && !StringUtils.isEmpty(it[0]) && !StringUtils.isEmpty(it[1])) {
                // 如果网站开头不为http则添加http://
                val customSiteBean = CustomSiteBean(it[0], it[1].let { url ->
                    if (!url.startsWith("http")) {
                        "http://$url"
                    } else {
                        url
                    }
                })
                XKeyValue.addObjectList(Key.CUSTOM_SITE, customSiteBean)
                addCustomSiteItem(customSiteBean)
                Toaster.showLong("添加成功")
            } else {
                Toaster.showLong("输入格式错误")
            }
        }
    }

    private fun loadCustomSite() {
        // 判断是否第一次启动
        if (BaseConfig.isFirstStart) {
            for (item in BaseConfig.CONFIG_BEAN.customSite) {
                item.split("@").let {
                    if (it.size == 2) {
                        val customSiteBean = CustomSiteBean(it[0], it[1])
                        XKeyValue.addObjectList(Key.CUSTOM_SITE, customSiteBean)
                    }
                }
            }
        }

        XKeyValue.getObjectList<CustomSiteBean>(Key.CUSTOM_SITE)?.let {
            it.forEach { bean ->
                addCustomSiteItem(bean)
            }
        }
    }

    private fun addCustomSiteItem(it: CustomSiteBean) {
        // 创建 Button
        val inflate = ItemCustomBtnBinding.inflate(layoutInflater)
        val button = inflate.customBtn
        button.tag = it
        button.text = it.siteName
        button.setOnClickListener {
            WebViewActivity.startActivity(
                requireContext(),
                (it.getTag() as CustomSiteBean).siteUrl,
                WebViewActivity.PHONE_AGENT,
                true
            )
        }
        button.setOnLongClickListener {
            XPopup.Builder(context).asConfirm(
                "删除", "是否删除当前站点?"
            ) {
                viewBinding.customSourceBox.removeAllViews()
                XKeyValue.removeObjectList(Key.CUSTOM_SITE, it.getTag() as CustomSiteBean)
                loadCustomSite()
            }.show()

            true
        }

        viewBinding.customSourceBox.addView(inflate.root)
    }

    @PageName
    override fun getPageName() = PageName.SNIFFER

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
