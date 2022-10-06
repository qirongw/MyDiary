package com.monica.mydiary.database

import android.net.Uri
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String="",
    @NonNull @ColumnInfo(name="date")
    val date:Date,
    @NonNull @ColumnInfo(name="content", defaultValue = "")
    val content:String,
    @ColumnInfo(name="photoFilename")
    val photoFilename:String?
)
