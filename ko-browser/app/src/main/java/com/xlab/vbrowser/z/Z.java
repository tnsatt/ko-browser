package com.xlab.vbrowser.z;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;

import com.android.ui.UiUtils;
import com.dom.lock.Lock;
import com.dom.lock.PIN;
import com.dom.ui.PinLockFragment;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.activity.MainActivity;
import com.xlab.vbrowser.utils.UrlConstants;
import com.android.dom.DragView;

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
    private static void setWindowFloating(Activity context, Window window){
        UiUtils.setWindowFloating(context, window, 0.75F, 0.75F);
    }
    public static void setDrag(Activity context, View view){
        if (Z.isFloating(context)) {
            new DragView(context, view);
        }
    }
    public static Boolean isLock(Activity context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean lock = pref.getBoolean("pinlock", false);
        return lock && isCreatedPin(context);
    }
    public static void showCreatePinScreen(Activity context){
        Lock.showCreatePinScreen(context);
    }
    public static void showLockScreen(Activity context){
        showLockScreen(context, 0);
    }
    public static void showLockScreen(Activity context, int code){
        showLockScreen(context, null, code);
    }
    public static void showLockScreen(Activity context, String action, int code) {
        if(false && context instanceof MainActivity){
            MainActivity main = (MainActivity) context;
            Lock.showFragment(main, PIN.MODE_AUTH, R.id.lock_container, new PinLockFragment.OnLock() {
                @Override
                public void onUnlock() {

                }

                @Override
                public void onCreatedLock() {

                }

                @Override
                public void onFailedUnlock() {

                }
            });
            return;
        }
        Lock.showLockScreen(context, action, code);
    }
    public static Boolean isCreatedPin(Context context){
        return Lock.isCreatedPin(context);
    }
    
    //    public static Boolean isLock(Activity context){
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        Boolean lock = pref.getBoolean("pinlock", false);
//        String enc = pref.getString("encodedPin", null);
//        return lock && enc!=null;
//    }
//    public static void showCreatePinScreen(Activity context){
//        showLockScreen(context, PinLockActivity.CREATE_PIN, 0);
//    }
//    public static void showLockScreen(Activity context){
//        showLockScreen(context, 0);
//    }
//    public static void showLockScreen(Activity context, int code){
//        showLockScreen(context, null, code);
//    }
//    public static void showLockScreen(Activity context, String action, int code){
//        Intent intent = new Intent(context, PinLockActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        if(action!=null) intent.setAction(action);
//        if(code>0) context.startActivityForResult(intent, code);
//        else context.startActivity(intent);
//    }
//        public static Boolean isCreatedPin(Context context){
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String enc = sharedPreferences.getString("encodedPin", null);
//            return enc!=null;
//        }
}
