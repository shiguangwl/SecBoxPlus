package com.xxhoz.secbox.module.home

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

class HomeViewModel : BaseRecyclerViewModel() {

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
//            delay(1000L)
            val viewDataList = mutableListOf<BaseViewData<*>>()
            if (!isLoadMore) {
                // 第一次加载
                LogUtils.i("开始加载数据...")
                val currentSource: IBaseSource? = BaseConfig.getCurrentSource()
                currentSource?.let {
                    val homeVideoList = it.homeVideoList()

                    val videoBean = VideoBean("aaa", homeVideoList, "xxx", "aaa", "up", 10000L, VideoType.NORMAL)
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                    viewDataList.add(LargeVideoViewData(videoBean))
                }

//                viewDataList.add(BannerViewData(BannerBean(listOf("https://img1.baidu.com/it/u=2148838167,3055147248&fm=26&fmt=auto", "https://img1.baidu.com/it/u=2758621636,2239499009&fm=26&fmt=auto", "https://img2.baidu.com/it/u=669799662,2628491047&fm=26&fmt=auto"))))
//                for (i in 0..10) {
//                    if (i != 0 && i % 6 == 0) {
//                        viewDataList.add(LargeVideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.LARGE)))
//                    } else {
//                        viewDataList.add(VideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.NORMAL)))
//                    }
//                }
                postData(isLoadMore, viewDataList)
            } else {
//                // 加载更多
//                for (i in 0..10) {
//                    if (i != 0 && i % 6 == 0) {
//                        viewDataList.add(LargeVideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.LARGE)))
//                    } else {
//                        viewDataList.add(VideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.NORMAL)))
//                    }
//                }
//                postData(isLoadMore, viewDataList)
                postData(isLoadMore, viewDataList)
            }
        }
    }


    fun loadHomeDataList(){

    }



    @PageName
    override fun getPageName() = PageName.HOME
}
