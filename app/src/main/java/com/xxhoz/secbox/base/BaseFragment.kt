package com.xxhoz.secbox.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope


/**
 * Fragment基类
 */
abstract class BaseFragment<T : ViewBinding> : Fragment(), IGetPageName,TaskManger {

    protected lateinit var viewBinding: T
    protected abstract val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> T
    protected lateinit var activity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = requireActivity()
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = inflater(inflater, container, false)
        return viewBinding.root
    }

    @CallSuper
    override fun onDestroyView() {
        clearTask()
        super.onDestroyView()
    }

    override fun getScope(): CoroutineScope = lifecycleScope
}
