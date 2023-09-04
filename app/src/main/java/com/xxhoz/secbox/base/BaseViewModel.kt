package com.xxhoz.secbox.base

import androidx.lifecycle.ViewModel

/**
 * ViewModel基类
 */
abstract class BaseViewModel : ViewModel(), IGetPageName {


    override fun onCleared() {
        super.onCleared()
    }


}
