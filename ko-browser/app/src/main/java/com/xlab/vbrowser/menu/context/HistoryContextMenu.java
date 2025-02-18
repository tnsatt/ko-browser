/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */
package com.xlab.vbrowser.menu.context;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.utils.Settings;

/**
 * The context menu shown when long pressing a URL or an image inside the WebView.
 */
public class HistoryContextMenu {
    public interface IActionMenu {
        void onOpen();
        void onOpenInNewTab();
        void onDelete();
        void onShareLink();
        void onCopyLink();
    }

    private static View createTitleView(final @NonNull Context context, final @NonNull String title, final Settings settings) {
        final TextView titleView = (TextView) LayoutInflater.from(context).inflate(R.layout.context_menu_title, null);
        titleView.setText(title);
        return titleView;
    }

    public static void show(final @NonNull Context context, final @NonNull String title, final IActionMenu actionMenu) {
        Settings settings = Settings.getInstance(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);

        final View titleView = createTitleView(context, title, settings);
        builder.setCustomTitle(titleView);

        final View view = LayoutInflater.from(context).inflate(R.layout.context_menu, null);
        builder.setView(view);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // This even is only sent when the back button is pressed, or when a user
                // taps outside of the dialog:
            }
        });

        final Dialog dialog = builder.create();

        NavigationView menu = view.findViewById(R.id.context_menu_light);

        if (settings.isEnabledNightMode()) {
            menu = view.findViewById(R.id.context_menu_dark);
        }

        menu.setVisibility(View.VISIBLE);

        setupMenu(dialog, menu, title, actionMenu);

        dialog.show();
    }

    /**
     * Set up the correct menu contents. Note: this method can only be called once the Dialog
     * has already been created - we need the dialog in order to be able to dismiss it in the
     * menu callbacks.
     */
    private static void setupMenu(final @NonNull Dialog dialog,
                                  final @NonNull NavigationView navigationView,
                                  final  @NonNull String title,
                                  final IActionMenu actionMenu) {
        navigationView.inflateMenu(R.menu.menu_history_context);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                dialog.dismiss();

                switch (item.getItemId()) {
                    case R.id.menu_open: {
                        actionMenu.onOpen();
                        return true;
                    }

                    case R.id.menu_open_in_new_tab: {
                        actionMenu.onOpenInNewTab();
                        return true;
                    }

                    case R.id.menu_share_link: {
                        actionMenu.onShareLink();
                        return true;
                    }

                    case R.id.menu_copy_link: {
                        actionMenu.onCopyLink();
                        return true;
                    }

                    case R.id.menu_delete: {
                        actionMenu.onDelete();
                        return true;
                    }
                    default:
                        return true;
                }
            }
        });
    }
}
