package com.tonyodev.fetch2.database

import android.arch.persistence.room.*
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_CREATED
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_FILE_NAME
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_FILE_TYPE
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_GROUP
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_ID
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_PRIORITY
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_REFERER_URL
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.COLUMN_STATUS
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.TABLE_NAME
import com.tonyodev.fetch2.util.FileTypeValue


@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(downloadInfo: DownloadInfo): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(downloadInfoList: List<DownloadInfo>): List<Long>

    @Delete
    fun delete(downloadInfo: DownloadInfo)

    @Delete
    fun delete(downloadInfoList: List<DownloadInfo>)

    @Query("DELETE FROM $TABLE_NAME")
    fun deleteAll()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(download: DownloadInfo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(downloadInfoList: List<DownloadInfo>)

    @Query("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_CREATED DESC")
    fun get(): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED < :columnCreated ORDER BY $COLUMN_CREATED DESC LIMIT :limit")
    fun getLimit(limit: Int, columnCreated: Long): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED < :columnCreated AND ($COLUMN_FILE_NAME LIKE :queryText OR $COLUMN_REFERER_URL LIKE :queryText) ORDER BY $COLUMN_CREATED DESC LIMIT :limit")
    fun getLimit(limit: Int, columnCreated: Long, queryText: String): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED < :columnCreated AND $COLUMN_FILE_TYPE = :selectedFileType ORDER BY $COLUMN_CREATED DESC LIMIT :limit")
    fun getLimit(limit: Int, columnCreated: Long, selectedFileType: FileTypeValue): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CREATED < :columnCreated AND ($COLUMN_FILE_NAME LIKE :queryText OR $COLUMN_REFERER_URL LIKE :queryText) AND $COLUMN_FILE_TYPE = :selectedFileType ORDER BY $COLUMN_CREATED DESC LIMIT :limit")
    fun getLimit(limit: Int, columnCreated: Long, queryText: String, selectedFileType: FileTypeValue): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun get(id: Int): DownloadInfo?

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID IN (:ids)")
    fun get(ids: List<Int>): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_STATUS = :status")
    fun getByStatus(status: Status): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_GROUP = :group")
    fun getByGroup(group: Int): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_GROUP = :group AND $COLUMN_STATUS = :status")
    fun getByGroupWithStatus(group: Int, status: Status): List<DownloadInfo>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_STATUS = :status ORDER BY $COLUMN_PRIORITY DESC, $COLUMN_CREATED ASC")
    fun getPendingDownloadsSorted(status: Status): List<DownloadInfo>

}