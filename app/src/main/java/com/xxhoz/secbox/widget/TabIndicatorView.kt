package com.xxhoz.secbox.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.xxhoz.secbox.databinding.ViewTabIndicatorBinding

class TabIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val viewBinding = ViewTabIndicatorBinding.inflate(LayoutInflater.from(context), this, true)
}
