package com.xlab.vbrowser.z.module;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.Log;

import com.xlab.vbrowser.R;

public class ThemeColors {
    protected static final String DEFAULT_COLOR = "1c1c1d";
    protected static final String NAME = "ThemeColors";
    protected static final String KEY = "theme_color";
    protected static final String PREFIX = "T_";
    Context context;
    String name;
    String key;
    String prefix;
    Activity activity;

    public static ThemeColors getInstance(Context context){
        return new ThemeColors(context, NAME, KEY, PREFIX);
    }

    public ThemeColors(Context context, String name, String key, String prefix) {
        this.context = context;
        this.name = name;
        this.key = key;
        this.prefix = prefix;
        this.activity = (Activity) context;
    }
    public boolean setTheme(){
        Log.d("theme", "setTheme");
        Log.d("theme", "NAME "+name);
        Log.d("theme", "KEY "+key);
        String stringColor = getSavedColor();
        if(stringColor == null) return false;
        while(stringColor.startsWith("#")){
            stringColor = stringColor.substring(1);
        }
        int color = Color.parseColor("#" + stringColor);

        if (isLightActionBar(color)) context.setTheme(R.style.AppTheme);
        int theme = context.getResources().getIdentifier(PREFIX + stringColor, "style", context.getPackageName());
        if(theme <= 0) return false;
        context.setTheme(theme);
        return true;
    }

    public int getSavedColorInt(){
        String stringColor = getSavedColor();
        if(stringColor == null) return -1;
        int color = Color.parseColor("#" + stringColor);
        return color;
    }

    public String getSavedColor(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString(key, null);
        if(stringColor == null) return null;
        while(stringColor.startsWith("#")){
            stringColor = stringColor.substring(1);
        }
        return stringColor;
    }

    public void setColor(String stringColor){
        SharedPreferences.Editor editor = activity.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.putString(key, stringColor);
        editor.apply();
    }

    public void setNewThemeColor(int red, int green, int blue) {
        int colorStep = 15;
        red = Math.round(red / colorStep) * colorStep;
        green = Math.round(green / colorStep) * colorStep;
        blue = Math.round(blue / colorStep) * colorStep;

        String stringColor = Integer.toHexString(Color.rgb(red, green, blue)).substring(2);
        setNewThemeColor(stringColor);
    }
    public void setNewThemeColor(int color){
        String stringColor = Integer.toHexString(color).substring(2);
        setNewThemeColor(stringColor);
    }
    public void setNewThemeColor(String stringColor){
        setColor(stringColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) activity.recreate();
        else {
            Intent i = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
        }
    }

    public static boolean isLightActionBar(int color) {// Checking if title text color will be black
        int rgb = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
        return rgb > 210;
    }
    public void clearColor(){
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.remove(key);
        editor.apply();
    }
}
