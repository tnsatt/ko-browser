package com.xlab.vbrowser.z.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.customtabs.CustomTabConfig;
import com.xlab.vbrowser.fragment.BrowserFragment;
import com.xlab.vbrowser.menu.browser.BlockingItemViewHolder;
import com.xlab.vbrowser.menu.browser.BrowserMenu;
import com.xlab.vbrowser.menu.browser.BrowserMenuViewHolder;
import com.xlab.vbrowser.menu.browser.NavigationItemViewHolder;
import com.xlab.vbrowser.session.Session;
import com.xlab.vbrowser.session.SessionManager;
import com.xlab.vbrowser.utils.Settings;
import com.xlab.vbrowser.z.fragment.menu.GridBrowserMenu;
import com.xlab.vbrowser.z.module.Menu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GridBrowserMenuAdapter extends RecyclerView.Adapter<BrowserMenuViewHolder>  {
    static interface IMenuItemAction {
        void onPerformAction();
    }

    public static class MenuItem {
        public final int id;
        public final String label;
        public final @Nullable
        PendingIntent pendingIntent;
        public IMenuItemAction menuAction;
        public boolean isEnabled = true;
        public Drawable icon;
        public boolean disabled;

        public MenuItem(Drawable icon, int id, String label, boolean disabled) {
            this.id = id;
            this.label = label;
            this.menuAction = null;
            this.pendingIntent = null;
            this.icon = icon;
            this.disabled = disabled;
        }

        public MenuItem(Drawable icon, int id, String label) {
            this.id = id;
            this.label = label;
            this.menuAction = null;
            this.pendingIntent = null;
            this.icon = icon;
        }

        public MenuItem(Drawable icon, int id, String label, IMenuItemAction menuAction) {
            this.id = id;
            this.label = label;
            this.menuAction = menuAction;
            this.pendingIntent = null;
            this.icon = icon;
        }

        public MenuItem(Drawable icon, int id, String label, @Nullable PendingIntent pendingIntent) {
            this.id = id;
            this.label = label;
            this.menuAction = null;
            this.pendingIntent = pendingIntent;
            this.icon = icon;
        }

        public void setEnabled(boolean enabled) {
            this.isEnabled = enabled;
        }
    }

    private final Context context;
    private final GridBrowserMenu menu;
    private final BrowserFragment fragment;

    private List<GridBrowserMenuAdapter.MenuItem> items;
    private WeakReference<NavigationItemViewHolder> navigationItemViewHolderReference;
    private final SharedPreferences prefs;

    public GridBrowserMenuAdapter(Context context, GridBrowserMenu menu, BrowserFragment fragment,
                                  final @Nullable CustomTabConfig customTabConfig) {
        this.context = context;
        this.menu = menu;
        this.fragment = fragment;

        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);

//        initializeMenu(fragment.getUrl(), customTabConfig);
    }

    public void setMenu(List<GridBrowserMenuAdapter.MenuItem> items){
        this.items = items;
    }

    private void initializeMenu(String url, final @Nullable CustomTabConfig customTabConfig) {
        this.items = Menu.createMenu(context, url, customTabConfig);
    }

    public void updateLoading(boolean loading) {
        if (navigationItemViewHolderReference == null) {
            return;
        }

        final NavigationItemViewHolder navigationItemViewHolder = navigationItemViewHolderReference.get();
        if (navigationItemViewHolder != null) {
            navigationItemViewHolder.updateLoading(loading);
        }
    }

    @Override
    public BrowserMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new GridMenuItemViewHolder(inflater.inflate(R.layout.grid_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(BrowserMenuViewHolder holder, int position) {
        holder.setMenu(menu);
        holder.setOnClickListener(fragment);

        if (position >= 0) {
            ((GridMenuItemViewHolder) holder).bind(items.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        int itemCount = items.size();
        return itemCount;
    }
}
