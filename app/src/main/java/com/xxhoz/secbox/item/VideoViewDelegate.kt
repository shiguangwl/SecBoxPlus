package com.xxhoz.secbox.item

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xxhoz.secbox.App
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.list.base.BaseItemViewDelegate
import com.xxhoz.secbox.databinding.ItemVideoBinding
import com.xxhoz.secbox.util.setImageUrl

class VideoViewDelegate : BaseItemViewDelegate<VideoViewData, VideoViewDelegate.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, context: Context, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemVideoBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: VideoViewData) {
        holder.viewBinding.tvTitle.setText(item.value.title)
        holder.viewBinding.ivCover.setImageUrl(item.value.coverImg)
        super.onBindViewHolder(holder, item)
    }

    class ViewHolder(val viewBinding: ItemVideoBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    }
}
