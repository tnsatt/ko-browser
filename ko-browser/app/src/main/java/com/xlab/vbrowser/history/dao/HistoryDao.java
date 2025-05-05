package com.xlab.vbrowser.history.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xlab.vbrowser.history.entity.History;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertHistory(History history);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public void updateHistory(History history);

    @Delete
    public void deleteHistories(History... histories);

    @Query("DELETE FROM history")
    public void clear();

    @Query("SELECT * FROM history order by accessTime desc")
    public History[] loadHistories();

    @Query("SELECT * FROM history order by accessTime desc limit :limitRecords")
    public History[] loadHistories(int limitRecords);

    @Query("SELECT * FROM history where accessTime < :lastAccessTime order by accessTime desc limit :limitRecords")
    public History[] loadHistories(long lastAccessTime, int limitRecords);

    @Query("SELECT * FROM history where accessTime < :lastAccessTime and (url like :queryText or title like :queryText) order by accessTime desc limit :limitRecords")
    public History[] loadHistories(long lastAccessTime, String queryText, int limitRecords);

    @Query("SELECT id FROM history where url = :url and :currentTime < accessTime + :rangeTime limit 1")
    public int loadMostVistedInDayByUrl(String url, long currentTime, long rangeTime);
}
