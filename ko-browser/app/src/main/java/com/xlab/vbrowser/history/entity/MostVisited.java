package com.xlab.vbrowser.history.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Entity
public class MostVisited {
    @PrimaryKey
    @NonNull
    public String url = "";
    public String title;
    public long count;
    public int isRemoved = 0;
}
