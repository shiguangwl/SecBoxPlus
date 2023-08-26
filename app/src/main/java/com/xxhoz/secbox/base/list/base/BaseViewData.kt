package com.xxhoz.secbox.base.list.base

import com.xxhoz.secbox.item.CatagoryViewData
import com.xxhoz.secbox.item.GoodsViewData
import com.xxhoz.secbox.item.VideoViewData

/**
 * 将具体数据类型再套一层BaseViewData以实现更多Item布局类型
 * 重写equals和hashCode是为后面的DiffUtil作准备
 */
open class BaseViewData<T>(var value: T) {

    override fun equals(other: Any?): Boolean {
        if (other is BaseViewData<*>) {
            return value?.equals(other.value) ?: false
        }
        return false
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    /**
     * 是否是网格布局的数据
     */
    open fun isGridViewData(): Boolean {
        //this is VideoViewData || this is CatagoryViewData || this is GoodsViewData
        // 具体子类决定
       return false
    }
}
