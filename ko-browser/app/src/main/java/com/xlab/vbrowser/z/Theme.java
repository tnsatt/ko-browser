package com.xlab.vbrowser.z;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import com.xlab.vbrowser.R;
import com.xlab.vbrowser.utils.Settings;

public class Theme {

    public static void setDarkMode(Activity context){
        int mode = Settings.getInstance(context).getDarkMode();
        if (mode > 0){
            boolean isDarkmode = mode == 1;
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(context.UI_MODE_SERVICE);
            boolean isSystemDarkmode = uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
            if (isDarkmode == isSystemDarkmode) {
                if (isDarkmode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }else{
                if (isDarkmode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    ((AppCompatActivity) context).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    ((AppCompatActivity) context).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
//            context.setTheme(isDarkmode? R.style.AppTheme_Dark:R.style.AppTheme);
        }
    }
    public static void setDarkmode(Context context){
        int mode = Settings.getInstance(context).getDarkMode();
        if(mode == 0) return;
        if(mode == 1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
   
}
