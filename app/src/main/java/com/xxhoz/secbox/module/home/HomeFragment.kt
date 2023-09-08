package com.xxhoz.secbox.module.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xxhoz.constant.BaseConfig.DefualtSourceKey
import com.xxhoz.constant.Key
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.base.list.XRecyclerView
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.bean.PlayInfoBean
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeBinding
import com.xxhoz.secbox.module.player.DetailPlayerActivity
import com.xxhoz.secbox.parserCore.bean.VideoBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.LogUtils
import com.xxhoz.secbox.widget.GridItemDecoration

/**
 * 首页
 */
class HomeFragment : BaseFragment<FragmentHomeBinding> {

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
    }

    private fun initView() {
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
                        val playInfoBean: PlayInfoBean = PlayInfoBean(
                            XKeyValue.getString(Key.CURRENT_SOURCE_KEY, DefualtSourceKey),
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

        // 更新数据
        homeVideoList?.let {
            viewModel.loadData(it)
        }

//        XEventBus.observe(viewLifecycleOwner, EventName.REFRESH_HOME_LIST) { message: String ->
//            viewBinding.rvList.refreshList()
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//        }
//
//        XEventBus.observe(viewLifecycleOwner, EventName.TEST) { message: String ->
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//        }
    }

    @PageName
    override fun getPageName() = PageName.HOME

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
