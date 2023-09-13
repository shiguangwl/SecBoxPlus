package com.xxhoz.secbox.module.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.base.list.XRecyclerView
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeFilterBinding
import com.xxhoz.secbox.module.player.DetailPlayerActivity
import com.xxhoz.secbox.parserCore.bean.CategoryBean
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.widget.ConditionTabView
import com.xxhoz.secbox.widget.GridItemDecoration


/**
 * 首页
 */
class HomeFilterFragment(val category: CategoryBean.ClassType, val categoryFilters: List<CategoryBean.Filter>) : BaseFragment<FragmentHomeFilterBinding>() {

    val conditionView: ConditionTabView by lazy { viewBinding.conditionView }

    val itemListView: XRecyclerView by lazy() { viewBinding.itemListView }

    private val viewModel: HomeFilterViewModel by viewModels()

    private var isLoading = false

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeFilterBinding
        get() = FragmentHomeFilterBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.loadPromptView.showLoading()
        initView()
        viewBinding.loadPromptView.hide()
        isLoading = true
    }



    @MainThread
    private fun initView() {
        LogUtils.d("当前分类:${category.type_id} | ${category.type_name}")
        LogUtils.d("当前Filter:${categoryFilters}")

        viewModel.category = category
        // 渲染赛选条件
        conditionView.addTabLine(categoryFilters){
            viewModel.conditons = it
            itemListView.startRefresh()
        }
        viewModel.conditons = conditionView.conditions

        itemListView.init(
            XRecyclerView.Config()
                .setViewModel(viewModel)
                .setPullRefreshEnable(true)
                .setPullUploadMoreEnable(true)
                .setLayoutManager(GridLayoutManager(activity, HomeFilterFragment.HOME_SPAN_COUNT))
                .setItemDecoration(GridItemDecoration(activity, HomeFilterFragment.HOME_SPAN_COUNT))
                .setOnItemClickListener(object : XRecyclerView.OnItemClickListener {
                    override fun onItemClick(parent: RecyclerView, view: View, viewData: BaseViewData<*>, position: Int, id: Long) {

                        val playInfoBean = PlayInfoBean(
                            XKeyValue.getString(Key.CURRENT_SOURCE_KEY, BaseConfig.DefualtSourceKey),
                            viewData.value as VideoBean,
                            1
                        )
                        LogUtils.i("条目点击: ${playInfoBean}")
                        DetailPlayerActivity.startActivity(context!!, playInfoBean)
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

    }

    @PageName
    override fun getPageName() = PageName.FILTER

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
