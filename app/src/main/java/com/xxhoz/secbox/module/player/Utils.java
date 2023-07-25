package com.xxhoz.secbox.module.player;

import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {
    public static float dp2px(float paramFloat) {
        return TypedValue.applyDimension(1, paramFloat, Resources.getSystem().getDisplayMetrics());
    }
}
