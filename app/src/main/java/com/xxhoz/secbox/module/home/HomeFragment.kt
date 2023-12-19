package com.xxhoz.secbox.module.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xxhoz.constant.BaseConfig.DefaultSourceKey
import com.xxhoz.constant.Key
import com.xxhoz.secbox.R
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.base.list.XRecyclerView
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeBinding
import com.xxhoz.secbox.module.detail.DetailPlayerActivity
import com.xxhoz.secbox.module.history.HistoryActivity
import com.xxhoz.secbox.module.search.SearchActivity
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.widget.GridItemDecoration

/**
 * 首页
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>, OnClickListener {

    private val viewModel: HomeViewModel by viewModels()
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }

    private var homeVideoList: List<VideoBean>? = null
    constructor(homeVideoList: List<VideoBean>) : super(){
        this.homeVideoList = homeVideoList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        viewBinding.cardViewSearch.setOnClickListener(this)
        viewBinding.cardViewHistory.setOnClickListener(this)

        viewBinding.rvList.init(
            XRecyclerView.Config()
                .setViewModel(viewModel)
                .setShowScrollBar(false)
                .setPullRefreshEnable(false)
                .setPullUploadMoreEnable(true)
                .setLayoutManager(GridLayoutManager(activity, HOME_SPAN_COUNT))
                .setItemDecoration(GridItemDecoration(activity, HOME_SPAN_COUNT))
                .setOnItemClickListener(object : XRecyclerView.OnItemClickListener {
                    override fun onItemClick(parent: RecyclerView, view: View, viewData: BaseViewData<*>, position: Int, id: Long) {
                        onVideoItemClick(viewData, view)
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


    fun initData() {
        // 加载推荐数据
        homeVideoList?.let {
            viewModel.loadData(it)
        }
    }

    /**
     * 条目点击
     */
    private fun onVideoItemClick(viewData: BaseViewData<*>, view: View) {
        val playInfoBean: PlayInfoBean = PlayInfoBean(
            XKeyValue.getString(Key.CURRENT_SOURCE_KEY, DefaultSourceKey),
            viewData.value as VideoBean,
            0
        )
        DetailPlayerActivity.startActivity(requireContext(), playInfoBean, view)
    }

    @PageName
    override fun getPageName() = PageName.HOME

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cardView_search -> {
                SearchActivity.startActivity(requireContext())
            }

            R.id.cardView_history -> {
                HistoryActivity.startActivity(requireContext())
            }

        }
    }

}
