package com.xxhoz.secbox.module.search

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.toast.Toaster
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivitySearchBinding
import com.xxhoz.secbox.module.start.StartViewModel
import com.xxhoz.secbox.util.UniversalAdapter


class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override fun getPageName(): String = PageName.SEARCH
    private val viewModel: StartViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivitySearchBinding
        get() = ActivitySearchBinding::inflate

    companion object{
        fun startActivity(context: Context){
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }
        // 加载历史记录
        loadSearchHistory()

        viewBinding.searchView.setIconified(false);
        viewBinding.searchView.queryHint = "输入搜索关键词"

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Toaster.show("开始搜索")
                viewBinding.historyLayout.visibility = View.INVISIBLE
                viewBinding.resultLayout.visibility = View.VISIBLE
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Toaster.show("内容改变${newText}")
                if (newText.length == 0){
                    viewBinding.historyLayout.visibility = View.VISIBLE
                    viewBinding.resultLayout.visibility = View.INVISIBLE
                }
                return true
            }
        })

        viewBinding.clearText.setOnClickListener(){
            // 清空历史记录
            viewBinding.historyBox.removeAllViews()
        }


        var dataList = listOf("11111","22222","333333","333333","333333","333333","333333","333333","333333","333333","333333","333333","666666","777777")
        viewBinding.resultSourceList.layoutManager =LinearLayoutManager(this)
        viewBinding.resultSourceList.adapter = UniversalAdapter(dataList,R.layout.search_result_source_layout,object: UniversalAdapter.DataViewBind<String> {
            override fun exec(data: String, view: View?) {
                val sourceText = view?.findViewById<TextView>(R.id.source_text)
                sourceText?.text = data
            }
        })
    }


    private fun loadSearchHistory() {
        for (i in 0..10) {
            val textView = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            textView.setPadding(10, 10, 10, 10)
            textView.layoutParams = layoutParams
            textView.text = "你好生活"
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            textView.setTextColor(Color.parseColor("#3f3e3e"))
            textView.background = AppCompatResources.getDrawable(this, R.drawable.history_tag_bg)
            viewBinding.historyBox.addView(textView)
        }
    }


}
