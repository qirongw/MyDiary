package com.monica.mydiary.database

import android.net.Uri
import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun uriToString(value: Uri?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun stringToUri(string: String?): Uri? {
        return string?.let { Uri.parse(it) }
    }
}