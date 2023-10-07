package com.xxhoz.secbox.module.player.video

import java.io.File

open class SimpleVideoCallback : DanmuVideoPlayer.PlayerCallback {
    override fun featureEnabled(): DanmuVideoPlayer.ViewState =
        DanmuVideoPlayer.ViewState(false, false, false)

    override fun nextClick() {
    }

    override fun throwingScreenClick() {
    }

    override fun selectPartsClick(position: Int) {
    }

    override fun retryClick() {
    }

    override fun loadDanmaku(callBack: (File) -> Unit) {
    }
}
