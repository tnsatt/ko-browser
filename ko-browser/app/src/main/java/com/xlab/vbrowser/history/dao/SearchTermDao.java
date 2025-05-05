package com.xlab.vbrowser.history.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xlab.vbrowser.history.entity.SearchTerm;

/**
 * Created by nguyenducthuan on 2/27/18.
 */

@Dao
public interface SearchTermDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insert(SearchTerm... searchTerms);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    public void update(SearchTerm searchTerm);

    @Delete
    public void delete(SearchTerm... searchTerms);

    @Query("DELETE FROM searchterm")
    public void clear();

    @Query("SELECT term FROM searchterm WHERE term like :term order by accessTime desc")
    public String[] getTerm(String term);
}
