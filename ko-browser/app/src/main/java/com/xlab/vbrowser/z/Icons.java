package com.xlab.vbrowser.z;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xlab.vbrowser.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Icons {
    public static final Map<Integer, int[]> lineIcons = new HashMap<Integer, int[]>(){{
        put(R.id.add_to_homescreen, new int[]{R.drawable.ic_shortcut_s});
        put(R.id.addToQuickAccess, new int[]{R.drawable.ic_home_smile, R.drawable.ic_home_s_disabled});
        put(R.id.bookmarkActivity, new int[]{R.drawable.ic_star_bookmark_s});
        put(R.id.history, new int[]{R.drawable.ic_history_s});
        put(R.id.download_manager, new int[]{R.drawable.ic_download_s});
        put(R.id.darkMode, new int[]{R.drawable.ic_sun_alt_s, R.drawable.ic_sun_s, R.drawable.ic_moon_s});
        put(R.id.speedMode, new int[]{R.drawable.ic_speed_alt_s, R.drawable.ic_speedmode_off});
        put(R.id.share, new int[]{R.drawable.ic_share_s});
        put(R.id.requestDesktopSite, new int[]{R.drawable.ic_desktop_s, R.drawable.ic_mobile_s});
        put(R.id.settings, new int[]{R.drawable.ic_setting_modern});
        put(R.id.themeMenu, new int[]{R.drawable.ic_color_lens});
        put(R.id.changeIcons, new int[]{R.drawable.ic_pinwheel_line_s});
        put(R.id.settingButton, new int[]{R.drawable.ic_setting_alt_line});
        put(R.id.signOutButtonView, new int[]{R.drawable.ic_exit_s});
        put(R.id.newTabButtonView, new int[]{R.drawable.ic_new_report_s});
        put(R.id.noBookmarkIcon, new int[]{R.drawable.ic_star_bookmark_s});
    }};
    public static final Map<Integer, int[]> fillIcons = new HashMap<Integer, int[]>(){{
        put(R.id.add_to_homescreen, new int[]{R.drawable.ic_app_badge_fill_s});
        put(R.id.addToQuickAccess, new int[]{R.drawable.ic_home_smile_angle_fill_s, R.drawable.ic_home_smile_angle_fill_s_disabled});
        put(R.id.bookmarkActivity, new int[]{R.drawable.ic_star_fill_s});
        put(R.id.history, new int[]{R.drawable.ic_history_s});
        put(R.id.download_manager, new int[]{R.drawable.ic_download_square_fill_s});
        put(R.id.darkMode, new int[]{R.drawable.ic_sun_alt_s, R.drawable.ic_sun_fill_s, R.drawable.ic_moon_stars_fill_s});
        put(R.id.speedMode, new int[]{R.drawable.ic_speed_alt_s, R.drawable.ic_speedmode_off});
        put(R.id.share, new int[]{R.drawable.ic_share_3_s});
        put(R.id.requestDesktopSite, new int[]{R.drawable.ic_desktop_fill_s, R.drawable.ic_mobile_fill_s});
        put(R.id.settings, new int[]{R.drawable.ic_setting_square_fill_s});
        put(R.id.themeMenu, new int[]{R.drawable.ic_color_mode_s});
        put(R.id.changeIcons, new int[]{R.drawable.ic_pinwheel_fill_s});
        put(R.id.settingButton, new int[]{R.drawable.ic_setting_fill_s});
        put(R.id.signOutButtonView, new int[]{R.drawable.ic_exit_fill_s});
    }};
    public static final Map<Integer, int[]> oldIcons = new HashMap<Integer, int[]>(){{
        put(R.id.download_manager, new int[]{R.drawable.ic_download_popup_menu});
        put(R.id.history, new int[]{R.drawable.ic_history});
        put(R.id.bookmarkActivity, new int[]{R.drawable.ic_bookmark_menu_popup});
        put(R.id.addToQuickAccess, new int[]{R.drawable.ic_add_to_quick_access, R.drawable.ic_add_to_quick_access});
        put(R.id.add_to_homescreen, new int[]{R.drawable.ic_home_menu_popup});
        put(R.id.speedMode, new int[]{R.drawable.ic_speedmode_on, R.drawable.ic_speedmode_off});
        put(R.id.nightMode, new int[]{R.drawable.ic_nightmode_on, R.drawable.ic_nightmode_off});
        put(R.id.share, new int[]{R.drawable.ic_share});
        put(R.id.requestDesktopSite, new int[]{R.drawable.ic_desktop, R.drawable.ic_mobile});
        put(R.id.settings, new int[]{R.drawable.ic_settings});
        put(R.id.rateApp, new int[]{R.drawable.ic_rate});
        put(R.id.noBookmarkIcon, new int[]{R.drawable.ic_star});
    }};
    private static final List<Map<Integer, int[]>> lst = new ArrayList<Map<Integer, int[]>>(){{
        add(lineIcons);
        add(fillIcons);
        add(oldIcons);
    }};
    private static int index = 0;
    private static final String KEY = "ICON_INDEX";
    private static final int DEFAULT_ICON = R.drawable.ic_extension;

    public static Integer get(int id, int index){
        Map<Integer, int[]> datas = lst.get(Icons.index);
        if(datas.get(id) != null){
            int[] icons = datas.get(id);
            if(index < icons.length){
                return icons[index];
            }
        }
        for(int i = 0; i < Icons.index; i++){
            Map<Integer, int[]> data = lst.get(i);
            if(data.get(id) == null) continue;
            int[] icons = data.get(id);
            if(index >= icons.length){
                if(i>0) continue;
                if(icons.length == 0) break;
                return icons[0];
            }
            return icons[index];
        }
        return DEFAULT_ICON;
    }
    public static Integer get(int id){
        return get(id, 0);
    }
    public static void change(Context context){
        int index = Icons.index;
        index++;
        if(index >= lst.size()) index = 0;
        set(context, index);
    }
    public static void setup(Context context){
        index = getIndex(context);
        if(index >= lst.size()) index = 0;
    }
    public static void set(Context context, int index){
        Icons.index = index;
        setIndex(context, index);
    }
    public static int getIndex(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(KEY, 0);
    }
    private static void setIndex(Context context, int index){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(KEY, index).commit();
    }
}
