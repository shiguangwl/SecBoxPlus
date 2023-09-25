package com.xxhoz.secbox.module.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.lxj.xpopup.core.BasePopupView
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.SubscribeBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityCustomSourceBinding
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.widget.SourceItemView


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
            var popview: BasePopupView? = null

//            popview = XPopup.Builder(this)
//                .isDestroyOnDismiss(true)
//                .asCustom(CustomPopup(this) {
//                    SingleTask {
//                        val loadding = XPopup.Builder(getActivity())
//                            .isDestroyOnDismiss(true)
//                            .dismissOnBackPressed(false)
//                            .dismissOnTouchOutside(false)
//                            .asLoading("正在加载中...")
//                        try {
//                            loadding.show()
//                            onIO2 { SourceManger.loadSourceConfig(it.url) }
//                            loadding.dismiss()
//                            // 加载成功
//                            XKeyValue.addObjectList(Key.SUBSCRIBE_LIST, it)
//                            XKeyValue.putString(Key.CURRENT_SOURCE_URL, it.url)
//                            viewList.forEach {
//                                it.setSelect(false)
//                            }
//                            addSubItem(it, true)
//                            popview?.dismiss()
//                            Toaster.show("添加订阅成功")
//                            // 发送订阅变化事件
//                            runOnUiThread {
//                                XEventBus.post(EventName.SOURCE_CHANGE, "源切换: ${it.name}")
//                            }
//                        } catch (e: Exception) {
//                            loadding.dismiss()
//                            Toaster.show("加载订阅[${it.name}]失败")
//                            e.printStackTrace()
//                        }
//                    }
//                })

//            popview.show()
        }
    }

    /**
     * 是否活动的订阅
     */
    private fun isSelect(
        subscribeBean: SubscribeBean,
        currentSub: String
    ) = subscribeBean.url.equals(currentSub)


    /**
     * 点解切换源逻辑
     */
    private fun onClick(it: View) {
        val subUrl = (it.tag as SubscribeBean).url
        val subLast = XKeyValue.getString(Key.CURRENT_SOURCE_URL, BaseConfig.BASE_SOURCE_URL)
//        SingleTask {
//            val loadding = XPopup.Builder(getActivity())
//                .isDestroyOnDismiss(true)
//                .dismissOnBackPressed(false)
//                .dismissOnTouchOutside(false)
//                .asLoading("切换订阅中...")
//
//            loadding.show()
//            try {
//                onIO2 { SourceManger.loadSourceConfig(subUrl) }
//                Toaster.show("切换订阅成功,当前订阅: ${(it.tag as SubscribeBean).name}")
//                viewList.forEach {
//                    it.setSelect(false)
//                }
//                (it as SourceItemView).setSelect(true)
//                XKeyValue.putString(Key.CURRENT_SOURCE_URL, subUrl)
//                // 发送订阅变化事件
//
//                XEventBus.post(EventName.SOURCE_CHANGE, "源切换: ${(it.tag as SubscribeBean).name}")
//
//            } catch (e: Exception) {
//                Toaster.show("加载订阅[${(it.tag as SubscribeBean).name}]失败,请切换订阅")
//                SourceManger.loadSourceConfig(subLast)
//                e.printStackTrace()
//            }
//            loadding.dismiss()
//
//        }

    }


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


    override fun getPageName(): String = PageName.CUSTOM_SOURCE
}
