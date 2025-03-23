package com.xlab.vbrowser.bookmark.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by nguyenducthuan on 3/25/18.
 */

@Entity(
//    tableName = "bookmark",
//    foreignKeys = @ForeignKey(
//        entity = Bookmark.class,
//        parentColumns = "id",
//        childColumns = "parentId",
//        onUpdate = ForeignKey.CASCADE,
//        onDelete = ForeignKey.NO_ACTION
//    )
)
public class Bookmark {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String url;
    public String title;
    public long accessTime;
    @ColumnInfo(name = "parentId", index = true)
    public int parentId;
    public boolean isFolder;

    public long createAt;

    public Bookmark() {
        this.createAt = System.currentTimeMillis();
    }
}
