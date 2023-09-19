package com.xxhoz.secbox.persistence

import android.app.Application
import android.os.Parcelable
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.tencent.mmkv.MMKV
import com.xxhoz.constant.Key

/**
 * 本类为MMKV的封装类，防止代码入侵
 */
object XKeyValue {

    private const val ID_DEFAULT = "id_default"
    private const val ID_ACCOUNT = "id_account"

    fun init(application: Application) {
        MMKV.initialize(application)
    }

    fun from(@Key key: String): MMKV {
        return if (key.startsWith("account_")) {
            getAccountMMKV()
        } else {
            getDefaultMMKV()
        }
    }

    private fun getDefaultMMKV(): MMKV {
        return MMKV.mmkvWithID(ID_DEFAULT, MMKV.SINGLE_PROCESS_MODE)
    }

    private fun getAccountMMKV(): MMKV {
        return MMKV.mmkvWithID(ID_ACCOUNT, MMKV.SINGLE_PROCESS_MODE)
    }

    fun clearAccountMMKV() {
        getAccountMMKV().clearAll()
    }

    fun putBoolean(@Key key: String, value: Boolean) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getBoolean(@Key key: String, defaultValue: Boolean = false): Boolean {
        return from(key).decodeBool(key, defaultValue)
    }

    fun putString(@Key key: String, value: String) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getString(@Key key: String, defaultValue: String = ""): String {
        return from(key).decodeString(key, defaultValue)!!
    }

    fun putInt(@Key key: String, value: Int) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getInt(@Key key: String, defaultValue: Int = 0): Int {
        return from(key).decodeInt(key, defaultValue)
    }

    fun putFloat(@Key key: String, value: Float) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getFloat(@Key key: String, defaultValue: Float = 0F): Float {
        return from(key).decodeFloat(key, defaultValue)
    }

    fun putLong(@Key key: String, value: Long) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getLong(@Key key: String, defaultValue: Long = 0L): Long {
        return from(key).decodeLong(key, defaultValue)
    }

    fun putDouble(@Key key: String, value: Double) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getDouble(@Key key: String, defaultValue: Double = 0.0): Double {
        return from(key).decodeDouble(key, defaultValue)
    }

    fun putByteArray(@Key key: String, value: ByteArray) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getByteArray(@Key key: String, defaultValue: ByteArray = ByteArray(0)): ByteArray {
        return from(key).decodeBytes(key, defaultValue)!!
    }

    fun putStringSet(@Key key: String, value: Set<String>) {
        from(key).encode(key, value)
    }

    @JvmOverloads
    fun getStringSet(@Key key: String, defaultValue: Set<String> = mutableSetOf()): Set<String> {
        return from(key).decodeStringSet(key, defaultValue)!!
    }

    fun putParcelable(@Key key: String, value: Parcelable) {
        from(key).encode(key, value)
    }

    inline fun <reified T : Parcelable> getParcelable(@Key key: String): T? {
        return from(key).decodeParcelable(key, T::class.java)
    }

//    /**
//     * 存储对象
//     */
//    fun <T> putObject(@Key key: String, value: T) {
//        val json = GsonFactory.getSingletonGson().toJson(value)
//        from(key).encode(key, json)
//    }
//
//    /**
//     * 获取对象
//     */
//    inline fun <reified T> getObject(@Key key: String): T? {
//        val json = from(key).decodeString(key, null)
//        return GsonFactory.getSingletonGson().fromJson(json, T::class.java)
//    }

    /**
     * 存储对象列表
     */
    fun <T> putObjectList(@Key key: String, value: ArrayList<T>) {
        val json = GsonFactory.getSingletonGson().toJson(value)
        from(key).encode(key, json)
    }

    /**
     * 获取对象列表
     */
    inline fun <reified T> getObjectList(@Key key: String): ArrayList<T>? {
        val json = from(key).decodeString(key, null)
        if (json == null){
            return null
        }
        val gson = GsonFactory.getSingletonGson()
        return gson.fromJson(json, object : TypeToken<ArrayList<T>>() {}.type)
    }

    /**
     * 列表添加元素
     */
    inline fun <reified T> addObjectList(@Key key: String, value: T) {
        val list = getObjectList<T>(key)
        if (list == null) {
            val newList = ArrayList<T>()
            newList.add(0,value)
            putObjectList(key, newList)
        } else {
            list.add(value)
            putObjectList(key, list)
        }
    }

    /**
     * 列表删除元素
     */
    inline fun <reified T> removeObjectList(@Key key: String, value: T) {
        val list = getObjectList<T>(key)
        if (list != null) {
            list.remove(value)
            putObjectList(key, list)
        }
    }
    /**
     * 清空某个列表
     */
    fun clearObjectList(@Key key: String) {
        from(key).removeValueForKey(key)
    }
}
