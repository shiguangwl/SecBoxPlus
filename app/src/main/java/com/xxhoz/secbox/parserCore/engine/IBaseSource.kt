package com.xxhoz.parserCore.parserImpl

import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.CategoryPageBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean

interface IBaseSource {

    var sourceBean:SourceBean


    /**
     * 获取首页视频列表,赛选条件
     */
    @Throws(Exception::class)
    fun homeVideoList(): List<VideoBean>?

    /**
     * 获取分类信息
     */
    @Throws(Exception::class)
    fun categoryInfo(): CategoryBean?

    /**
     * 获取分类数据
     */
    @Throws(Exception::class)
    fun categoryVideoList(tid: String, page: String, extend: HashMap<String, String>): CategoryPageBean


    /**
     * 获取影视详情
     */
    @Throws(Exception::class)
    fun videoDetail(ids: List<String>): VideoDetailBean?

    /**
     * 获取播放链接
     */
    @Throws(Exception::class)
    fun playInfo(flag: String, id: String): PlayLinkBean

    /**
     * 影视搜索
     */
    @Throws(Exception::class)
    fun searchVideo(kw: String): List<VideoBean>

}
