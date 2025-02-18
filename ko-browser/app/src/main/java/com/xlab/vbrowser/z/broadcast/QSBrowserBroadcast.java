package com.xlab.vbrowser.z.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import com.xlab.vbrowser.z.service.QSBrowserService;

public class QSBrowserBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        String action = intent.getAction();
        if(action==null) return;
        if(action.equals(Intent.ACTION_MY_PACKAGE_REPLACED) || action.equals(Intent.ACTION_BOOT_COMPLETED)
        ){

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(context, new ComponentName(context, QSBrowserService.class));
        }
    }
}
