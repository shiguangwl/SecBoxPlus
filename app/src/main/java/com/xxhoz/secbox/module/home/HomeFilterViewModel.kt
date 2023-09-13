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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFilterViewModel : BaseRecyclerViewModel() {



    lateinit var category: CategoryBean.ClassType
    lateinit var conditons: HashMap<String, String>

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val viewDataList = mutableListOf<BaseViewData<*>>()
                LogUtils.d("type_id:" + category.type_id + "currentPageNum:" + page+1 + "conditons:" + conditons)
                val currentSource: IBaseSource = BaseConfig.getCurrentSource()!!
                val categoryVideoList: CategoryPageBean = currentSource.categoryVideoList(
                    category.type_id,
                    (page+1).toString(),
                    conditons
                )
                LogUtils.d("按条件检索数据:" + categoryVideoList)
                categoryVideoList.list?.forEach {
                    viewDataList.add(VideoViewData(it))
                }
                postData(isLoadMore, viewDataList)
            } catch (e: Exception) {
                LogUtils.d("按条件检索数据失败:" + e.message)
                e.printStackTrace()
                postError(isLoadMore)
                return@launch
            }
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
