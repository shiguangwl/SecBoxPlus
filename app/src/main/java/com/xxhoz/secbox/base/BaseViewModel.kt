package com.xxhoz.secbox.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel基类
 */
abstract class BaseViewModel : ViewModel(), IGetPageName,TaskManger {


    override fun onCleared() {
        clearTask()
        super.onCleared()
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

    override fun getScope(): CoroutineScope = viewModelScope
}
