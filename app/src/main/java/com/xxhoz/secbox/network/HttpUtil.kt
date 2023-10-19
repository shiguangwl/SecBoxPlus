package com.xxhoz.secbox.network


import com.hjq.gson.factory.GsonFactory
import com.xxhoz.secbox.util.LogUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream


object HttpUtil {
    private val client = OkHttpClient.Builder()
        .addInterceptor(DeflateInterceptor())
        .build()
    private val gson = GsonFactory.getSingletonGson()





    fun getBytes(url: String): ByteArray? {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }

            return response.body?.bytes()
        }
    }

    fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            return response.body?.string() as String
        }
    }

    fun <T> get(url: String, clazz: Class<T>): T {
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string()
                if (clazz == String::class.java){
                    return responseBody as T
                }
                LogUtils.d("网络请求URL:" + url + "  响应内容: " + responseBody)
                return gson.fromJson(responseBody, clazz)
            }
        } catch (e: Exception) {
            LogUtils.e("网络请求错误URL:" + url)
            e.printStackTrace()
            throw e
        }
    }


    /**
     * 发起HTTP GET请求，获取指定URL的响应并将其反序列化为给定类型的对象。
     *
     * @param url 请求的URL字符串
     * @param clazz 反序列化响应数据时要使用的类（通常是一个数据模型类）
     * @param retryCount 重试次数，用于在请求失败时重试请求（至少会执行一次请求）
     * @return 返回反序列化后的对象
     * @throws Exception 如果在所有重试尝试后仍然无法成功获取响应，则抛出异常
     */
    fun <T> getWithRetry(url: String, clazz: Class<T>, retryCount: Int): T {
        var lastException: Exception? = null

        for (retryAttempt in 0 until retryCount) {
            try {
                return get(url, clazz)
            } catch (e: Exception) {
                lastException = e
            }
        }

        // 如果所有重试都失败，则抛出异常
        lastException?.let {
            throw it
        }
            ?: throw RuntimeException("No exception occurred during retry, but the request still failed.")
    }

    fun <T> post(url: String, body: RequestBody, clazz: Class<T>): T? {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseBody = response.body?.string()
            return gson.fromJson(responseBody, clazz)
        }
    }

    // GET请求异步方式
    fun <T> getAsync(url: String, clazz: Class<T>, callback: (T?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val result = gson.fromJson(responseBody, clazz)
                callback(result, null)
            }
        })
    }

    // POST请求异步方式
    fun <T> postAsync(url: String, body: RequestBody, clazz: Class<T>, callback: (T?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val result = gson.fromJson(responseBody, clazz)
                callback(result, null)
            }
        })
    }

    /**
     * 下载文件
     * @param url 下载地址
     * @param path 保存路径
     * @return 保存的文件
     */
    fun downLoad(url: String, path: String): File {
        val request = Request.Builder()
            .url(url)
            .build()


        // 设置超时时间为20秒
        val client = OkHttpClient.Builder()
            // 设置连接超时时间，单位为秒
            .connectTimeout(30, TimeUnit.SECONDS)
            // 设置读取超时时间，单位为秒
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(DeflateInterceptor())
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            file.writeBytes(response.body?.bytes()!!)
            return file
        }
    }

    class DeflateInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())
            val originalBody = originalResponse.body

            // 检查响应是否使用 Deflate 压缩
            if ("deflate".equals(originalResponse.header("Content-Encoding"), true)) {
                // 解压 Deflate 数据
                val inflater = Inflater(true)
                val inflaterInputStream = InflaterInputStream(ByteArrayInputStream(originalBody?.bytes()), inflater)
                val uncompressedBody =
                    inflaterInputStream.readBytes().toResponseBody(originalBody?.contentType())

                // 创建一个新的响应
                return originalResponse.newBuilder()
                    .body(uncompressedBody)
                    .build()
            }

            return originalResponse
        }
    }

}
