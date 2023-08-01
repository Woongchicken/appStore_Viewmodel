package com.example.appstore.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MainDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getHistoryAll(): List<HistoryEntity>

    @Query("SELECT * FROM history ORDER BY date DESC LIMIT 1")
    fun getHistoryRecent(): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setInsertHistory(entity: HistoryEntity)
}