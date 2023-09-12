package com.xxhoz.secbox.parserCore.bean

import java.io.Serializable

data class VideoBean(
    var vod_id: String = "",
    var vod_name: String = "",
    var vod_pic: String = "",
    var vod_remarks: String = ""
): Serializable{
    var sourceBean:SourceBean? = null
}
