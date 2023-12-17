package com.xxhoz.danmuplayer.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.xxhoz.danmuplayer.popup.DmSetting;

public class DmSettingContext {
    private final Context mContext;

    public DmSettingContext(Context context) {
        mContext = context;
    }

    private void saveData(String key, String value) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("dm_setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String loadData(String key, String defaultValue) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("dm_setting", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }


    public DmSetting loadDmSetting() {
        DmSetting dmSetting = new DmSetting();
        String dm_scrollSpeed = loadData("dm_scrollSpeed", "");
        if (!"".equals(dm_scrollSpeed)) {
            dmSetting.setScrollSpeed(Float.parseFloat(dm_scrollSpeed));
        }

        String dm_scaleTextSize = loadData("dm_scaleTextSize", "");
        if (!"".equals(dm_scaleTextSize)) {
            dmSetting.setScaleTextSize(Float.parseFloat(dm_scaleTextSize));
        }

        String dm_maximumLines = loadData("dm_maximumLines", "");
        if (!"".equals(dm_maximumLines)) {
            dmSetting.setMaximumLines(Integer.parseInt(dm_maximumLines));
        }

        return dmSetting;
    }

    public void saveDmSetting(DmSetting dmSetting) {
        saveData("dm_scrollSpeed", String.valueOf(dmSetting.getScrollSpeed()));
        saveData("dm_scaleTextSize", String.valueOf(dmSetting.getScaleTextSize()));
        saveData("dm_maximumLines", String.valueOf(dmSetting.getMaximumLines()));
    }
}
