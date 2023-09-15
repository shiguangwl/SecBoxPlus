package com.xxhoz.parserCore.parserImpl

import com.github.catvod.crawler.Spider
import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.CategoryPageBean
import com.xxhoz.secbox.parserCore.bean.PlayLinkBean
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.parserCore.bean.VideoDetailBean

class JsonSource(override var sourceBean: SourceBean, var spider: Spider)  :
    IBaseSource {
    override fun homeVideoList(): List<VideoBean>? {
        TODO("Not yet implemented")
    }

    override fun categoryInfo(): CategoryBean? {
        TODO("Not yet implemented")
    }

    override fun categoryVideoList(
        tid: String,
        page: String,
        extend: HashMap<String, String>
    ): CategoryPageBean {
        TODO("Not yet implemented")
    }

    override fun videoDetail(ids: List<String>): VideoDetailBean {
        TODO("Not yet implemented")
    }

    override fun playInfo(flag: String, id: String): PlayLinkBean {
        TODO("Not yet implemented")
    }

    override fun searchVideo(kw: String): List<VideoBean> {
        TODO("Not yet implemented")
    }
}
