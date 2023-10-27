package com.xxhoz.secbox.persistence

import com.xxhoz.constant.Key

/**
 * 持久化增删改查实现
 */
class CRUD<T>(@Key val nameKey: String) {

    /**
     * 添加
     */
    inline fun <reified R : T> add(value: R, index: Int = 0) {
        synchronized(this) {
            val list = XKeyValue.getObjectList<R>(nameKey) ?: ArrayList()
            list.add(index, value)
            XKeyValue.putObjectList(nameKey, list)
        }
    }

    /**
     * 删除
     */
    inline fun <reified R : T> remove(value: R) {
        synchronized(this) {
            val list = XKeyValue.getObjectList<R>(nameKey)
            if (list != null) {
                list.remove(value)
                XKeyValue.putObjectList(nameKey, list)
            }
        }
    }

    /**
     * 更新
     */
    inline fun <reified R : T> update(value: R) {
        synchronized(this) {
            val list = XKeyValue.getObjectList<R>(nameKey)
            if (list != null) {
                val index = list.indexOf(value)
                if (index != -1) {
                    list[index] = value
                    XKeyValue.putObjectList(nameKey, list)
                }
            }
        }
    }

    /**
     * 查询
     */
    inline fun <reified R : T> list(): ArrayList<R> {
        return XKeyValue.getObjectList(nameKey) ?: ArrayList()
    }

    /**
     * 删除全部
     */
    fun removeAll() {
        XKeyValue.clearObjectList(nameKey)
    }
}
