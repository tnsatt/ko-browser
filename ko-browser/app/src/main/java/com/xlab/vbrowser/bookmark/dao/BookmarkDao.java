package com.xlab.vbrowser.bookmark.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.xlab.vbrowser.bookmark.entity.Bookmark;

/**
 * Created by nguyenducthuan on 3/25/18.
 */

@Dao
public interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertBookmark(Bookmark bookmark);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateBookmark(Bookmark bookmark);

//    @Delete
//    public void deleteBookmarks(Bookmark... bookmarks);

    @Query("DELETE FROM bookmark where id=(SELECT id FROM bookmark WHERE url = :url ORDER BY id DESC LIMIT 1) and not exists(SELECT 1 FROM BOOKMARK b WHERE b.parentId=bookmark.id)")
    public int deleteByUrl(String url);

    @Query("DELETE FROM bookmark where id = :id and not exists(SELECT 1 FROM BOOKMARK b WHERE b.parentId=bookmark.id)")
    public int deleteById(int id);

    @Query("DELETE FROM bookmark")
    public void clear();

    @Query("SELECT * FROM bookmark where parentId=:parentId order by id desc")
    public Bookmark[] loadBookmarksByParentId(int parentId);

    @Query("SELECT * FROM bookmark where url=:url order by id desc")
    public Bookmark[] getBookmarkByUrl(String url);

    @Query("SELECT * FROM bookmark order by id desc")
    public Bookmark[] loadBookmarks();

    @Query("SELECT * FROM bookmark order by id desc limit :limitRecords")
    public Bookmark[] loadBookmarks(int limitRecords);

    @Query("SELECT * FROM bookmark where accessTime < :lastAccessTime order by isFolder desc, id desc limit :limitRecords")
    public Bookmark[] loadBookmarks(long lastAccessTime, int limitRecords);

    @Query("SELECT * FROM bookmark where accessTime < :lastAccessTime and (url like :queryText or title like :queryText) order by isFolder desc, id desc limit :limitRecords")
    public Bookmark[] loadBookmarks(long lastAccessTime, String queryText, int limitRecords);

    @Query("SELECT count(url) FROM bookmark where url = :url")
    public int countBookmarkByUrl(String url);

    @Query("SELECT * FROM bookmark where url is null order by updateAt desc")
    public Bookmark[] getAllFolders();

    @Query("SELECT * FROM bookmark where parentId=:parentId order by isFolder desc, id desc")
    public Bookmark[] getChildrens(int parentId);

    @Query("SELECT count(*) FROM bookmark where parentId=:parentId")
    int countChildren(int parentId);
}
