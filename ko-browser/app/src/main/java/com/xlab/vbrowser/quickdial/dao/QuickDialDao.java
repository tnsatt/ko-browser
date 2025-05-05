package com.xlab.vbrowser.quickdial.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xlab.vbrowser.quickdial.entity.QuickDialItem;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Dao
public interface QuickDialDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insert(QuickDialItem quickDialItems);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insert(QuickDialItem... quickDialItems);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public void update(QuickDialItem... quickDialItems);

    @Query("SELECT * FROM quickdialitem order by sortOrder asc")
    public QuickDialItem[] load();

    @Query("SELECT * FROM quickdialitem order by sortOrder asc limit :limitRecords")
    public QuickDialItem[] load(int limitRecords);

    @Delete
    public void delete(QuickDialItem... quickDialItems);

    @Query("DELETE FROM quickdialitem")
    public void clear();

    @Query("SELECT id FROM quickdialitem where url = :url")
    public int load(String url);

    @Query("SELECT url FROM quickdialitem WHERE url like :url order by sortOrder asc")
    public String[] getSuggestion(String url);
}
