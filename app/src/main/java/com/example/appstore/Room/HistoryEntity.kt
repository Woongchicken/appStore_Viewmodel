package com.example.appstore.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    /* 검색어 */
    @PrimaryKey
    @ColumnInfo(name = "search_term")
    val searchTerm: String,

    /* 검색 시간 */
    @ColumnInfo(name = "date")
    val date: String?
)