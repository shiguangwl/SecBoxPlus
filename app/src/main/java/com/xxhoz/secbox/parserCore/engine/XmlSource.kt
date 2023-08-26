package com.xxhoz.parserCore.parserImpl

import com.github.catvod.crawler.Spider
import com.xxhoz.secbox.parserCore.bean.SourceBean

class XmlSource(var source: SourceBean, var spider: Spider)  :
    IBaseSource {
    override fun homeVideoList(): String {
        TODO("Not yet implemented")
    }

    override fun categoryInfo(): String {
        TODO("Not yet implemented")
    }

    override fun categoryVideoList(
        tid: String,
        page: String,
        extend: HashMap<String, String>
    ): String {
        TODO("Not yet implemented")
    }

    override fun videoDetail(ids: List<String>): String {
        TODO("Not yet implemented")
    }

    override fun playInfo(flag: String, id: String): String {
        TODO("Not yet implemented")
    }

    override fun searchVideo(kw: String): String {
        TODO("Not yet implemented")
    }
}
