package com.xxhoz.secbox.module.search

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.xxhoz.constant.Key
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseActivity
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.ActivitySearchBinding
import com.xxhoz.secbox.databinding.ItemSearchMovieResultListBinding
import com.xxhoz.secbox.databinding.ItemSearchResultSourceBinding
import com.xxhoz.secbox.module.player.DetailPlayerActivity
import com.xxhoz.secbox.module.start.StartViewModel
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.util.UniversalAdapter
import com.xxhoz.secbox.util.getActivity
import com.xxhoz.secbox.util.setImageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override fun getPageName(): String = PageName.SEARCH
    private val viewModel: StartViewModel by viewModels()
    override val inflater: (inflater: LayoutInflater) -> ActivitySearchBinding
        get() = ActivitySearchBinding::inflate

    private lateinit var sourceAdapter: UniversalAdapter<String>
    private lateinit var resultAdapter: UniversalAdapter<VideoBean>
    private var sourceList = ArrayList<String>()
    private var resultItemList = ArrayList<VideoBean>()
    // 所有搜索结果
    private var allResultItemList = HashMap<String, List<VideoBean>>()
    private var searchJobs: ArrayList<Job> = ArrayList()

    // 选中的source
    private var ALL_DATA = "全部显示"
    private var selectSourceKey = "全部显示"
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
        initView()
    }

    override fun onResume() {
        super.onResume()
        // 选中的source
        selectSourceKey = "全部显示"
    }

    override fun onBackPressed() {
        searchJobs.forEach {
            it.cancel()
        }
        searchJobs.clear()
        if (viewBinding.searchView.query.length == 0) {
            super.onBackPressed()
        } else {
            viewBinding.searchView.setQuery("",false)
        }
    }

    private fun initView() {
        // 加载历史记录
        loadSearchHistory()
        viewBinding.promptView.hide()
        // 返回
        viewBinding.returnImage.setOnClickListener {
            onBackPressed()
        }
        // 搜索逻辑
        viewBinding.searchView.isIconified = false
        viewBinding.searchView.queryHint = "输入搜索关键词"

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (viewBinding.promptView.isShowing()) {
                    return true
                }
                viewBinding.promptView.showLoading()
                startSearch(query.trim())

                viewBinding.historyLayout.visibility = View.INVISIBLE
                viewBinding.resultLayout.visibility = View.VISIBLE
                // 隐藏键盘
                try {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                } catch (_: Exception) {
                }

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length == 0) {
                    viewBinding.historyLayout.visibility = View.VISIBLE
                    viewBinding.resultLayout.visibility = View.INVISIBLE
                }
                return true
            }
        })

        // 清空历史记录
        viewBinding.clearText.setOnClickListener {
            clearSearchHistory()
        }

        // 左边 搜索源
        viewBinding.resultSourceList.layoutManager = LinearLayoutManager(this)
        sourceAdapter = UniversalAdapter(
            sourceList,
            R.layout.item_search_result_source,
            object : UniversalAdapter.DataViewBind<String> {
                override fun exec(data: String, view: View) {
                    val bind = ItemSearchResultSourceBinding.bind(view)
                    bind.sourceText.background = AppCompatResources.getDrawable(getActivity()!!, R.color.white)
                    bind.sourceText.text = data
                    if (data.equals(selectSourceKey)){
                        bind.sourceText.background = AppCompatResources.getDrawable(getActivity()!!, R.color.theme_color)
                    }
                    bind.sourceText.setOnClickListener {
                        onClickSource(data)
                    }
                }
            })
        viewBinding.resultSourceList.adapter = sourceAdapter

        // 右边 搜索结果
        viewBinding.resultVideoList.layoutManager = LinearLayoutManager(this)
        resultAdapter = UniversalAdapter(
            resultItemList,
            R.layout.item_search_movie_result_list,
            object : UniversalAdapter.DataViewBind<VideoBean> {
                override fun exec(data: VideoBean, view: View) {
                    val bind = ItemSearchMovieResultListBinding.bind(view)
                    bind.picMovie.setImageUrl(data.vod_pic)
                    bind.titleMovie.text = data.vod_name
                    bind.sourceText.text = data.vod_remarks
                    bind.root.setOnClickListener {
                        onClickResultItem(data)
                    }
                }
            })
        viewBinding.resultVideoList.adapter = resultAdapter
    }

    /**
     * 搜索
     */
    private fun startSearch(query: String) {
        searchJobs.forEach {
            it.cancel()
        }
        searchJobs.clear()

        Toaster.show("开始搜索: ${query}")
        sourceList.clear()
        resultItemList.clear()
        allResultItemList.clear()
        addSearchHistory(query)

        sourceList.add(ALL_DATA)

        sourceAdapter.notifyDataSetChanged()
        resultAdapter.notifyDataSetChanged()

        searchJobs.add(lifecycleScope.launch(Dispatchers.IO){
//            val searchAbleList: List<IBaseSource> = SourceManger.getSearchAbleList()
            val sourceBeanList = SourceManger.getSourceBeanList()
            for (sourceBean in sourceBeanList) {
                val source = SourceManger.getSpiderSource(sourceBean.key)!!
                // 多线程提高搜索速度
                async {
                    asyncSearch(source, query)
                }
            }
        })
    }

    private suspend fun asyncSearch(iBaseSource: IBaseSource, query: String) {

        var searchVideo: List<VideoBean>? = try {
            iBaseSource.searchVideo(query)
        } catch (e: Exception) {
            LogUtils.e("${iBaseSource.sourceBean.name}=>搜索失败:" + iBaseSource.sourceBean)
            e.printStackTrace()
            return
        }

        if (searchVideo == null || searchVideo.isEmpty()) {
            return
        }

        searchVideo.forEach {
            it.sourceBean = iBaseSource.sourceBean
        }
        // 严格搜索模式
        searchVideo = searchVideo.filter { it.vod_name.contains(query) }

        withContext(Dispatchers.Main) {
            // 源列表添加
            sourceList.add(iBaseSource.sourceBean.name)
            sourceAdapter.notifyDataSetChanged()

            if (selectSourceKey.equals(ALL_DATA)) {
                resultItemList.addAll(searchVideo)
                resultAdapter.notifyDataSetChanged()
            }
            allResultItemList.put(iBaseSource.sourceBean.name, searchVideo)
            if (viewBinding.promptView.isShowing()) {
                viewBinding.promptView.hide()
            }
        }
    }


    /**
     * 点击源列表
     */
    private fun onClickSource(data: String) {
//        Toaster.show("点击了源${data}")
        selectSourceKey = data
        sourceAdapter.notifyDataSetChanged()

        resultItemList.clear()
        if (selectSourceKey.equals(ALL_DATA)){
            allResultItemList.forEach {
                resultItemList.addAll(it.value)
            }
        }else{
            resultItemList.addAll(allResultItemList.get(selectSourceKey)!!)
        }
        resultAdapter.notifyDataSetChanged()
    }


    /**
     * 点击搜索结果
     */
    private fun onClickResultItem(data: VideoBean) {
        val playInfoBean: PlayInfoBean = PlayInfoBean(
            data.sourceBean!!.key,
            data,
            0
        )
        LogUtils.i("条目点击: ${playInfoBean}")
        searchJobs.forEach {
            it.cancel()
        }
        DetailPlayerActivity.startActivity(this, playInfoBean)
    }

    val gson = GsonFactory.getSingletonGson()


    /**
     * 清空历史搜索记录
     */
    private fun clearSearchHistory() {
        viewBinding.historyBox.removeAllViews()
        XKeyValue.putString(Key.SEARCH_HISTORY, "[]")
    }

    private fun loadSearchHistory() {
        val searchHistoryList: ArrayList<String> = getHistoryList()

        for (query: String in searchHistoryList) {
            val textView = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            textView.setPadding(10, 10, 10, 10)
            textView.layoutParams = layoutParams
            textView.text = query
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            textView.setTextColor(Color.parseColor("#3f3e3e"))
            textView.background = AppCompatResources.getDrawable(this, R.drawable.history_tag_bg)
            textView.setOnClickListener {
                // 点击历史记录回调
                viewBinding.searchView.setQuery(query,true)
            }
            viewBinding.historyBox.addView(textView)
        }

    }

    /**
     * 添加历史记录
     */
    private fun addSearchHistory(query: String) {
        val searchHistoryList: ArrayList<String> = getHistoryList()
        if (searchHistoryList.contains(query)){
            return
        }
        searchHistoryList.add(query)
        val json: String = gson.toJson(searchHistoryList)
        clearSearchHistory()
        XKeyValue.putString(Key.SEARCH_HISTORY, json)
        loadSearchHistory()
    }

    private fun getHistoryList(): ArrayList<String> {
        val json: String = XKeyValue.getString(Key.SEARCH_HISTORY, "[]")
        val arrayListType = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, arrayListType)
    }

}
