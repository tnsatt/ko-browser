package com.tonyodev.fetch2.database


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tonyodev.fetch2.database.DownloadDatabase.Companion.DATABASE_VERSION
import com.tonyodev.fetch2.database.migration.Migration
import com.tonyodev.fetch2.database.migration.MigrationOneToTwo

@Database(entities = [DownloadInfo::class], version = DATABASE_VERSION, exportSchema = false)
@TypeConverters(value = [Converter::class])
abstract class DownloadDatabase : RoomDatabase() {

    abstract fun requestDao(): DownloadDao

    fun wasRowInserted(row: Long): Boolean {
        return row != (-1).toLong()
    }

    companion object {
        const val TABLE_NAME = "requests"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAMESPACE = "_namespace"
        const val COLUMN_URL = "_url"
        const val COLUMN_FILE = "_file"
        const val COLUMN_GROUP = "_group"
        const val COLUMN_PRIORITY = "_priority"
        const val COLUMN_HEADERS = "_headers"
        const val COLUMN_DOWNLOADED = "_written_bytes"
        const val COLUMN_TOTAL = "_total_bytes"
        const val COLUMN_STATUS = "_status"
        const val COLUMN_ERROR = "_error"
        const val COLUMN_NETWORK_TYPE = "_network_type"
        const val COLUMN_CREATED = "_created"
        const val COLUMN_TAG = "_tag"
        const val COLUMN_FILE_TYPE = "_file_type"
        const val COLUMN_FILE_NAME = "_file_name"
        const val COLUMN_ORIGINAL_FILE_NAME = "_original_file_name"
        const val COLUMN_REFERER_URL = "_referer_url"
        const val OLD_DATABASE_VERSION = 1
        const val DATABASE_VERSION = 2

        @JvmStatic
        fun getMigrations(): Array<Migration> {
            return arrayOf(MigrationOneToTwo())
        }

    }

}