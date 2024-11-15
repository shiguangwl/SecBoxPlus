package com.xxhoz.secbox.module.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.SubscribeBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityCustomSourceBinding
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.getActivity
import com.xxhoz.secbox.widget.CustomPopup
import com.xxhoz.secbox.widget.SourceItemView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CustomSourceActivity : BaseActivity<ActivityCustomSourceBinding>() {

    override val inflater: (inflater: LayoutInflater) -> ActivityCustomSourceBinding
        get() = ActivityCustomSourceBinding::inflate

    var viewList: ArrayList<SourceItemView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }

        val currentSub = XKeyValue.getString(Key.CURRENT_SOURCE_URL, BaseConfig.BASE_SOURCE_URL)

        // 添加默认订阅
        val subscribeBean = SubscribeBean("默认订阅", BaseConfig.BASE_SOURCE_URL)
        addSubItem(subscribeBean, isSelect(subscribeBean, currentSub), true)

        // 添加自定义订阅
        val subscribeBeans: ArrayList<SubscribeBean>? = XKeyValue.getObjectList(Key.SUBSCRIBE_LIST)
        subscribeBeans?.let {
            for (bean in it) {
                addSubItem(bean, isSelect(bean, currentSub))
            }
        }

        // 添加订阅弹窗
        viewBinding.addTextview.setOnClickListener {
            onClickAddSub()
        }
    }


    /**
     * 添加订阅
     */
    private fun onClickAddSub() {
        var popview: BasePopupView? = null

        popview = XPopup.Builder(this)
            .isDestroyOnDismiss(true)
            .asCustom(CustomPopup(this) {
                SingleTask(lifecycleScope.launch {
                    val loadding = XPopup.Builder(getActivity())
                        .isDestroyOnDismiss(true)
                        .dismissOnBackPressed(false)
                        .dismissOnTouchOutside(false)
                        .asLoading("正在加载中...")
                    try {

                        loadding.show()
                        withContext(IO) {
                            BaseConfig.changeSubscribe(it.url)
                        }
                        XKeyValue.addObjectList(Key.SUBSCRIBE_LIST, it)
                        for (sourceItemView in viewList) {
                            sourceItemView.setSelect(false)
                        }
                        addSubItem(it, true)
                        popview?.dismiss()
                        Toaster.show("添加订阅成功")
                        loadding.dismiss()
                    } catch (e: Exception) {
                        loadding.dismiss()
                        Toaster.show("加载解析订阅 [${it.name}] 失败")
                        LogUtils.e("加载解析订阅 [${it.name}] 失败", e)
                    }
                })
            })

        popview.show()
    }


    /**
     * 点解切换源逻辑
     */
    private fun onClick(it: View) {
        var subUrl = (it.tag as SubscribeBean).url
//        if (StringUtils.isEmpty(subUrl) && (it.tag as SubscribeBean).name.equals("默认订阅")) {
//            subUrl = BaseConfig.BASE_SOURCE_URL
//        }
        val lastSub = XKeyValue.getString(Key.CURRENT_SOURCE_URL, BaseConfig.BASE_SOURCE_URL)
        lifecycleScope.launch {
            val loadding = XPopup.Builder(getActivity())
                .isDestroyOnDismiss(true)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .asLoading("切换订阅中...")

            loadding.show()
            try {
                withContext(IO) {
                    BaseConfig.changeSubscribe(subUrl)
                }
                Toaster.show("切换订阅成功,当前订阅: ${(it.tag as SubscribeBean).name}")
                for (sourceItemView in viewList) {
                    sourceItemView.setSelect(false)
                }
                (it as SourceItemView).setSelect(true)

            } catch (e: Exception) {
                withContext(IO) {
                    BaseConfig.changeSubscribe(lastSub)
                }
                Toaster.show("加载订阅失败[${(it.tag as SubscribeBean).name}]")
                LogUtils.e("加载订阅失败[${(it.tag as SubscribeBean).name}]失败:", e)
            }
            loadding.dismiss()

        }
    }


    /**
     * 添加订阅项
     * @param subscribeBean 订阅项
     * @param isSelect 是否选中
     * @param isDefault 是否是默认订阅
     */
    @MainThread
    private fun addSubItem(
        subscribeBean: SubscribeBean,
        isSelect: Boolean = false,
        isDefault: Boolean = false
    ) {
        val sourceItemView = SourceItemView(this, subscribeBean)
        sourceItemView.tag = subscribeBean
        sourceItemView.setSelect(isSelect)
        sourceItemView.setOnRemoveListener {
            // 默认和当前使用的无法删除
            if (isDefault) {
                Toaster.showLong("无法删除默订阅")
                return@setOnRemoveListener false
            } else {
                XKeyValue.removeObjectList(Key.SUBSCRIBE_LIST, subscribeBean)
                Toaster.show("删除成功")
                return@setOnRemoveListener true
            }
        }

        sourceItemView.setOnClickListener {
            onClick(it)
        }
        viewBinding.itemGroup.addView(sourceItemView)
        viewList.add(sourceItemView)
    }


    /**
     * 是否活动的订阅
     */
    private fun isSelect(
        subscribeBean: SubscribeBean,
        currentSub: String
    ) = subscribeBean.url.equals(currentSub)

    override fun getPageName(): String = PageName.CUSTOM_SOURCE
}
