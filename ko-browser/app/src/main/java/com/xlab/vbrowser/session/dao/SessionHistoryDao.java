package com.xlab.vbrowser.session.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.xlab.vbrowser.session.entity.SessionHistory;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Dao
public interface SessionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(SessionHistory... sessionHistories);

    @Query("DELETE FROM sessionhistory")
    public void clear();

    @Query("SELECT * FROM sessionhistory order by accessTime asc")
    public SessionHistory[] load();
}
