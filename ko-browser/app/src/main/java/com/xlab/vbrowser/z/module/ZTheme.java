package com.xlab.vbrowser.z.module;

import android.content.Context;

import com.xlab.vbrowser.z.Z;

public class ZTheme extends ThemeColors {
    protected static final String NAME = "ZThemeColors";
    protected static final String KEY = "theme_color";
    protected static final String PREFIX = "Z_";

    public static ZTheme getInstance(Context context){
        return new ZTheme(context, NAME, KEY, PREFIX);
    }

    public ZTheme(Context context, String name, String key, String prefix) {
        super(context, name, key, prefix);
    }
}
