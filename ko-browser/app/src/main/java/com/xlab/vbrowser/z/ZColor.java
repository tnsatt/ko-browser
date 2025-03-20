package com.xlab.vbrowser.z;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.adapter.ColorAdapter;
import com.xlab.vbrowser.z.items.ColorItem;
import com.xlab.vbrowser.z.module.ThemeColors;
import com.xlab.vbrowser.z.module.ZTheme;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZColor {
    public final static List<String[]> colors = new ArrayList<String[]>(){{
        add(new String[]{"#1c1c1d", "#0f0f0f"});
        add(new String[]{"#191f30", "#353b4d"});
        add(new String[]{"#3E3F5B", "#1c1c1d"});
        add(new String[]{"#261FB3", "#1c1c1d"});

        add(new String[]{"#1c1c1d", "#0f0f0f"});
        add(new String[]{"#1c1c1d", "#0f0f0f"});
    }};
    public static final String PRIMARY_COLOR_KEY = "primary_color";
    public static final String SECONDARY_COLOR_KEY = "secondary_color";

    public static void showColorPicker(Activity context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        LayoutInflater inf = context.getLayoutInflater();
        View view = inf.inflate(R.layout.color_picker_view, null);
        ColorPickerView colorPickerView = view.findViewById(R.id.color_picker_view);
        colorPickerView.setColor(ThemeColors.getInstance(context).getSavedColorInt());
        builder.setView(view);
        builder.setTitle("Pick Color");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                int colorInt = colorPickerView.getColor();
                colorInt = fixColor(context, colorInt);
                String color = Integer.toHexString(colorInt).substring(2);
                ThemeColors.getInstance(context).setNewThemeColor(color);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static int fixColor(Activity context, int color){
        List<Map<String, String>> data = parseStylesXml(context, R.xml.styles);
        List<Integer> lst = new ArrayList<>();
        for(Map<String, String> item: data) {
            String sc = item.get("colorPrimary");
            int c = android.graphics.Color.parseColor(sc);
            lst.add(c);
        }
        color = getNearestColor(color, lst);
        return color;
    }
    public static void showThemePicker(Activity context){
        List<ColorItem> lst = getColorsList(context);
        List<ColorItem> lst2 = getStyleColors(context, R.xml.colors);
        lst.addAll(lst2);
        ColorAdapter.OnSelected onSelected = new ColorAdapter.OnSelected() {
            @Override
            public void onSelectedColor(ColorItem colors) {

            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        LayoutInflater inf = context.getLayoutInflater();
        View view = inf.inflate(R.layout.color_picker, null);
        RecyclerView listView = view.findViewById(R.id.listView);
        listView.setLayoutManager(new GridLayoutManager(context, 4));
        ColorAdapter adapter = new ColorAdapter(context, lst, onSelected);
        listView.setAdapter(adapter);
        builder.setView(view);
        builder.setTitle("Select Theme");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ColorItem colorItem = adapter.getSelectedColor();
                if(colorItem !=null){
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(PRIMARY_COLOR_KEY, colorItem.primary);
                    editor.putString(SECONDARY_COLOR_KEY, colorItem.secondary);
                    editor.commit();
                    int color = android.graphics.Color.parseColor(colorItem.primary);
                    ZTheme.getInstance(context).setNewThemeColor(color);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static List<ColorItem> getColorsList(Context context){
        ColorItem color = getSavedColors(context);
        List<ColorItem> lst = new ArrayList<ColorItem>();
        boolean same = false;
        for(String[] item: colors){
            if(item.length < 2) continue;
            ColorItem c = new ColorItem(item[0], item[1]);
            c.selected = color != null && color.primary.equals(c.primary) && color.secondary.equals(c.secondary);
            lst.add(c);
            if(c.selected) same = true;
        }
        if(!same && color!=null) {
            color.selected = true;
            lst.add(0, color);
        }
        return lst;
    }
    public static ColorItem getSavedColors(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String primary = pref.getString(PRIMARY_COLOR_KEY, null);
        if(primary==null) return null;
        String secondary = pref.getString(SECONDARY_COLOR_KEY, null);
        if(secondary==null) return null;
        return new ColorItem(primary, secondary);
    }
    public static List<ColorItem> getStyleColors(Context context, int xml){
        List<ColorItem> lst = new ArrayList<>();
        List<Map<String, String>> data = parseStylesXml(context, xml);
        for(Map<String, String> item: data){
            String colorPrimary = item.get("colorPrimary");
            String colorPrimaryDark = item.get("colorPrimaryDark");
            String colorAccent = item.get("colorAccent");
            ColorItem c = new ColorItem(colorPrimary, colorAccent.equals(colorPrimary)?colorPrimaryDark:colorAccent);
            lst.add(c);
        }
        return lst;
    }
    public static List<Map<String, String>> parseStylesXml(Context context, int xml) {
        List<Map<String, String>> stylesList = new ArrayList<>();
        Resources res = context.getResources();
        XmlResourceParser parser = res.getXml(xml); // Ensure styles.xml exists in res/values/

        Map<String, String> currentStyle = null;
        String styleName = null;

        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("style".equals(parser.getName())) {
                            currentStyle = new HashMap<>();
                            styleName = parser.getAttributeValue(null, "name");
                        } else if ("item".equals(parser.getName()) && currentStyle != null) {
                            String itemName = parser.getAttributeValue(null, "name");
                            parser.next();
                            String itemValue = parser.getText();
                            currentStyle.put(itemName, itemValue);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("style".equals(parser.getName()) && currentStyle != null) {
                            currentStyle.put("style", styleName);
                            stylesList.add(currentStyle);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parser.close();
        }
        return stylesList;
    }
    public static void clearColor(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(PRIMARY_COLOR_KEY);
        editor.remove(SECONDARY_COLOR_KEY);
        editor.commit();
        ThemeColors.getInstance(context).clearColor();
        ZTheme.getInstance(context).clearColor();
    }
    public static void confirmClearColor(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setTitle("Reset color")
                .setMessage("Want to reset color?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        clearColor(context);
                        Toast.makeText(context, "Reset Color Success", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    public static int getNearestColor(int targetColor, List<Integer> colorList) {
        int nearestColor = colorList.get(0);
        double minDistance = Double.MAX_VALUE;

        int targetR = android.graphics.Color.red(targetColor);
        int targetG = android.graphics.Color.green(targetColor);
        int targetB = android.graphics.Color.blue(targetColor);

        for (int color : colorList) {
            int r = android.graphics.Color.red(color);
            int g = android.graphics.Color.green(color);
            int b = android.graphics.Color.blue(color);

            double distance = Math.sqrt(Math.pow(r - targetR, 2) +
                    Math.pow(g - targetG, 2) +
                    Math.pow(b - targetB, 2));

            if (distance < minDistance) {
                minDistance = distance;
                nearestColor = color;
            }
        }

        return nearestColor;
    }
}
