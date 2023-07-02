package com.xxhoz.secbox.module.main

import com.xxhoz.secbox.base.BaseViewModel
import com.xxhoz.secbox.constant.PageName

class MainViewModel : BaseViewModel() {

    @PageName
    override fun getPageName() = PageName.HOME
}
