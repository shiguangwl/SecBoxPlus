package com.xxhoz.secbox.module.home

import com.xxhoz.secbox.base.list.base.BaseRecyclerViewModel
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.item.VideoViewData
import com.xxhoz.secbox.parserCore.bean.VideoBean

class HomeFilterViewModel : BaseRecyclerViewModel() {

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        if (isLoadMore){
            val viewDataList = mutableListOf<BaseViewData<*>>()
            postData(true, viewDataList)
        }
    }

    fun loadData(){
        val viewDataList = mutableListOf<BaseViewData<*>>()
        val videoBean = VideoBean("1", "我是标题", "https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2885057891.webp", "aaa")
        for (i in 0..19) {
            viewDataList.add(VideoViewData(videoBean))
        }
        postData(false, viewDataList)
    }


    @PageName
    override fun getPageName() = PageName.FILTER
}
