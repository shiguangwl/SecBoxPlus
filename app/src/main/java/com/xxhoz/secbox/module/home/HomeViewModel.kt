package com.xxhoz.secbox.module.home

import com.xxhoz.secbox.base.list.base.BaseRecyclerViewModel
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.item.VideoViewData
import com.xxhoz.secbox.parserCore.bean.VideoBean

class HomeViewModel : BaseRecyclerViewModel() {


    fun loadData(homeVideoList: List<VideoBean>){
        val viewDataList = mutableListOf<BaseViewData<*>>()

//        val bannerBean = BannerBean(BaseConfig.CONFIG_BEAN.banner)
//        viewDataList.add(BannerViewData(bannerBean))

        for (videoBean in homeVideoList) {
            viewDataList.add(VideoViewData(videoBean))
        }

        postData(false, viewDataList)
    }

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        val viewDataList = mutableListOf<BaseViewData<*>>()
        postData(isLoadMore, viewDataList)

//        viewModelScope.launch(Dispatchers.IO) {
//            val viewDataList = mutableListOf<BaseViewData<*>>()
//            postData(isLoadMore, viewDataList)
//            if (!isLoadMore) {
//                // 第一次加载或刷新
//                val currentSource: IBaseSource? = BaseConfig.getCurrentSource()
//                currentSource?.let {
//                    // 首页banner
//                    val bannerBean = BannerBean(listOf("https://img9.doubanio.com/view/photo/s_ratio_poster/public/p2656327176.webp","https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2885057891.webp"))
//                    viewDataList.add(BannerViewData(bannerBean))
//
//                    var homeVideoList: List<VideoBean> = it.homeVideoList()
//                    for (videoBean in homeVideoList) {
//                        viewDataList.add(VideoViewData(videoBean))
//                    }
//                }
//
////                viewDataList.add(BannerViewData(BannerBean(listOf("https://img1.baidu.com/it/u=2148838167,3055147248&fm=26&fmt=auto", "https://img1.baidu.com/it/u=2758621636,2239499009&fm=26&fmt=auto", "https://img2.baidu.com/it/u=669799662,2628491047&fm=26&fmt=auto"))))
////                for (i in 0..10) {
////                    if (i != 0 && i % 6 == 0) {
////                        viewDataList.add(LargeVideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.LARGE)))
////                    } else {
////                        viewDataList.add(VideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.NORMAL)))
////                    }
////                }
//                postData(isLoadMore, viewDataList)
//            } else {
////                // 加载更多
////                for (i in 0..10) {
////                    if (i != 0 && i % 6 == 0) {
////                        viewDataList.add(LargeVideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.LARGE)))
////                    } else {
////                        viewDataList.add(VideoViewData(VideoBean("aaa", "我是标题", "xxx", "aaa", "up", 10000L, VideoType.NORMAL)))
////                    }
////                }
////                postData(isLoadMore, viewDataList)
//                postData(isLoadMore, viewDataList)
//            }
//        }
    }


    @PageName
    override fun getPageName() = PageName.HOME
}
