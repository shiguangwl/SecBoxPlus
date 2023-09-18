package com.xxhoz.secbox.module.sniffer.view

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.impl.PartShadowPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.xxhoz.secbox.R

/**
 * Description: 自定义局部阴影弹窗
 * @author DengNanYu
 */
class CustomPartShadowPopupView(context: Context,val ccallback:(rc:RecyclerView)->Unit) : PartShadowPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.custom_part_shadow_popup
    }

    override fun onCreate() {
        super.onCreate()
        val recyclerDatView = findViewById<View>(R.id.recycler_data_view) as RecyclerView
        ccallback(recyclerDatView)
    }

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getAppHeight(context) * .4f).toInt()
    }
}
