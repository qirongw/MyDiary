package com.monica.mydiary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary")
    fun getDiaries(): Flow<List<Diary>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiary(diary: Diary)

    @Query("SELECT * FROM diary WHERE id = :diaryId")
    fun getDiary(diaryId: Int): Flow<Diary>

    @Delete
    suspend fun deleteDiary(diary: Diary)
}