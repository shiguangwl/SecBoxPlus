package com.xxhoz.parserCore.parserImpl

import com.github.catvod.crawler.Spider
import com.xxhoz.secbox.parserCore.bean.SourceBean

class SpiderSource(var source: SourceBean, var spider: Spider)  :
    IBaseSource {

    // 获取首页视频列表,赛选条件
    override fun homeVideoList(): String {
        return spider.homeVideoContent()
    }

    /**
     * 获取分类信息
     */
    override fun categoryInfo(): String {
        return spider.homeContent(true)
    }

    /**
     * 获取分类数据
     */
    override fun categoryVideoList(tid: String, page: String, extend: HashMap<String, String>): String {

        var filter: Boolean = false;
        if (extend != null && extend.size > 0) {
            filter = true;
        }

        return spider.categoryContent(tid, page, filter, extend)
    }


    /**
     * 获取影视详情
     */
    override fun videoDetail(ids:List<String>): String {
        return spider.detailContent(ids);
    }


    /**
     * 获取播放链接
     */
    override fun playInfo(flag:String, id:String):String{
        return spider.playerContent(flag,id,source.flags)
    }

    /**
     * 影视搜索
     */
    override fun searchVideo(kw:String): String{
        return spider.searchContent(kw,source.isQuickSearch)
    }


}
