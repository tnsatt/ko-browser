package com.xlab.vbrowser.history.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xlab.vbrowser.history.entity.MostVisited;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Dao
public interface MostVisistedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertMostVisited(MostVisited... mostVisited);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertMostVisited(MostVisited mostVisited);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public void updateMostVisited(MostVisited... mostVisiteds);

    @Delete
    public void deleteMostVisisted(MostVisited... mostVisiteds);

    @Query("DELETE FROM mostvisited")
    public void clear();

    @Query("SELECT * FROM mostvisited where isRemoved = 0 order by count desc limit :limitRecords")
    public MostVisited[] loadMostVisteds(int limitRecords);

    @Query("SELECT * FROM mostvisited WHERE isRemoved = 0 and url = :url")
    public MostVisited getMostVisited(String url);

    @Query("SELECT url FROM mostvisited WHERE url like :url order by count desc")
    public String[] getSuggestion(String url);
}