package com.xlab.vbrowser.history.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Entity
public class SearchTerm {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String term;
    public long accessTime;
}
