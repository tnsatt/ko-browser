package com.xlab.vbrowser.z.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;

import com.xlab.vbrowser.R;

public class Toast{
    public static final int LENGTH_LONG = 1;
    public static final int LENGTH_SHORT = 0;
    public Toast(Context context) {

    }

    public static android.widget.Toast makeText(Context context, String msg, int duration){
        android.widget.Toast toast = android.widget.Toast.makeText(context, msg, duration);
        View view = toast.getView();
        view.getBackground().setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        TextView text = view.findViewById(android.R.id.message);
//        text.setTextColor(context.getResources().getColor(R.color.colorTextActive));
        text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_toast, 0, 0, 0);
        text.setCompoundDrawablePadding(16);
        return toast;
    }
}
