package com.xxhoz.secboxbrowser.popview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.PartShadowPopupView
import com.lxj.xpopup.util.XPopupUtils

import com.xxhoz.secboxbrowser.adapter.UniversalAdapter
import com.xxhoz.snifferwebview.R


/**
 * Description: 自定义局部阴影弹窗
 * @author DengNanYu
 */
class SnifferListPopupView(context: Context) : PartShadowPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.custom_part_shadow_popup
    }

    lateinit var callback: (String) -> Unit

    private val itemList = mutableListOf<String>()

    private var universalAdapter: UniversalAdapter<String>? = null
    override fun onCreate() {
        super.onCreate()
        val recyclerView = findViewById<View>(R.id.recycler_data_view) as RecyclerView
        val dataList = itemList

        recyclerView.layoutManager = LinearLayoutManager(context)
        universalAdapter = UniversalAdapter(
            dataList,
            R.layout.item_sniffer_result,
            object : UniversalAdapter.DataViewBind<String> {
                override fun exec(data: String, view: View) {
                    view.findViewById<ImageView>(R.id.start_play).setOnClickListener {
                        callback(it.tag as String)
                    }

                    view.findViewById<TextView>(R.id.tv_url_text).text = data
                    view.findViewById<ImageView>(R.id.start_play).tag = data

                    view.findViewById<TextView>(R.id.tv_url_text).setOnClickListener {
                        var confirm = XPopup.Builder(activity).asInputConfirm(
                            "编辑结果", ""
                        ) { text ->
                            (it as TextView).text = text
                        }
                        confirm.show()
                        Handler(Looper.myLooper()!!).postDelayed({
                            confirm?.editText?.setText((it as TextView).text)
                        }, 300)
                    }
                }
            })
        recyclerView.adapter = universalAdapter

    }

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getAppHeight(context) * .5f).toInt()
    }

    /**
     * 添加项
     */
    fun addItem(item: String) {
        itemList.add(item)
        universalAdapter?.notifyDataSetChanged()
    }

    /**
     * 清空项
     */
    fun clearItem() {
        itemList.clear()
        universalAdapter?.notifyDataSetChanged()
    }

    /**
     * 获取数量
     */
    fun getItemCount(): Int {
        return itemList.size
    }
}

