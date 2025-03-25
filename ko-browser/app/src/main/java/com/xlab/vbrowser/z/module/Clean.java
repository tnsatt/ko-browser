package com.xlab.vbrowser.z.module;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.xlab.vbrowser.quickdial.service.QuickDialService;
import com.xlab.vbrowser.utils.BackgroundTask;
import com.xlab.vbrowser.utils.IBackgroundTask;
import com.xlab.vbrowser.z.utils.Toast;

public class Clean {
    public static void reset(Context context){
        QuickDialService.clear(context);
    }

    public static void clean(Context context){
        reset(context);
    }
    
    public static void confirmReset(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reset")
                .setMessage("Want to reset settings?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new BackgroundTask(new IBackgroundTask() {
                            @Override
                            public void run() {
                                reset(context);
                            }

                            @Override
                            public void onComplete() {
                                Toast.makeText(context, "Reset Success", Toast.LENGTH_SHORT).show();
                            }
                        }).execute();
                    }
                }).show();
    }

    public static void confirmClean(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Clean")
                .setMessage("Want to clean browser?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new BackgroundTask(new IBackgroundTask() {
                            @Override
                            public void run() {
                                clean(context);
                            }

                            @Override
                            public void onComplete() {
                                Toast.makeText(context, "Clean Success", Toast.LENGTH_SHORT).show();
                            }
                        }).execute();
                    }
                }).show();
    }
}
