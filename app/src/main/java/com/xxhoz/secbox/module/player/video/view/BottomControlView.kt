package com.xxhoz.secbox.module.player.video.view

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.xxhoz.secbox.R
import xyz.doikki.videocontroller.component.VodControlView
import xyz.doikki.videoplayer.player.VideoView

class BottomControlView(context: Context) : VodControlView(context) {

    private var nextVod: ImageView
    private var changeEposde: TextView
    private var changeSpeed: TextView
    private var fullScreen: ImageView
    init {
        nextVod = findViewById(R.id.next_vod)
        changeEposde = findViewById(R.id.change_epsode)
        changeSpeed = findViewById(R.id.change_speed)
        fullScreen = findViewById(R.id.fullscreen);
    }

    override fun getLayoutId(): Int {
        return R.layout.dkplayer_layout_vod_control_view
    }


    override fun onPlayerStateChanged(playerState: Int){
        super.onPlayerStateChanged(playerState)

        when (playerState) {
            VideoView.PLAYER_NORMAL -> {
                changeEposde.visibility = GONE
                changeSpeed.visibility = GONE
                fullScreen.visibility = VISIBLE
            }
            VideoView.PLAYER_FULL_SCREEN -> {
                changeEposde.visibility = VISIBLE
                changeSpeed.visibility = VISIBLE
                fullScreen.visibility = GONE
            }
        }
    }


    fun setNextVodListener(listener: OnClickListener) {
        nextVod.setOnClickListener(listener)
    }
    fun setChangeEposdeListener(listener: OnClickListener) {
        changeEposde.setOnClickListener(listener)
    }
    fun setChangeSpeedListener(listener: OnClickListener) {
        changeSpeed.setOnClickListener(listener)
    }

}
