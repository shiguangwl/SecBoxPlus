package com.xxhoz.secbox.module.discovery

import androidx.lifecycle.viewModelScope
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.list.base.BaseRecyclerViewModel
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.bean.CatagoryBean
import com.xxhoz.secbox.bean.GoodsBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.item.CatagoryListViewData
import com.xxhoz.secbox.item.CatagoryViewData
import com.xxhoz.secbox.item.GoodsViewData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiscoveryViewModel : BaseRecyclerViewModel() {

    override fun loadData(isLoadMore: Boolean, isReLoad: Boolean, page: Int) {
        viewModelScope.launch {
            delay(1000L)
            val viewDataList = mutableListOf<BaseViewData<*>>()
            if (!isLoadMore) {
                val categoryList = mutableListOf<CatagoryViewData>()
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_girl, "萌妹")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_cat, "撸猫")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_bodybuilding, "健身")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_movie, "电影")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_music, "音乐")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_game, "游戏")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_photography, "摄影")))
                categoryList.add(CatagoryViewData(CatagoryBean(R.drawable.icon_learn, "学习")))
                viewDataList.add(CatagoryListViewData(categoryList))

                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))

                postData(isLoadMore, viewDataList)
            } else {
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                viewDataList.add(GoodsViewData(GoodsBean("", "", 100, 50000L)))
                postData(isLoadMore, viewDataList)
            }
        }
    }

    @PageName
    override fun getPageName() = PageName.DISCOVERY
}
