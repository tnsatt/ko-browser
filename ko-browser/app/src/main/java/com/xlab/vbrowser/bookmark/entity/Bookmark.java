package com.xlab.vbrowser.bookmark.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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
    public int parentId = 0;
    public boolean isFolder;

    public long updateAt;
    public long createAt;

    public Bookmark() {
        this.createAt = System.currentTimeMillis();
        this.updateAt = this.createAt;
    }

    public String toString(){
        if(id == 0) return "Root";
        return title==null||title.trim().isEmpty()?"<Empty Title>":title;
    }
}
