package com.xxhoz.secbox.module.home

import android.graphics.PostProcessor
import androidx.lifecycle.viewModelScope
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.base.list.base.BaseRecyclerViewModel
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.bean.BannerBean
import com.xxhoz.secbox.bean.VideoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.constant.VideoType
import com.xxhoz.secbox.item.BannerViewData
import com.xxhoz.secbox.item.LargeVideoViewData
import com.xxhoz.secbox.item.VideoViewData
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFilterViewModel : BaseRecyclerViewModel() {

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        if (isLoadMore){
            val viewDataList = mutableListOf<BaseViewData<*>>()
            postData(true, viewDataList)
        }
    }

    fun loadData(){
        val viewDataList = mutableListOf<BaseViewData<*>>()
        val videoBean = VideoBean("1", "我是标题", "https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2885057891.webp", "aaa", "up", 10000L, VideoType.NORMAL)
        for (i in 0..19) {
            viewDataList.add(VideoViewData(videoBean))
        }
        postData(false, viewDataList)
    }


    @PageName
    override fun getPageName() = PageName.FILTER
}