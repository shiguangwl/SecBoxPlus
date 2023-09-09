package com.xxhoz.parserCore.parserImpl

import com.github.catvod.crawler.Spider
import com.hjq.gson.factory.GsonFactory
import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.CategoryPageBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean
import com.xxhoz.secbox.util.LogUtils
import org.json.JSONObject

class SpiderSource(override var sourceBean: SourceBean, var spider: Spider)  :
    IBaseSource {

    private val gson = GsonFactory.getSingletonGson()

    /**
     * 获取首页视频列表,赛选条件
     */
    override fun homeVideoList(): List<VideoBean> {
        val result = ArrayList<VideoBean>()
        var homeVideoContent = spider.homeVideoContent()
        if (homeVideoContent.length == 0){
            homeVideoContent = spider.homeContent(true)
        }
        LogUtils.d(sourceBean.name +"首页数据String:${homeVideoContent}")
        val jsonobject = gson.fromJson(homeVideoContent, JSONObject::class.java)
        val jsonArray = jsonobject.getJSONArray("list")
        for (i in 0..jsonArray.length()-1) {
            val item = gson.fromJson(jsonArray.get(i).toString(),VideoBean::class.java)
            result.add(item)
        }
        return result
    }

    /**
     * 获取分类信息
     */
    override fun categoryInfo(): CategoryBean {
        val homeContent = spider.homeContent(true)
        LogUtils.d(sourceBean.name +"分类信息数据String:${homeContent}")
        val fromJson = gson.fromJson(homeContent, CategoryBean::class.java)
        return fromJson
    }

    /**
     * 获取分类数据
     */
    override fun categoryVideoList(tid: String, page: String, extend: HashMap<String, String>): CategoryPageBean {
        var filter: Boolean = false;
        if (extend.size > 0) {
            filter = true;
        }
        var categoryContent = spider.categoryContent(tid, page, filter, extend)
        LogUtils.d(sourceBean.name +"分类条件过滤数据String:${categoryContent}")
        return gson.fromJson(categoryContent,CategoryPageBean::class.java)
    }


    /**
     * 获取影视详情
     */
    override fun videoDetail(ids:List<String>): VideoDetailBean? {
        val videoDetail: String =  spider.detailContent(ids)
        LogUtils.d(sourceBean.name +"影视详情数据String:${videoDetail}")
        if (videoDetail.length == 0){
            return null;
        }
        val fromJson = gson.fromJson(videoDetail, JSONObject::class.java)
        val jsonArray = fromJson.getJSONArray("list")
        var videoDetailBean: VideoDetailBean = gson.fromJson(jsonArray.getJSONObject(0).toString(),
            VideoDetailBean::class.java)
        return videoDetailBean;
    }


    /**
     * 获取播放链接
     */
    override fun playInfo(flag:String, id:String):PlayLinkBean{
        val playerContent: String = spider.playerContent(flag, id, sourceBean.flags)
        LogUtils.d(sourceBean.name +"    播放链接数据String:${playerContent}")
        return gson.fromJson(playerContent, PlayLinkBean::class.java)
    }

    /**
     * 影视搜索
     */
    override fun searchVideo(kw:String): List<VideoBean> {
        val result = ArrayList<VideoBean>()
        var searchRsult = spider.searchContent(kw,sourceBean.isQuickSearch)
        if (searchRsult.length == 0){
            searchRsult = spider.searchContent(kw,!sourceBean.isQuickSearch)
        }
        LogUtils.d(sourceBean.name +"=>搜索数据String:${searchRsult}")
        val jsonobject = gson.fromJson(searchRsult, JSONObject::class.java)
        val jsonArray = jsonobject.getJSONArray("list")
        for (i in 0..jsonArray.length()-1) {
            val item = gson.fromJson(jsonArray.get(i).toString(),VideoBean::class.java)
            result.add(item)
        }
        return result
    }


}
