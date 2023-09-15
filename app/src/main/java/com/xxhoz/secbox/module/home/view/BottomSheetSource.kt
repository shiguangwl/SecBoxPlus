
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.xxhoz.constant.BaseConfig
import com.xxhoz.constant.Key
import com.xxhoz.parserCore.SourceManger
import com.xxhoz.parserCore.parserImpl.IBaseSource
import com.xxhoz.secbox.R
import com.xxhoz.secbox.constant.EventName
import com.xxhoz.secbox.databinding.ItemSelectSourceBinding
import com.xxhoz.secbox.eventbus.XEventBus
import com.xxhoz.secbox.parserCore.bean.SourceBean
import com.xxhoz.secbox.persistence.XKeyValue
import com.xxhoz.secbox.util.UniversalAdapter


class BottomSheetSource(context: Context) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.sheet_bottom_sourace_layout
    }

    override fun onCreate() {
        super.onCreate()
        val recyclerViewChooseSource = findViewById<RecyclerView>(R.id.recyclerView_choose_source)

        val sourceBeanList: List<SourceBean> = SourceManger.getSourceBeanList()
        val currentSource: IBaseSource? = BaseConfig.getCurrentSource()
        recyclerViewChooseSource.layoutManager = GridLayoutManager(context, 2)
        recyclerViewChooseSource.adapter = UniversalAdapter(
            sourceBeanList,
            R.layout.item_select_source,
            object : UniversalAdapter.DataViewBind<SourceBean> {
                override fun exec(data: SourceBean, view: View) {
                    val bind = ItemSelectSourceBinding.bind(view)
                    bind.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                    if (currentSource != null && data.key.equals(currentSource.sourceBean.key)) {
                        bind.root.setBackgroundColor(ContextCompat.getColor(context,R.color.theme_color))
                    }
                    bind.sourceItemText.text = data.name
                    view.setOnClickListener(){
                        selectItem(data)
                    }
                }
            })
    }


    /**
     * 选择源
     */
    private fun selectItem(data: SourceBean) {
        XKeyValue.putString(Key.CURRENT_SOURCE_KEY, data.key)
        dismiss()
        XEventBus.post(EventName.SOURCE_CHANGE, "源切换: ${data.name}")
    }

    // 最大高度为Window的0.85
    override fun getMaxHeight(): Int {
        return (XPopupUtils.getAppHeight(context) * .8f).toInt()
    }
}
