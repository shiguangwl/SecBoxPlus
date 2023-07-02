package com.xxhoz.secbox.bean

import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.constant.TabId
import kotlin.reflect.KClass

data class Tab(
    @TabId
    val id: String,
    val title: String,
    val icon: Int,
    val fragmentClz: KClass<out BaseFragment<*>>
)
