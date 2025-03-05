package com.xlab.vbrowser.z.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.menu.browser.BrowserMenuAdapter;

import java.util.List;

public class CardMenuAdapter extends PagerAdapter {
    private Context context;
    private List<GridBrowserMenuAdapter> data;

    public CardMenuAdapter(Context context, List<GridBrowserMenuAdapter> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.full_menu, container, false);
        GridBrowserMenuAdapter adapter = data.get(position);

        RecyclerView menuList = view.findViewById(com.xlab.vbrowser.R.id.list);
        menuList.setLayoutManager(new GridLayoutManager(context, 5));
        menuList.setAdapter(adapter);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
