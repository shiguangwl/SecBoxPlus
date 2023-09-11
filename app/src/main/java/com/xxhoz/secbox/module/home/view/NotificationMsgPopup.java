package com.xxhoz.secbox.module.home.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.PositionPopupView;
import com.xxhoz.secbox.R;


/**
 * Description: 自定义自由定位Position弹窗
 * Create by dance, at 2019/6/14
 */
public class NotificationMsgPopup extends PositionPopupView {
    public NotificationMsgPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_notification_msg;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.tvClose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected int getPopupWidth() {
        // 宽度占满全屏
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public void setMsg(String msg){
        ((TextView) findViewById(R.id.notice_text)).setText(msg);
    }
}
