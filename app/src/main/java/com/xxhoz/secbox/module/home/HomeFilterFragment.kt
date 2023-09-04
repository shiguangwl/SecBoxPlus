package com.xxhoz.secbox.module.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.hjq.toast.Toaster
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.base.list.XRecyclerView
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeFilterBinding
import com.xxhoz.secbox.module.player.DetailPlayerActivity
import com.xxhoz.secbox.widget.ConditionTabView
import com.xxhoz.secbox.widget.GridItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * 首页
 */
class HomeFilterFragment(var categorr: String) : BaseFragment<FragmentHomeFilterBinding>() {

    lateinit var conditionView: ConditionTabView
    lateinit var itemListView: XRecyclerView
    private val viewModel: HomeFilterViewModel by viewModels()

    private var isLoading = false
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeFilterBinding
        get() = FragmentHomeFilterBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conditionView = viewBinding.conditionView
        itemListView = viewBinding.itemListView
        viewBinding.loadPromptView.showLoading()
    }


    override fun onResume() {
        super.onResume()
        if (isLoading){
            return
        }
        lifecycleScope.launch(Dispatchers.IO){
            initView()
            isLoading = true
        }
    }

    private suspend fun initView() {
        val conditionMap: Map<String, List<String>> = mapOf(
            "分类1" to listOf("value1", "value2", "value2", "value2", "value2"),
            "分类2" to listOf("value3", "value4", "value4", "value4", "value4", "value4", "value4", "value4"),
            "分类3" to listOf("value3", "value4", "value4", "value4"),
            "分类4" to listOf("value3", "value4", "value4", "value4", "value4"),
            "分类4" to listOf("value3", "value4", "value4", "value4", "value4", "value4", "value4"),
        )
        withContext(Dispatchers.Main){
            conditionView.addTabLine(conditionMap){
                Toaster.showLong("条件变化: " +Gson().toJson(conditionMap))
            }

            itemListView.init(
                XRecyclerView.Config()
                    .setViewModel(viewModel)
                    .setPullRefreshEnable(false)
                    .setPullUploadMoreEnable(true)
                    .setLayoutManager(GridLayoutManager(activity, HomeFilterFragment.HOME_SPAN_COUNT))
                    .setItemDecoration(GridItemDecoration(activity, HomeFilterFragment.HOME_SPAN_COUNT))
                    .setOnItemClickListener(object : XRecyclerView.OnItemClickListener {
                        override fun onItemClick(parent: RecyclerView, view: View, viewData: BaseViewData<*>, position: Int, id: Long) {
                            Toast.makeText(context, "条目点击: ${viewData.value}", Toast.LENGTH_SHORT).show()
                        }
                    })
                    .setOnItemChildViewClickListener(object : XRecyclerView.OnItemChildViewClickListener {
                        override fun onItemChildViewClick(parent: RecyclerView, view: View, viewData: BaseViewData<*>, position: Int, id: Long, extra: Any?) {
                            if (extra is String) {
                                Toast.makeText(context, "条目子View点击: $extra", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            )

            viewModel.loadData()
            viewBinding.loadPromptView.hide()
        }
    }

    @PageName
    override fun getPageName() = PageName.FILTER

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
