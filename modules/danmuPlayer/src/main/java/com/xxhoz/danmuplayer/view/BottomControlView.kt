package com.xxhoz.secbox.module.detail.video.view

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.xxhoz.danmuplayer.R
import xyz.doikki.videocontroller.component.VodControlView
import xyz.doikki.videoplayer.player.VideoView

class BottomControlView(context: Context, private var danmuState: Boolean) :
    VodControlView(context) {

    private var nextVod: ImageView
    private var changeEposde: TextView
    private var changeSpeed: TextView
    private var fullScreen: ImageView
    private var danmuBtn: ImageView
    private var danmuSettingBtn: ImageView

    // 是否显示下一集控件
    private var next: Boolean = true

    // 是否显示弹幕控件
    private var danmu: Boolean = true

    // 是否显示选集控件
    private var eposde: Boolean = true

    init {
        nextVod = findViewById(R.id.next_vod)
        changeEposde = findViewById(R.id.change_epsode)
        changeSpeed = findViewById(R.id.change_speed)
        danmuBtn = findViewById(R.id.danmu_control)
        danmuSettingBtn = findViewById(R.id.danmu_setting)
        fullScreen = findViewById(R.id.fullscreen)
        changeDanmuState(danmuState)
    }

    override fun getLayoutId(): Int {
        return R.layout.dkplayer_layout_vod_control_view
    }


    override fun onPlayerStateChanged(playerState: Int) {
        super.onPlayerStateChanged(playerState)

        when (playerState) {
            VideoView.PLAYER_NORMAL -> {
                changeEposde.visibility = GONE
                changeSpeed.visibility = GONE
                danmuSettingBtn.visibility = GONE
                fullScreen.visibility = VISIBLE
            }

            VideoView.PLAYER_FULL_SCREEN -> {
                if (eposde) {
                    changeEposde.visibility = VISIBLE
                }
                if (danmu) {
                    danmuSettingBtn.visibility = VISIBLE
                }
                changeSpeed.visibility = VISIBLE
                fullScreen.visibility = GONE
            }
        }
    }

    /**
     * 弹幕状态修改
     */
    fun changeDanmuState(danmuState: Boolean) {
        this.danmuState = danmuState
        if (danmuState) {
            danmuBtn.setImageResource(R.drawable.icon_danmu_open)
        } else {
            danmuBtn.setImageResource(R.drawable.icon_danmu_close)
        }
    }

    /**
     * 下一集
     */
    fun setNextVodListener(listener: OnClickListener) {
        nextVod.setOnClickListener(listener)
    }

    /**
     * 选集
     */
    fun setChangeEposdeListener(listener: OnClickListener) {
        changeEposde.setOnClickListener(listener)
    }

    /**
     * 倍速
     */
    fun setChangeSpeedListener(listener: OnClickListener) {
        changeSpeed.setOnClickListener(listener)
    }

    /**
     * 点击弹幕切换状态回调
     */
    fun setDanmuBtnListener(listener: (danmuState: Boolean) -> Unit) {
        danmuBtn.setOnClickListener {
            changeDanmuState(!danmuState)
            listener(danmuState)
        }
    }

    /**
     * 点击弹幕设置回调
     */
    fun setDanmuSettingBtnListener(listener: OnClickListener) {
        danmuSettingBtn.setOnClickListener(listener)
    }

    /**
     * 设置可视视图
     * @param next 下一集
     * @param eposde 选集
     * @param danmu 弹幕
     */
    fun setViewVisible(next: Boolean, danmu: Boolean, eposde: Boolean) {
        this.danmu = danmu
        this.eposde = eposde
        this.next = next
        nextVod.visibility = if (next) VISIBLE else GONE
        danmuBtn.visibility = if (danmu) VISIBLE else GONE
        danmuSettingBtn.visibility = if (danmu) VISIBLE else GONE
        changeEposde.visibility = if (eposde) VISIBLE else GONE
    }
}
