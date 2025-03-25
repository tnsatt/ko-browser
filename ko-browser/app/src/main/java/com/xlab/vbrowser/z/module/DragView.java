package com.xlab.vbrowser.z.module;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.xlab.vbrowser.z.utils.Toast;

import com.android.y.dom.Drag;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.Z;

public class DragView {
    public Activity context;
    public View view;
    public Boolean fullscreen = true;

    public DragView(Activity context, View view){
        this.context = context;
        this.view = view;
        run();
    }
    private void run(){
        if (Z.isFloating(context)) {
            fullscreen = false;
            FrameLayout buttons = (FrameLayout) view!=null?view.findViewById(R.id.floatButtons):context.findViewById(R.id.floatButtons);
            View drag = view!=null?view.findViewById(R.id.drag):context.findViewById(R.id.drag);
            if(buttons!=null) {
                buttons.setVisibility(View.VISIBLE);
                new Drag(context, drag);

                int height = 90;
                FrameLayout container = (FrameLayout) context.findViewById(R.id.container);
                LinearLayout progessView = (LinearLayout) context.findViewById(R.id.load_sessions_progress_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                params.topMargin = height;
                container.setLayoutParams(params);
                params = (ViewGroup.MarginLayoutParams) progessView.getLayoutParams();
                params.topMargin = height;
                progessView.setLayoutParams(params);
            }
            ImageView close = view!=null?view.findViewById(R.id.close):context.findViewById(R.id.close);
            if(close!=null){
                long[] time = new long[]{0};
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long t = System.currentTimeMillis();
                        if(t - time[0] > 500){
                            time[0] = t;
                            close.setImageDrawable(context.getDrawable(R.drawable.ic_exit_to_app));
                            Toast.makeText(context, "Press again to close", Toast.LENGTH_SHORT);
                            (new Handler(context.getMainLooper())).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    close.setImageDrawable(context.getDrawable(R.drawable.ic_close));
                                }
                            }, 500);
                            return;
                        }
                        context.finish();
                    }
                });
            }
            ImageView resize = view!=null?view.findViewById(R.id.resize):context.findViewById(R.id.resize);
            resize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fullscreen = !fullscreen;
                    if(fullscreen){
                        context.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        resize.setImageDrawable(context.getDrawable(R.drawable.ic_fullscreen_exit));
                    }else{
                        Z.setWindowFloating(context, context.getWindow());
                        resize.setImageDrawable(context.getDrawable(R.drawable.ic_fullscreen));
                    }
                }
            });
        }
    }
}
