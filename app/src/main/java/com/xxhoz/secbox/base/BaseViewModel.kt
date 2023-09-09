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


}
