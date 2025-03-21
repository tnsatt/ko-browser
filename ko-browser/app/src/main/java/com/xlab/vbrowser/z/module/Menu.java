package com.xlab.vbrowser.z.module;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.customtabs.CustomTabConfig;
import com.xlab.vbrowser.session.Session;
import com.xlab.vbrowser.session.SessionManager;
import com.xlab.vbrowser.utils.Settings;
import com.xlab.vbrowser.z.adapter.GridBrowserMenuAdapter;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    public static List<GridBrowserMenuAdapter.MenuItem> createMenu(Context context, String url, final @Nullable CustomTabConfig customTabConfig){
        final Resources resources = context.getResources();
        //final Browsers browsers = new Browsers(context, url);
        Settings settings = Settings.getInstance(context);

        List<GridBrowserMenuAdapter.MenuItem> items = new ArrayList<>();

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_shortcut_s),
                R.id.add_to_homescreen, resources.getString(R.string.menu_add_to_home_screen)));

        Session currentSession = SessionManager.getInstance().getCurrentSession();
        boolean wasNotAddedToQuickAccess = currentSession != null && !currentSession.wasAddedToQuickAccess();

        if(wasNotAddedToQuickAccess) {
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_home_smile)
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access)));
        }else{
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_home_s_disabled)
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access), true));
        }

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_star_bookmark_s),
                R.id.bookmarkActivity, resources.getString(R.string.bookmark)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_history_s),
                R.id.history, resources.getString(R.string.history)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_download_s)
                ,R.id.download_manager, resources.getString(R.string.download_manager)));

        int darkMode = settings.getDarkMode();
        Drawable darkModeDrawable = darkMode == 0 ? context.getDrawable(R.drawable.ic_night_light_s): (darkMode!=1 ? context.getDrawable(R.drawable.ic_sun_s)
                :  context.getDrawable(R.drawable.ic_moon_s));
        items.add(new GridBrowserMenuAdapter.MenuItem(darkModeDrawable, R.id.darkMode,
                darkMode == 0 ? "Auto Dark mode" : darkMode != 1 ? resources.getString(R.string.light_mode) : resources.getString(R.string.dark_mode) ));

        boolean enableSpeedmode = settings.shouldEnterSpeedMode();
        Drawable speedModeDrawbale = enableSpeedmode ? context.getDrawable(R.drawable.ic_speedmode_off)
                :  context.getDrawable(R.drawable.ic_speed_alt_s);
        items.add(new GridBrowserMenuAdapter.MenuItem(speedModeDrawbale, R.id.speedMode, enableSpeedmode ? resources.getString(R.string.exit_speed_mode) :
                resources.getString(R.string.speed_mode) ));

        if (customTabConfig == null || customTabConfig.showShareMenuItem) {
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_share_s), R.id.share, resources.getString(R.string.menu_share)));
        }else{
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_share_s), R.id.share, resources.getString(R.string.menu_share), true));
        }

        //Request Desktop Site
        boolean isRequestingDesktopSite = settings.shouldRequestDesktopSite();
        Drawable requestDesktopDrawable = isRequestingDesktopSite ? context.getDrawable(R.drawable.ic_mobile_s)
                :  context.getDrawable(R.drawable.ic_desktop_s);
        items.add(new GridBrowserMenuAdapter.MenuItem(requestDesktopDrawable, R.id.requestDesktopSite, isRequestingDesktopSite ? resources.getString(R.string.request_mobile_site)
                : resources.getString(R.string.request_desktop_site)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_setting_modern),
                R.id.settings, resources.getString(R.string.menu_settings)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(R.drawable.ic_color_lens),
                R.id.themeMenu, "Theme Color"));

        return items;
    }
}
