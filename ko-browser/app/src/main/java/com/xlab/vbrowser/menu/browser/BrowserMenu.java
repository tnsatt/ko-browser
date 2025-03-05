/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.menu.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.xlab.vbrowser.customtabs.CustomTabConfig;
import com.xlab.vbrowser.utils.ViewUtils;
import com.xlab.vbrowser.fragment.BrowserFragment;

/**
 * The overflow menu shown in the BrowserFragment containing page actions like "Refresh", "Share" etc.
 */
public class BrowserMenu extends PopupWindow {
    private BrowserMenuAdapter adapter;

    public BrowserMenu(Context context, BrowserFragment fragment, final @Nullable CustomTabConfig customTabConfig) {
        @SuppressLint("InflateParams") // This View will have it's params ignored anyway:
        final View view = LayoutInflater.from(context).inflate(com.xlab.vbrowser.R.layout.menu, null);
        setContentView(view);

        adapter = new BrowserMenuAdapter(context, this, fragment, customTabConfig);

        RecyclerView menuList = view.findViewById(com.xlab.vbrowser.R.id.list);
        menuList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        menuList.setAdapter(adapter);

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setFocusable(true);

        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        setElevation(context.getResources().getDimension(com.xlab.vbrowser.R.dimen.menu_elevation));
    }

    public BrowserMenu() {
    }

    public void updateTrackers(int trackers) {
        adapter.updateTrackers(trackers);
    }

    public void updateLoading(boolean loading) {
        adapter.updateLoading(loading);
    }

    public void show(View anchor) {
        final int xOffset = ViewUtils.isRTL(anchor) ? -anchor.getWidth() : 0;

//        super.showAsDropDown(anchor, xOffset, -(anchor.getHeight() + anchor.getPaddingBottom()));
        super.showAtLocation(anchor, Gravity.BOTTOM|Gravity.RIGHT, 0, anchor.getHeight());
    }
}
