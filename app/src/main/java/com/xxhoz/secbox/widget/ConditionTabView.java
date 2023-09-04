package com.xxhoz.secbox.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.xxhoz.secbox.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <ConditionTab>
 * <ConditionTab>
 *
 * @author DengNanYu
 * @version 1.0_2022/12/8
 * @date 2022/12/8 17:46
 */
public class ConditionTabView extends LinearLayout implements TabLayout.OnTabSelectedListener {
    private final Map<String, String> conditions = new HashMap<>();
    private Callback callback;


    public ConditionTabView(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
    }

    public ConditionTabView(Context context, AttributeSet attrs) {
        super(context,attrs);
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        getParent().requestDisallowInterceptTouchEvent(true);
    }

    public void addTabLine(Map<String, List<String>> conditionMap, Callback callback){
        this.callback = callback;
        for (Map.Entry<String, List<String>> stringListEntry : conditionMap.entrySet()) {
            String k = stringListEntry.getKey();
            List<String> v = stringListEntry.getValue();

            // 第一次保存第一个条件
            conditions.put(k,v.get(0));

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.view_conditiontab_item, null);
            // 设置tab key
            ((TextView) inflate.findViewById(R.id.tab_key)).setText(k);
            // 添加value
            TabLayout tabLayout = inflate.findViewById(R.id.tab_values);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

            for (String key : v) {
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setCustomView(R.layout.view_conditiontab_item_view);
                TextView textView = tab.getCustomView().findViewById(R.id.tab_text);
                textView.setText(key);
                //textView.setTextColor(R.color.ThemeColor);
                tab.setTag(k);
                tabLayout.addTab(tab);
            }
            this.addView(inflate);


            // 添加回调
            tabLayout.addOnTabSelectedListener(this);
        }

        // 第一次回调通知 用用首次加载
        callback.conditionChange(conditions);

    }

    /**
     * 返回所选中的条件map
     */
    public Map<String, String> getConditions(){
        return this.conditions;
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        TextView value = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
        conditions.put(tab.getTag().toString(),value.getText().toString());
        callback.conditionChange(conditions);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public interface Callback{
        void conditionChange(Map<String,String> conditions);
    }
}
