package com.xlab.vbrowser.z;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.android.y.dom.Drag;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.activity.MainActivity;
import com.xlab.vbrowser.utils.UrlConstants;
import com.xlab.vbrowser.z.module.DragView;

public class Z {
    public static void openURLTop(String url, Context context){
        if(!Settings.canDrawOverlays(context)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            context.startActivity(intent);
            return;
        }
    }
    public static void openURL(String url, Context context){
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        if(url!=null) intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
    public static String parseUri(Intent intent) {
        String uri = intent.getDataString();
        if(uri != null){
            return parseUri(uri);
        }
        return UrlConstants.getHomeUrl();
    }
    public static String parseUri(String url){
        if(url.startsWith("ko:")){
            Uri uri = Uri.parse(url);
            url = uri.getQueryParameter("url");
            if(url!=null && !url.isEmpty()){
                return url;
            }else{
                return UrlConstants.getHomeUrl();
            }
        }
        return url;
    }
    public static Boolean isFloating(Activity context){
        return context.getIntent() != null && Intent.ACTION_VIEW.equals(context.getIntent().getAction());
    }
    public static void setFloating(Activity context){
        if (isFloating(context)){
            context.setTheme(R.style.FloatingTheme);
            Window window = context.getWindow();
            if (window != null) {
                setWindowFloating(context, window);
            }
        }
    }
    public static void setWindowFloating(Activity context, Window window){
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        window.setLayout((int) (0.75*width), (int) (0.75*height));
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    public static void setDrag(Activity context, View view){
        new DragView(context, view);
    }
    public static Boolean isLock(Activity context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean lock = pref.getBoolean("pinlock", false);
        return lock;
    }
    public static void setLock(Activity context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean lock = pref.getBoolean("pinlock", false);
        FrameLayout container = (FrameLayout) context.findViewById(R.id.pin_lock_container);
        if(!lock) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        String pincode = pref.getString("pin", null);
        if(pincode == null || !pincode.matches("^\\d+$")) pincode = PIN.PIN;
        PinLockView pinLockView = (PinLockView) context.findViewById(R.id.pin_lock_view);
        IndicatorDots mIndicatorDots = (IndicatorDots) context.findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(mIndicatorDots);
        pinLockView.resetPinLockView();
        String finalPincode = pincode;
        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if(pin.equals(finalPincode)){
                    container.setVisibility(View.GONE);
                }else{
                    (new Handler(context.getMainLooper())).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 400);
                }
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });
    }
}
