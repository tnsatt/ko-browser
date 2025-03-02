package com.xlab.vbrowser.z.module;


import android.app.Activity;
import android.widget.Toast;

public class Back {
    public static final long interval = 500;
    public static long time = 0;
    public static void back(Activity context){
        long time = System.currentTimeMillis();
        if(time - Back.time < interval){
            context.finish();
            return;
        }
        Back.time = time;
        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show();
    }
}
