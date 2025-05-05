package com.xlab.vbrowser.z.menu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.customtabs.CustomTabConfig;
import com.xlab.vbrowser.session.Session;
import com.xlab.vbrowser.session.SessionManager;
import com.xlab.vbrowser.utils.Settings;
import com.xlab.vbrowser.z.Icons;
import com.xlab.vbrowser.z.adapter.GridBrowserMenuAdapter;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    public static List<GridBrowserMenuAdapter.MenuItem> createMenu(Context context, String url, final @Nullable CustomTabConfig customTabConfig){
        final Resources resources = context.getResources();
        //final Browsers browsers = new Browsers(context, url);
        Settings settings = Settings.getInstance(context);

        List<GridBrowserMenuAdapter.MenuItem> items = new ArrayList<>();

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.add_to_homescreen)),
                R.id.add_to_homescreen, resources.getString(R.string.menu_add_to_home_screen)));

        Session currentSession = SessionManager.getInstance().getCurrentSession();
        boolean wasNotAddedToQuickAccess = currentSession != null && !currentSession.wasAddedToQuickAccess();

        if(wasNotAddedToQuickAccess) {
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.addToQuickAccess))
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access)));
        }else{
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.addToQuickAccess, 1))
                    , R.id.addToQuickAccess, resources.getString(R.string.add_to_quick_access), true));
        }

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.bookmarkActivity)),
                R.id.bookmarkActivity, resources.getString(R.string.bookmark)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.history)),
                R.id.history, resources.getString(R.string.history)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.download_manager))
                ,R.id.download_manager, resources.getString(R.string.download_manager)));

        int darkMode = settings.getDarkMode();
        Drawable darkModeDrawable = context.getDrawable(
                darkMode == 0 ? Icons.get(R.id.darkMode, 0)
                        : (darkMode!=1 ? Icons.get(R.id.darkMode, 1)
                        : Icons.get(R.id.darkMode, 2)));
        items.add(new GridBrowserMenuAdapter.MenuItem(darkModeDrawable, R.id.darkMode,
                darkMode == 0 ? "Auto Dark mode" : darkMode != 1 ? resources.getString(R.string.light_mode) : resources.getString(R.string.dark_mode) ));

        boolean enableSpeedmode = settings.shouldEnterSpeedMode();
        Drawable speedModeDrawbale = enableSpeedmode ? context.getDrawable(Icons.get(R.id.speedMode, 1))
                :  context.getDrawable(Icons.get(R.id.speedMode));
        items.add(new GridBrowserMenuAdapter.MenuItem(speedModeDrawbale, R.id.speedMode, enableSpeedmode ? resources.getString(R.string.exit_speed_mode) :
                resources.getString(R.string.speed_mode) ));

        if (customTabConfig == null || customTabConfig.showShareMenuItem) {
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.share)), R.id.share, resources.getString(R.string.menu_share)));
        }else{
            items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.share, 1)), R.id.share, resources.getString(R.string.menu_share), true));
        }

        //Request Desktop Site
        boolean isRequestingDesktopSite = settings.shouldRequestDesktopSite();
        Drawable requestDesktopDrawable = isRequestingDesktopSite ? context.getDrawable(Icons.get(R.id.requestDesktopSite, 1))
                :  context.getDrawable(Icons.get(R.id.requestDesktopSite));
        items.add(new GridBrowserMenuAdapter.MenuItem(requestDesktopDrawable, R.id.requestDesktopSite, isRequestingDesktopSite ? resources.getString(R.string.request_mobile_site)
                : resources.getString(R.string.request_desktop_site)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.settings)),
                R.id.settings, resources.getString(R.string.menu_settings)));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.themeMenu)),
                R.id.themeMenu, "Theme Color"));

        items.add(new GridBrowserMenuAdapter.MenuItem(context.getDrawable(Icons.get(R.id.changeIcons)),
                R.id.changeIcons, "Change Icons"));

        return items;
    }
}
