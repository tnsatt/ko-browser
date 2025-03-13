package com.xlab.vbrowser.z.fragment.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.customtabs.CustomTabConfig;
import com.xlab.vbrowser.fragment.BrowserFragment;
import com.xlab.vbrowser.locale.LocaleAwareAppCompatActivity;
import com.xlab.vbrowser.menu.browser.BlockingItemViewHolder;
import com.xlab.vbrowser.menu.browser.BrowserMenu;
import com.xlab.vbrowser.menu.browser.BrowserMenuAdapter;
import com.xlab.vbrowser.utils.ViewUtils;
import com.xlab.vbrowser.z.Z;
import com.xlab.vbrowser.z.activity.AdblockActivity;
import com.xlab.vbrowser.z.adapter.CardMenuAdapter;
import com.xlab.vbrowser.z.adapter.GridBrowserMenuAdapter;
import com.xlab.vbrowser.z.module.Adblock;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GridBrowserMenu extends BrowserMenu {
    private GridBrowserMenuAdapter adapter;
    private BrowserFragment fragment;
    private Activity context;
    private WeakReference<BlockingItemViewHolder> blockingItemViewHolderReference;

    public GridBrowserMenu(Context context, BrowserFragment fragment, @Nullable @org.jetbrains.annotations.Nullable CustomTabConfig customTabConfig) {
        final View view = LayoutInflater.from(context).inflate(R.layout.bottom_menu_tab, null);
        setContentView(view);
        this.fragment = fragment;
        this.context = fragment.getActivity();
        final BlockingItemViewHolder blockingItemViewHolder = new BlockingItemViewHolder(
                view.findViewById(R.id.ad_menu), fragment);
        blockingItemViewHolderReference = new WeakReference<>(blockingItemViewHolder);

        adapter = new GridBrowserMenuAdapter(context, this, fragment, customTabConfig);

        List<GridBrowserMenuAdapter> menuAdapters = new ArrayList<>();
        menuAdapters.add(adapter);
        menuAdapters.add(new GridBrowserMenuAdapter(context, this, fragment, customTabConfig));

        ViewPager viewPager = view.findViewById(R.id.viewPager);
        CardMenuAdapter cardAdapter = new CardMenuAdapter(context, menuAdapters);
        viewPager.setAdapter(cardAdapter);

        setup(view);

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setFocusable(true);

        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        setElevation(context.getResources().getDimension(com.xlab.vbrowser.R.dimen.menu_elevation));

        final Switch switchView = view.findViewById(com.xlab.vbrowser.R.id.blocking_switch);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                blockingItemViewHolder.onCheckedChanged(compoundButton, isChecked);
                if(isChecked){
                    Adblock.reload(fragment.getActivity());
                }
                updateAd(view);
            }
        });
    }
    public void updateAd(View view){
        ImageView adIcon = view.findViewById(R.id.adIcon);
        if(fragment.getSession().isBlockingEnabled()){
            adIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_shield_check_full_s));
            adIcon.setColorFilter(Color.GREEN);
        }else {
            adIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_shield_cross_full_s));
            adIcon.setColorFilter(Color.RED);
        }
    }
    public void setup(View view){
        updateAd(view);

        ImageView lockButton = view.findViewById(R.id.lockButton);
        lockButton.setVisibility(Z.isLock(context)?View.VISIBLE:View.GONE);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Z.isLock(context)) {
                    lockButton.setVisibility(View.GONE);
                    return;
                }
                Z.showLockScreen(context);
            }
        });
        ImageView adSetting = view.findViewById(R.id.adSetting);
        adSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AdblockActivity.class);
                context.startActivity(intent);
            }
        });
        ImageButton settingButton = view.findViewById(R.id.settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LocaleAwareAppCompatActivity) context).openPreferences();
            }
        });
        ImageButton closeMenu = view.findViewById(R.id.closeMenuButton);
        closeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GridBrowserMenu.this.dismiss();
            }
        });
        ImageButton exitButton = view.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext(), R.style.DialogStyle);
                builder.setTitle(fragment.getResources().getString(R.string.app_name))
                        .setMessage("Want to close app?")
                        .setIcon(context.getResources().getDrawable(R.drawable.ic_power_off_s))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                context.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });
    }
    public void updateTrackers(int trackers) {
        if (blockingItemViewHolderReference == null) {
            return;
        }

        final BlockingItemViewHolder navigationItemViewHolder = blockingItemViewHolderReference.get();
        if (navigationItemViewHolder != null) {
            navigationItemViewHolder.updateTrackers(trackers);
        }
    }

    public void updateLoading(boolean loading) {
        adapter.updateLoading(loading);
    }

    public void show(View anchor) {
        super.showAtLocation(anchor, Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
    }
}
