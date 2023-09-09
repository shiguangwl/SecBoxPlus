package com.xxhoz.secbox.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.cancel
import me.imid.swipebacklayout.lib.app.SwipeBackActivity

/**
 * Activity基类
 */
abstract class BaseActivity<T : ViewBinding> : SwipeBackActivity(), IGetPageName {

    protected lateinit var viewBinding: T
    protected abstract val inflater: (inflater: LayoutInflater) -> T

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
        setSwipeBackEnable(swipeBackEnable())
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        // 这里可以添加页面打点
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        // 这里可以添加页面打点
    }

    @CallSuper
    override fun onDestroy() {
        lifecycleScope.cancel()
        super.onDestroy()
    }

    /**
     * 默认开启左滑返回,如果需要禁用,请重写此方法
     */
    protected open fun swipeBackEnable() = true



}
