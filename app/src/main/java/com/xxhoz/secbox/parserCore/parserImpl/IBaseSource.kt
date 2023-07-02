package com.xxhoz.parserCore.parserImpl

interface IBaseSource {

    /**
     * 获取首页视频列表,赛选条件
     */
    fun homeVideoList(): String

    /**
     * 获取分类信息
     */
    fun categoryInfo(): String

    /**
     * 获取分类数据
     */
    fun categoryVideoList(tid: String, page: String, extend: HashMap<String, String>): String


    /**
     * 获取影视详情
     */
    fun videoDetail(ids: List<String>): String

    /**
     * 获取播放链接
     */
    fun playInfo(flag: String, id: String): String

    /**
     * 影视搜索
     */
    fun searchVideo(kw: String): String

}
