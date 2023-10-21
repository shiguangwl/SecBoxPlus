package com.xxhoz.secboxbrowser.popview

import android.app.Activity
import com.lxj.xpopup.impl.PartShadowPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.xxhoz.secboxbrowser.adapter.UniversalAdapter
import com.xxhoz.snifferwebview.R


/**
 * Description: 自定义局部阴影弹窗
 * @author DengNanYu
 */
class MenuPopupView(activity: Activity) : PartShadowPopupView(activity) {

    override fun getImplLayoutId(): Int {
        return R.layout.menu_popup
    }

    private var universalAdapter: UniversalAdapter<String>? = null

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getAppHeight(context) * .4f).toInt()
    }

}

