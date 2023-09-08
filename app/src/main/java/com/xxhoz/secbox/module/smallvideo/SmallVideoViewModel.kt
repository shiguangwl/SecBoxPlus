package com.xxhoz.secbox.module.smallvideo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xxhoz.secbox.base.BaseViewModel
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.parserCore.bean.VideoBean
import kotlinx.coroutines.launch

class SmallVideoViewModel : BaseViewModel() {

    val helloWorldLiveData = MutableLiveData<Result<VideoBean>>()

    fun requestVideoDetail(id: String) {
        viewModelScope.launch {
//            val result = NetworkApi.requestVideoDetail(id)
//            helloWorldLiveData.value = "result"
        }
    }

    @PageName
    override fun getPageName() = PageName.SMALL_VIDEO
}
