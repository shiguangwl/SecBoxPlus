package com.xxhoz.secbox.item

import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.parserCore.bean.VideoBean

class VideoViewData(data: VideoBean) : BaseViewData<VideoBean>(data) {
    override fun isGridViewData(): Boolean = true
}
