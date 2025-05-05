package com.xlab.vbrowser.history.entity;




import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String url;
    public String title;
    public long accessTime;
}
