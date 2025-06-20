package com.xlab.vbrowser.quickdial.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class QuickDialItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String url;
    public String title;
    public float sortOrder;

    public static QuickDialItem create(String url, String title) {
        QuickDialItem quickDialItem = new QuickDialItem();
        quickDialItem.sortOrder = new Date().getTime();
        quickDialItem.url = url;
        quickDialItem.title = title;
        return quickDialItem;
    }
}
