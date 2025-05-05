package com.xlab.vbrowser.z.service;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.Z;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QSBrowserService extends TileService {
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateServiceState();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateServiceState();
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
        updateServiceState();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        Z.openURL(null, this);
    }
    public void updateState(boolean on){
        String title = getResources().getString(R.string.app_name);
        if(on){
            update(true, title, title);
        }else{
            update(false, title, title);
        }
    }

    public void update(Boolean on, String title, String desc, int iconId){
        Tile tile = getQsTile();
        if(tile==null) return;
        tile.setState(on?Tile.STATE_ACTIVE:Tile.STATE_INACTIVE);
        tile.setLabel(title);
        tile.setContentDescription(desc);
        tile.setIcon(Icon.createWithResource(this, iconId));
        tile.updateTile();
    }
    public void update(Boolean on, String title, String desc){
        update(on, title, desc, R.drawable.ic_ghost_blue);
    }
    public void updateServiceState(){
//        updateState();
    }
}
