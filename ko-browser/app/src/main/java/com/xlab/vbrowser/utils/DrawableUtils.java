/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;


public class DrawableUtils {
    public static Drawable loadAndTintDrawable(@NonNull Context context, @DrawableRes int resourceId, @ColorInt  int color) {
        final Drawable drawable = context.getResources().getDrawable(resourceId, context.getTheme());
        final Drawable wrapped = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrapped, color);
        return wrapped;
    }
}
