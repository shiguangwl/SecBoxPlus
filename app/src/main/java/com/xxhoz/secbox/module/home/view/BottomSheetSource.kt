package com.xxhoz.secbox.module.home.view

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xxhoz.secbox.R
import com.xxhoz.secbox.util.DipAndPx
import com.xxhoz.secbox.util.GlobalActivityManager.getTopActivity

class BottomSheetSource : BottomSheetDialogFragment {
    private var view: View? = null

    constructor(view: View?) {
        this.view = view
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.sheet_bottom_sourace_layout, container, false)
        view.let {
            val layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                DipAndPx.px2dip(getTopActivity()!!,getDistanceToBottom(getTopActivity()!!, view!!).toFloat())
            )
            inflate.rootView.layoutParams = layoutParams
        }
        return inflate
    }

    private fun getDistanceToBottom(context: Activity, view: View): Int {
        val rootView = context.findViewById<ViewGroup>(android.R.id.content)
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val viewBottom = getViewBottomOnScreen(view)
        return screenHeight - viewBottom
    }

    private fun getViewBottomOnScreen(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[1] + view.height
    }
}
