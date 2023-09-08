package com.xxhoz.secbox.item

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xxhoz.secbox.base.list.base.BaseItemViewDelegate
import com.xxhoz.secbox.databinding.ItemVideoBinding
import com.xxhoz.secbox.util.setImageUrl

class VideoViewDelegate : BaseItemViewDelegate<VideoViewData, VideoViewDelegate.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, context: Context, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemVideoBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: VideoViewData) {
        holder.viewBinding.tvTitle.text = item.value.vod_name
        var vodPic = item.value.vod_pic
        if (item.value.vod_pic.contains("@")){
            vodPic = item.value.vod_pic.split("@")[0]
        }
        holder.viewBinding.ivCover.setImageUrl(vodPic)
        holder.viewBinding.remarkText.text = item.value.vod_remarks
        super.onBindViewHolder(holder, item)
    }

    class ViewHolder(val viewBinding: ItemVideoBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    }
}
