package com.xxhoz.secbox.base

import com.xxhoz.secbox.constant.PageName

/**
 * 获取页面名称通用接口
 */
interface IGetPageName {

    @PageName
    fun getPageName(): String

}
