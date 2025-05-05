package com.xlab.vbrowser.session.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Entity
public class SessionHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String source;
    public String searchTerms;
    public String url;
    public String title;
    public boolean isSelectedSession;
    public boolean isBlockingEnabled;
    public long accessTime;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] webviewState;
}
