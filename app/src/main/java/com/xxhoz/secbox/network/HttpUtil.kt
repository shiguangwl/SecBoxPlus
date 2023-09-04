package com.xxhoz.secbox.network


import com.hjq.gson.factory.GsonFactory
import okhttp3.*
import java.io.IOException
import java.util.Objects

object HttpUtil {
    private val client = OkHttpClient()
    private val gson = GsonFactory.getSingletonGson()


    fun getBytes(url: String): ByteArray? {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null;
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

    fun <T> get(url: String, clazz: Class<T>): T? {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseBody = response.body?.string()
            if (clazz == String::class.java){
                return responseBody as T
            }
            return gson.fromJson(responseBody, clazz)
        }
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
}
