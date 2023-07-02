package com.xxhoz.secbox.network

import com.xxhoz.secbox.bean.VideoBean
import com.xxhoz.secbox.network.base.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface INetworkService {

    @GET("videodetail")
    suspend fun requestVideoDetail(@Query("id") id: String): BaseResponse<VideoBean>
}
