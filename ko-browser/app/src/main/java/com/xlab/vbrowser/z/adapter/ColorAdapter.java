package com.xlab.vbrowser.z.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xlab.vbrowser.R;
import com.xlab.vbrowser.z.items.ColorItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    List<ColorItem> lst;
    Context context;
    OnSelected onSelected;
    ColorItem selectedColor;

    public interface OnSelected{
        void onSelectedColor(ColorItem colorItem);
    }

    public ColorAdapter(Context context, List<ColorItem> lst, OnSelected onSelected){
        this.lst = lst;
        this.context = context;
        this.onSelected = onSelected;
    }

    @Override
    public ColorAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        return new ColorAdapter.ViewHolder(inflater.inflate(R.layout.item_colors, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, @SuppressLint("RecyclerView") int i) {
        ColorItem colorItem = lst.get(i);

        holder.primary.setColorFilter(Color.parseColor(colorItem.primary));
        holder.secondary.setColorFilter(Color.parseColor(colorItem.secondary));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ColorItem color: lst){
                    color.selected = false;
                }
                colorItem.selected = true;
                selectedColor = colorItem;
                notifyDataSetChanged();
                if(onSelected!=null) onSelected.onSelectedColor(colorItem);
            }
        });
        if(colorItem.selected){
            holder.view.setBackground(context.getResources().getDrawable(R.drawable.outline));
        }else{
            holder.view.setBackground(null);
        }
    }
    public ColorItem getSelectedColor(){
        return selectedColor;
    }

    public static Drawable createIconWithBackground(Context context, int iconRes, String colorString) {
        // Create a background color drawable
        Drawable background = new ColorDrawable(Color.parseColor(colorString));

        // Get the icon from resources
        Drawable icon = ContextCompat.getDrawable(context, iconRes);

        // Ensure the icon is not null
        if (icon == null) return background;

        // Layer both drawables: [background, icon]
        Drawable[] layers = new Drawable[]{background, icon};
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        return layerDrawable;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return lst.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView primary;
        public ImageView secondary;
        public ViewHolder(View view){
            super(view);
            this.view = view;
            this.primary = view.findViewById(R.id.primary);
            this.secondary = view.findViewById(R.id.secondary);
        }
    }
}
