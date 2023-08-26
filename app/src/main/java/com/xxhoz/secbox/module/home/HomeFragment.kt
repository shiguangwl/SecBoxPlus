package com.xxhoz.secbox.module.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xxhoz.secbox.base.BaseFragment
import com.xxhoz.secbox.base.list.XRecyclerView
import com.xxhoz.secbox.base.list.base.BaseViewData
import com.xxhoz.secbox.constant.EventName
import com.xxhoz.secbox.constant.PageName
import com.xxhoz.secbox.databinding.FragmentHomeBinding
import com.xxhoz.secbox.eventbus.XEventBus
import com.xxhoz.secbox.module.player.AGVideoActivity
import com.xxhoz.secbox.widget.GridItemDecoration

/**
 * 首页
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    companion object {
        private const val HOME_SPAN_COUNT = 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
//        val sourceBeanList = SourceManger.getSourceBeanList()
//        LogUtils.i("获取站源数量:${sourceBeanList.size}")
//
//        sourceBeanList.forEach {
//            lifecycleScope.launch(Dispatchers.IO) {
//                val source = SourceManger.getSource(it.key)!!
//                try {
//                    LogUtils.e("站源数据:${it.key}====homeVideoList:${source.homeVideoList()}===categoryInfo:${source.categoryInfo()}")
//                }catch (e:Exception){
//                    LogUtils.e("站源数据异常:${it.key},${e}")
//                }
//
//            }
//        }

    }

    private fun initView() {
        viewBinding.rvList.init(
            XRecyclerView.Config()
                .setViewModel(viewModel)
                .setPullRefreshEnable(false)
                .setPullUploadMoreEnable(true)
                .setLayoutManager(GridLayoutManager(activity, HOME_SPAN_COUNT))
                .setItemDecoration(GridItemDecoration(activity, HOME_SPAN_COUNT))
                .setOnItemClickListener(object : XRecyclerView.OnItemClickListener {
                    override fun onItemClick(parent: RecyclerView, view: View, viewData: BaseViewData<*>, position: Int, id: Long) {
                        Toast.makeText(context, "条目点击: ${viewData.value}", Toast.LENGTH_SHORT).show()
                        AGVideoActivity.startActivity(getActivity())

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

        XEventBus.observe(viewLifecycleOwner, EventName.REFRESH_HOME_LIST) { message: String ->
            viewBinding.rvList.refreshList()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        XEventBus.observe(viewLifecycleOwner, EventName.TEST) { message: String ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @PageName
    override fun getPageName() = PageName.HOME

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 这里可以添加页面打点
    }
}
