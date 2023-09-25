package com.xxhoz.secbox.base

import com.xxhoz.constant.Key
import com.xxhoz.secbox.App
import com.xxhoz.secbox.persistence.XKeyValue
import java.io.File

/**
 * 缓存文件管理器
 */
object CacheManger {

    init {
        //  缓存惰性删除,每次启动时检查缓存文件是否过期,过期则删除
        val cacheList = getCacheList()
        val expiredCacheList = cacheList.filter { it.expirationMillis < System.currentTimeMillis() }
        expiredCacheList.forEach {
            deleteCacheFile(it.cacheName)
        }
        cacheList.removeAll(expiredCacheList)
        XKeyValue.putObjectList(Key.CACHE_FILE_LIST, cacheList)
    }
    /**
     * 缓存保存位置
     */
    private val cacheDir:String by lazy {
        val danmuDir = App.instance.filesDir.absolutePath + "/danmu/"
        //  创建缓存文件夹
        val cacheDir = File(danmuDir)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        danmuDir
    }

    /**
     * 获取所有缓存记录
     */
    private fun getCacheList(): ArrayList<CacheItem> {
        val cacheItems = XKeyValue.getObjectList<CacheItem>(Key.CACHE_FILE_LIST)
        return cacheItems ?: ArrayList()
    }

    /**
     * 添加缓存记录
     */
    private fun addCacheItem(item: CacheItem) {
        XKeyValue.addObjectList(Key.CACHE_FILE_LIST,item)
    }

    /**
     * 删除缓存记录
     */
    private fun deleteCacheItem(cacheName: String) {
        val cacheList = getCacheList()
        val cacheItem = cacheList.find { it.cacheName == cacheName }
        if (cacheItem != null) {
            XKeyValue.addObjectList(Key.CACHE_FILE_LIST, cacheList)
        }
    }

    /**
     * 添加缓存文件
     * @param cacheName 缓存名称
     * @param file 缓存文件
     * @param expirationMillis 过期时间
     */
    fun cacheFile(cacheName: String, file: File, expirationMillis: Long) :File{
        //  缓存记录中判断是否存在缓存文件,存在则删除文件 写入新的缓存文件
        val cacheFile = File(cacheDir, cacheName)
        file.copyTo(cacheFile, overwrite = true)
        //  记录缓存信息
        val cacheItem = CacheItem(cacheName, System.currentTimeMillis() + expirationMillis)
        addCacheItem(cacheItem)

        return cacheFile
    }

    /**
     * 获取缓存文件,如果缓存文件不存在则返回null
     */
    fun getCacheFile(cacheName: String): File? {
        val cacheList = getCacheList()
        val cacheItem = cacheList.find { it.cacheName == cacheName }

        return if (cacheItem != null) {
            val cacheFile = File(cacheDir, cacheItem.cacheName)
            if (cacheFile.exists()) {
                cacheFile
            } else {
                // Cache file doesn't exist, remove the cache item from the list
                deleteCacheItem(cacheName)
                null
            }
        } else {
            null
        }
    }


    /**
     * 删除缓存文件
     * @param cacheName 缓存名称
     */
    fun deleteCacheFile(cacheName: String) {
        val cacheFile = File(cacheDir, cacheName)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        deleteCacheItem(cacheName)
    }


    /**
     * 清空缓存
     */
    fun cleanCache(){
        val cacheList = getCacheList()
        cacheList.forEach {
            deleteCacheFile(it.cacheName)
        }
        XKeyValue.putObjectList(Key.CACHE_FILE_LIST, ArrayList<CacheItem>())
    }

    data class CacheItem(
        /**
         * 缓存名称
         */
        val cacheName: String,
        /**
         * 缓存过期时间戳
         */
        val expirationMillis: Long
    )
}
