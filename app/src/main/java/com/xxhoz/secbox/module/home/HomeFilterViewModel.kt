package com.xxhoz.secbox.module.home

import androidx.lifecycle.viewModelScope
import com.xxhoz.constant.BaseConfig
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.base.list.base.BaseRecyclerViewModel
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.item.VideoViewData
import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.CategoryPageBean
import com.xxhoz.secbox.util.LogUtils
import kotlinx.coroutines.launch

class HomeFilterViewModel : BaseRecyclerViewModel() {

    var currentPageNum = 1

    lateinit var category: CategoryBean.ClassType
    lateinit var conditons: HashMap<String,String>

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        viewModelScope.launch() {
            val viewDataList = mutableListOf<BaseViewData<*>>()

            val currentSource: IBaseSource = BaseConfig.getCurrentSource()!!
            val categoryVideoList: CategoryPageBean = currentSource.categoryVideoList(category.type_id, currentPageNum.toString(), conditons)
            LogUtils.d("按条件检索数据:" + categoryVideoList)
            categoryVideoList.list.forEach {
                viewDataList.add(VideoViewData(it))
            }
            postData(isLoadMore, viewDataList)
            currentPageNum++
        }
    }

    fun loadData() {
//        val viewDataList = mutableListOf<BaseViewData<*>>()
//        val videoBean = VideoBean("1", "我是标题", "https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2885057891.webp", "aaa")
//        for (i in 0..19) {
//            viewDataList.add(VideoViewData(videoBean))
//        }
//        postData(false, viewDataList)
    }


    @PageName
    override fun getPageName() = PageName.FILTER
}
