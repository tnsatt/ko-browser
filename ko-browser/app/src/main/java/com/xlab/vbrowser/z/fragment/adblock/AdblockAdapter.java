package com.xlab.vbrowser.z.fragment.adblock;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.fragment.adblock.placeholder.AdblockPlaceholder.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AdblockAdapter extends BaseAdapter {

    private final List<PlaceholderItem> mValues;
    private int layout = R.layout.item_adblock_url;
    private LayoutInflater li;
    public OnEvent onEvent;
    public interface OnEvent{
        void onChecked(PlaceholderItem item, Boolean checked);
        void onRefresh(PlaceholderItem item);
        void onDelete(PlaceholderItem item);
    }
    public AdblockAdapter(Activity context, List<PlaceholderItem> items) {
        mValues = items;
        this.li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.checkbox.setChecked(holder.mItem.on);
        holder.mTextView.setText(holder.mItem.url);
        holder.mHeaderView.setText(holder.mItem.name + (holder.mItem.isLoading?" Loading...":" ("+holder.mItem.count+")"));
        holder.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onEvent!=null) onEvent.onRefresh(holder.mItem);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onEvent!=null) onEvent.onDelete(holder.mItem);
            }
        });
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(onEvent!=null) onEvent.onChecked(holder.mItem, b);
            }
        });
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public Object getItem(int i) {
        return mValues.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder vh;
        if(view==null){
            view = li.inflate(layout, parent, false);
            vh = new ViewHolder(view);
            view.setTag(vh);
        }else{
            vh = (ViewHolder) view.getTag();
            vh.checkbox.setOnCheckedChangeListener(null);
        }
        onBindViewHolder(vh, position);
        return view;
    }

    public class ViewHolder{
        public final TextView mTextView;
        public final TextView mHeaderView;
        public final FrameLayout refreshButton;
        public final FrameLayout delete;
        public final CheckBox checkbox;
        public PlaceholderItem mItem;

        public ViewHolder(View view) {
            mTextView = view.findViewById(R.id.text);
            mHeaderView = view.findViewById(R.id.header);
            refreshButton = view.findViewById(R.id.refresh);
            delete = view.findViewById(R.id.delete);
            checkbox = view.findViewById(R.id.checkbox);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mHeaderView.getText() + "'";
        }
    }
}