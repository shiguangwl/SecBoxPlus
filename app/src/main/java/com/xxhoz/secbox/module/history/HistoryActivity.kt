package com.xxhoz.secbox.module.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.toast.Toaster
import com.xxhoz.constant.Key
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivityHistoryBinding
import com.xxhoz.secbox.databinding.ItemHistoryVideoBinding
import com.xxhoz.secbox.module.player.DetailPlayerActivity
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.UniversalAdapter
import com.xxhoz.secbox.util.setImageUrl
import java.util.Formatter
import java.util.Locale

class HistoryActivity : BaseActivity<ActivityHistoryBinding>() {

    var universalAdapter: UniversalAdapter<PlayInfoBean>? = null
    override val inflater: (inflater: LayoutInflater) -> ActivityHistoryBinding
        get() = ActivityHistoryBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        immersionBar {
//            transparentStatusBar()
//            statusBarDarkFont(true)
//            navigationBarColor(R.color.white)
//            navigationBarDarkIcon(true)
//        }

        initView()
    }


    private val playInfoBeans: ArrayList<PlayInfoBean>
        get() {
            var playInfoBeans =
                XKeyValue.getObjectList<PlayInfoBean>(Key.PLAY_History)
            if (playInfoBeans == null || playInfoBeans.isEmpty()) {
                playInfoBeans = ArrayList()
            }
            return playInfoBeans
        }

    fun initView() {
        viewBinding.returnImageview.setOnClickListener() {
            finish()
        }
        viewBinding.clearAllTextview.setOnClickListener() {
            // 清空数据
            XKeyValue.clearObjectList(Key.PLAY_History)
            initView()
            Toaster.showLong("清空成功")
        }

        viewBinding.playHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        universalAdapter = UniversalAdapter(
            playInfoBeans,
            R.layout.item_history_video,
            object : UniversalAdapter.DataViewBind<PlayInfoBean> {
                override fun exec(data: PlayInfoBean, view: View) {
                    val itemView: ItemHistoryVideoBinding = ItemHistoryVideoBinding.bind(view)
                    val videoBean: VideoBean = data.videoBean

                    itemView.playhistoryTitle.text = videoBean.vod_name + "  第${data.preNum + 1}集"

                    var vodPic = videoBean.vod_pic
                    if (videoBean.vod_pic.contains("@")) {
                        vodPic = videoBean.vod_pic.split("@")[0]
                    }
                    itemView.picImageview.setImageUrl(vodPic)
                    itemView.sourceNameText.text = videoBean.vod_remarks

                    itemView.currentPositionTextView.text = "观看至:" + stringForTime(data.position)
                    itemView.root.setOnClickListener() {
                        DetailPlayerActivity.startActivity(this@HistoryActivity, data)
                    }
                }
            })
        viewBinding.playHistoryRecyclerView.adapter = universalAdapter
    }


    fun stringForTime(timeMs: Long): String {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = timeMs / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    @PageName
    override fun getPageName() = PageName.HISTORY

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, HistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

}
