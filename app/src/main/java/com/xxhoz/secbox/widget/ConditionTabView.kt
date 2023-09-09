package com.xxhoz.secbox.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.xxhoz.secbox.R
import com.xxhoz.secbox.parserCore.bean.CategoryBean

/**
 * <ConditionTab>
 * <ConditionTab>
 *
 * @author DengNanYu
 * @version 1.0_2022/12/8
 * @date 2022/12/8 17:46
</ConditionTab></ConditionTab> */
class ConditionTabView : LinearLayout, OnTabSelectedListener {
    val conditions: HashMap<String, String> = HashMap()
    private lateinit var callback: (HashMap<String, String>) -> Unit
    constructor(context: Context?) : super(context) {
        orientation = VERTICAL
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        orientation = VERTICAL
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        parent.requestDisallowInterceptTouchEvent(true)
    }

    fun addTabLine(categoryFilters: List<CategoryBean.Filter>, callback: (HashMap<String, String>) -> Unit) {
         this.callback= callback
        for (categoryFilter in categoryFilters) {
            // 第一次保存第一个条件
            conditions[categoryFilter.key] = categoryFilter.value.get(0).v

            val inflate =
                LayoutInflater.from(context).inflate(R.layout.view_conditiontab_item, null)
            // 设置tab key
            (inflate.findViewById<View>(R.id.tab_key) as TextView).setText(categoryFilter.name)

            // 添加value
            val tabLayout = inflate.findViewById<TabLayout>(R.id.tab_values)
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            for (filter in categoryFilter.value) {
                val tab = tabLayout.newTab()
                tab.setCustomView(R.layout.view_conditiontab_item_view)
                val textView = tab.customView!!.findViewById<TextView>(R.id.tab_text)
                textView.text = filter.n
                //textView.setTextColor(R.color.ThemeColor);
                tab.tag = arrayOf(categoryFilter.key,filter.v)
                tabLayout.addTab(tab)
            }
            this.addView(inflate)

            // 添加回调
            tabLayout.addOnTabSelectedListener(this)
        }

        //第一次回调通知 用用首次加载
//        callback(conditions)
    }

    /**
     * 返回所选中的条件map
     */
    fun getConditions(): Map<String, String> {
        return conditions
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        var keyVlue: Array<*> = tab.tag as Array<*>
        conditions[keyVlue[0].toString()] = keyVlue[1].toString()
        callback(conditions)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

}
