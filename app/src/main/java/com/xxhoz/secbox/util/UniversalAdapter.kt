package com.xxhoz.secbox.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 通用快速适配器
 *
 * @param dataResource 数据源
 * @param layoutId     布局id
 * @param function     回调方法用于数据与视图的绑定
 */
class UniversalAdapter<T>(
    val dataResource: List<T>, // 视图资源
    val layoutId: Int, // 数据视图绑定
    val function: DataViewBind<T>
) : RecyclerView.Adapter<UniversalAdapter.ViewHolder>() {
    // 子项渲染个数，-1代表所有
    private var len = -1
    fun setLength(length: Int) {
        len = length
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.view
        function.exec(dataResource[position], view)
    }

    override fun getItemCount(): Int {
        if (len > 0) {
            return len
        } else if (len == 0) {
            throw RuntimeException("The length must not be 0")
        }
        return dataResource.size
    }

    interface DataViewBind<T> {
        fun exec(data: T, view: View)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var view: View

        init {
            view = itemView
        }
    }
}
