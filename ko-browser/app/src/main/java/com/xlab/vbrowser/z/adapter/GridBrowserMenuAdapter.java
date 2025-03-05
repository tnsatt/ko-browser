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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GridBrowserMenuAdapter extends RecyclerView.Adapter<BrowserMenuViewHolder>  {
    static interface IMenuItemAction {
        void onPerformAction();
    }

    static class MenuItem {
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

        initializeMenu(fragment.getUrl(), customTabConfig);
    }

    private void initializeMenu(String url, final @Nullable CustomTabConfig customTabConfig) {
        final Resources resources = context.getResources();
        //final Browsers browsers = new Browsers(context, url);
        Settings settings = Settings.getInstance(context);

        this.items = new ArrayList<>();

        items.add(new MenuItem(context.getDrawable(R.drawable.ic_shortcut_s),
                R.id.add_to_homescreen, resources.getString(R.string.menu_add_to_home_screen)));

        Session currentSession = SessionManager.getInstance().getCurrentSession();
        boolean wasNotAddedToQuickAccess = currentSession != null && !currentSession.wasAddedToQuickAccess();

        if(wasNotAddedToQuickAccess) {
            items.add(new MenuItem(context.getDrawable(R.drawable.ic_home_smile)
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access)));
        }else{
            items.add(new MenuItem(context.getDrawable(R.drawable.ic_home_s_disabled)
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access), true));
        }

        items.add(new MenuItem(context.getDrawable(R.drawable.ic_bookmark_s),
                R.id.bookmarkActivity, resources.getString(R.string.bookmark)));

        items.add(new MenuItem(context.getDrawable(R.drawable.ic_history_s),
                R.id.history, resources.getString(R.string.history)));

        items.add(new MenuItem(context.getDrawable(R.drawable.ic_download_s)
                ,R.id.download_manager, resources.getString(R.string.download_manager)));

        boolean enableDarkMode = settings.isEnabledDarkMode();
        Drawable darkModeDrawable = enableDarkMode ? context.getDrawable(R.drawable.ic_sun_s)
                :  context.getDrawable(R.drawable.ic_moon_s);
        items.add(new MenuItem(darkModeDrawable, R.id.darkMode, enableDarkMode ? resources.getString(R.string.light_mode)
                : resources.getString(R.string.dark_mode) ));

        boolean enableSpeedmode = settings.shouldEnterSpeedMode();
        Drawable speedModeDrawbale = enableSpeedmode ? context.getDrawable(R.drawable.ic_speedmode_off)
                :  context.getDrawable(R.drawable.ic_speed_alt_s);
        items.add(new MenuItem(speedModeDrawbale, R.id.speedMode, enableSpeedmode ? resources.getString(R.string.exit_speed_mode) :
                resources.getString(R.string.speed_mode) ));

        if (customTabConfig == null || customTabConfig.showShareMenuItem) {
            items.add(new MenuItem(context.getDrawable(R.drawable.ic_share_s), R.id.share, resources.getString(R.string.menu_share)));
        }else{
            items.add(new MenuItem(context.getDrawable(R.drawable.ic_share_s), R.id.share, resources.getString(R.string.menu_share), true));
        }

        //Request Desktop Site
        boolean isRequestingDesktopSite = settings.shouldRequestDesktopSite();
        Drawable requestDesktopDrawable = isRequestingDesktopSite ? context.getDrawable(R.drawable.ic_mobile_s)
                :  context.getDrawable(R.drawable.ic_desktop_s);
        items.add(new MenuItem(requestDesktopDrawable, R.id.requestDesktopSite, isRequestingDesktopSite ? resources.getString(R.string.request_mobile_site)
                : resources.getString(R.string.request_desktop_site)));

        items.add(new MenuItem(context.getDrawable(R.drawable.ic_setting_modern),
                R.id.settings, resources.getString(R.string.menu_settings)));

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
