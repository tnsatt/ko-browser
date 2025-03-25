package com.xlab.vbrowser.z.fragment.adblock;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.xlab.vbrowser.z.utils.Toast;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.fragment.adblock.placeholder.AdblockPlaceholder;
import com.xlab.vbrowser.z.ad.AdblockRuleSet;
import com.xlab.vbrowser.z.ad.ZEasyListRuleSet;

import java.io.File;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class AdblockFragment extends Fragment {
    ListView listView;
    AdblockAdapter adapter;
    AdblockRuleSet adblock;
    ZEasyListRuleSet ruleset;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdblockFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdblockFragment newInstance(int columnCount) {
        AdblockFragment fragment = new AdblockFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adblock, container, false);
        adblock = AdblockRuleSet.getInstance();
        ruleset = (ZEasyListRuleSet) adblock.getRuleSet();
        // Set the adapter
        if (view instanceof ListView) {
            listView = (ListView) view;
            adapter = new AdblockAdapter(getActivity(), AdblockPlaceholder.ITEMS);
            listView.setAdapter(adapter);
            adapter.onEvent = new AdblockAdapter.OnEvent() {
                @Override
                public void onChecked(AdblockPlaceholder.PlaceholderItem item, Boolean checked) {
                    item.on = checked;
                    String url = item.url;
                    if(checked){
                        ruleset.addUrl(url);
                        (new Thread(new Runnable() {
                            @Override
                            public void run() {
                                loadUrl(item);
                            }
                        })).start();
                    }else{
                        ruleset.removeUrl(url);
                    }
                }

                @Override
                public void onRefresh(AdblockPlaceholder.PlaceholderItem item) {
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(!item.on) {
                                runUi(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Filter is off", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            loadUrl(item);
                        }
                    })).start();
                }

                @Override
                public void onDelete(AdblockPlaceholder.PlaceholderItem item) {
                    ruleset.cleanUrl(item.url);
                    ruleset.deleteUrl(item.url);
                    item.count = ruleset.getRuleCount(item.url);
                    adapter.notifyDataSetChanged();
                }
            };
        }
        load();
        return view;
    }
    public void loadUrl(AdblockPlaceholder.PlaceholderItem item){
        try {
            item.isLoading = true;
            runUi(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
            ruleset.loadUrl(item.url);
        } catch (Exception e) {

        }
        item.count = ruleset.getRuleCount(item.url);
        item.isLoading = false;
        runUi(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    public void load(){
        AdblockPlaceholder.ITEMS.clear();
        List<String> onUrls = ruleset.getInternetUrls();
        for(String url: ruleset.getAllInternetUrls()){
            Uri uri = Uri.parse(url);
            String name = (new File(uri.getPath())).getName();
            Boolean on = onUrls.contains(url);
            AdblockPlaceholder.PlaceholderItem item = new AdblockPlaceholder.PlaceholderItem(url, name, url, ruleset.getRuleCount(url), on);
            AdblockPlaceholder.ITEMS.add(item);
        }
        adapter.notifyDataSetChanged();
    }
    public void runUi(Runnable r){
        getActivity().runOnUiThread(r);
    }
}