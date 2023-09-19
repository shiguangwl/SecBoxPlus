package com.xxhoz.secbox.widget

import android.content.Context
import android.view.View
import android.widget.EditText
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.core.CenterPopupView
import com.xxhoz.secbox.R
import com.xxhoz.secbox.bean.SubscribeBean

class CustomPopup(context: Context,val callback: (subscribeBean: SubscribeBean)->Unit) : CenterPopupView(context) {
    // 返回自定义弹窗的布局
    override fun getImplLayoutId(): Int {
        return R.layout.custom_popup
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    override fun onCreate() {
        super.onCreate()

        val subNmae: EditText = findViewById<EditText>(R.id.et_input1)
        val subUrl: EditText = findViewById<EditText>(R.id.et_input2)

        findViewById<View>(R.id.tv_cancel).setOnClickListener {
            dismiss() // 关闭弹窗
        }
        findViewById<View>(R.id.tv_confirm).setOnClickListener {
            val subscribeBean = SubscribeBean(subNmae.text.toString(), subUrl.text.toString())
            callback(subscribeBean)
        }
    }

    // 设置最大宽度，看需要而定，
    override fun getMaxWidth(): Int {
        return super.getMaxWidth()
    }

    // 设置最大高度，看需要而定
    override fun getMaxHeight(): Int {
        return super.getMaxHeight()
    }

    // 设置自定义动画器，看需要而定
    override fun getPopupAnimator(): PopupAnimator {
        return super.getPopupAnimator()
    }

    /**
     * 弹窗的宽度，用来动态设定当前弹窗的宽度，受getMaxWidth()限制
     *
     * @return
     */
    override fun getPopupWidth(): Int {
        return 0
    }

    /**
     * 弹窗的高度，用来动态设定当前弹窗的高度，受getMaxHeight()限制
     *
     * @return
     */
    override fun getPopupHeight(): Int {
        return 0
    }
}
