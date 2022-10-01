package com.monica.mydiary.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @NonNull @ColumnInfo(name = "title")
    val title: String,
    @NonNull @ColumnInfo(name="date")
    val date:Date
)
