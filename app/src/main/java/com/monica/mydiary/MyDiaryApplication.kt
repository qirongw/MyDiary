package com.monica.mydiary

import android.app.Application
import com.monica.mydiary.database.AppDatabase

class MyDiaryApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}