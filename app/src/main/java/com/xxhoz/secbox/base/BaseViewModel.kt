package com.xxhoz.secbox.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel

/**
 * ViewModel基类
 */
abstract class BaseViewModel : ViewModel(), IGetPageName {


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


//    private val taskList:HashMap<String,Job> = HashMap()
//    open fun submitTask(task :()->Unit){
//        viewModelScope.launch {
//            withContext(Dispatchers.IO){
//                try {
//                    block()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    if (e is SocketTimeoutException) {
//                        stateVideoPlayerMsg.postValue("网络连接超时")
//                    } else {
//                        stateVideoPlayerMsg.postValue("网络连接错误")
//                    }
//                }
//            }
//        }
//
//    }
}
