package com.xxhoz.secbox.parserCore.bean


/**
 * 分类信息
 */
data class CategoryPageBean(
    val page: Int,
    val pagecount: Int,
    val limit: Int,
    val total: Int,
    val list: List<VideoBean>
)
