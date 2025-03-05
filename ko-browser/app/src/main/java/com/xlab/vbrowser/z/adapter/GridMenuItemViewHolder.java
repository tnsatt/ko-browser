package com.xlab.vbrowser.z.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.menu.browser.BrowserMenuAdapter;
import com.xlab.vbrowser.utils.UrlUtils;
import com.xlab.vbrowser.menu.browser.BrowserMenuViewHolder;

public class GridMenuItemViewHolder extends BrowserMenuViewHolder {
    static final int LAYOUT_ID = com.xlab.vbrowser.R.layout.grid_menu_item;

    private View entireView;
    private TextView titleView;
    private ImageView imageView;
    private ImageButton actionButtonView;

    GridMenuItemViewHolder(View itemView) {
        super(itemView);

        entireView = itemView;
        titleView = (TextView) itemView.findViewById(R.id.titleView);
        imageView = (ImageView) itemView.findViewById(R.id.icon);
        actionButtonView = (ImageButton) itemView.findViewById(R.id.actionButtonView);
    }

    /* package-private */ void bind(final GridBrowserMenuAdapter.MenuItem menuItem) {
        Context context = entireView.getContext();
        entireView.setId(menuItem.id);
        titleView.setText(menuItem.label);
        boolean wasSetIcon = false;

        final String title = browserFragment.getSession() != null ? browserFragment.getSession().getTitle().getValue() : "";
        String url = browserFragment.getInitialUrl();
        final boolean isUrl = url != null
                && UrlUtils.isUrl(url) && !UrlUtils.isBlankUrl(url) && !UrlUtils.isInternalErrorURL(url);

        if (entireView.getId() == com.xlab.vbrowser.R.id.add_to_homescreen && (title == null || TextUtils.isEmpty(title.trim()) || !isUrl)) {
            int color = getAttributeColor(entireView.getContext(), R.attr.inactiveTextColor);
            color = color == -1 ? browserFragment.getResources().getColor(com.xlab.vbrowser.R.color.colorTextInactive) : color;

            titleView.setTextColor(color);
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_shortcut_s_disabled));
            entireView.setEnabled(false);
            wasSetIcon = true;
        } else {
            entireView.setOnClickListener(this);
        }

        if (entireView.getId() == com.xlab.vbrowser.R.id.share && !isUrl) {
            int color = getAttributeColor(entireView.getContext(), R.attr.inactiveTextColor);
            color = color == -1 ? browserFragment.getResources().getColor(com.xlab.vbrowser.R.color.colorTextInactive) : color;
            titleView.setTextColor(color);
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_share_s_disabled));
            entireView.setEnabled(false);
            wasSetIcon = true;
        }
        else {
            entireView.setOnClickListener(this);
        }

        if(menuItem.disabled){
            int color = getAttributeColor(entireView.getContext(), R.attr.inactiveTextColor);
            color = color == -1 ? browserFragment.getResources().getColor(com.xlab.vbrowser.R.color.colorTextInactive) : color;
            titleView.setTextColor(color);
//            imageView.setColorFilter(color); //not work
            imageView.setImageDrawable(menuItem.icon);
            entireView.setEnabled(false);
            wasSetIcon = true;
        }else {
            entireView.setOnClickListener(this);
        }

        if (!wasSetIcon) {
            imageView.setImageDrawable(menuItem.icon);
        }

        actionButtonView.setVisibility(menuItem.menuAction != null ? View.VISIBLE : View.GONE);
        actionButtonView.setVisibility(View.GONE);

        if (menuItem.menuAction != null) {
            actionButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View var1) {
                    closeMenu();
                    menuItem.menuAction.onPerformAction();
                }
            });
        }
    }

    private int getAttributeColor(
            Context context,
            int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
        }

        return color;
    }
}
