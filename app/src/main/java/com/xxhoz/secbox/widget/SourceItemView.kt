package com.xxhoz.secbox.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.xxhoz.secbox.R
import com.xxhoz.secbox.bean.SubscribeBean
import com.xxhoz.secbox.databinding.ViewSourceBinding

class SourceItemView @JvmOverloads constructor(
    context: Context, val subBean: SubscribeBean, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val viewBinding = ViewSourceBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        viewBinding.sourceNameTextview.text = subBean.name
        viewBinding.sourceUrlTextview.text = subBean.url
        viewBinding.cancelImageview.tag = subBean
    }

    fun setOnRemoveListener(callback: (view: View) -> Boolean) {
        viewBinding.cancelImageview.setOnClickListener(){
            if (callback(it)) {
                // 从父view中删除当前view
                (parent as LinearLayout).removeView(this)
            }
        }
    }


    fun setSelect(isSelect: Boolean) {
        if (isSelect){
            viewBinding.selectActiveImageview.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.icon_select_on))
        }else{
            viewBinding.selectActiveImageview.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.icon_select_off))
        }
    }
}
