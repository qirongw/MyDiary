package com.monica.mydiary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.monica.mydiary.database.Diary
import com.monica.mydiary.database.DiaryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Date

class DiariesViewModel(private val context: Context, private val diaryDao: DiaryDao) : ViewModel() {

    private var _draft = ""
    val draft: String get() = _draft

    val diaries: LiveData<List<Diary>> = diaryDao.getDiaries().asLiveData()

    private var _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri

    fun setSelectedPhotoUri(uri: Uri) {
        _photoUri.value = uri
    }

    fun removeSelectedPhotoUri() {
        _photoUri = MutableLiveData()
    }

    fun saveDiary(title:String = "", content: String): LiveData<Unit> {
        val diary = Diary(0, title, Date(), content, null)
        return flow {
            emit(diaryDao.insertDiary(diary))
        }.asLiveData()
    }

    fun saveDiaryWithImage(title:String = "", content: String, bitmap: Bitmap): LiveData<Unit> {
        return flow {
            val filename = saveImageToFile(bitmap)
            val diary = Diary(0, title, Date(), content, filename)
            emit(diaryDao.insertDiary(diary))
        }.asLiveData()
    }

    private suspend fun saveImageToFile(bitmap: Bitmap): String = coroutineScope {
        withContext(Dispatchers.IO) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            val filename = FILE_PREFIX + System.currentTimeMillis().toString() + FILE_SUFFIX

            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(data)
            }
            filename
        }
    }

    fun updateDiary(diary: Diary): LiveData<Unit> {
        return flow {
            emit(diaryDao.updateDiary(diary))
        }.asLiveData()
    }

    fun saveDraft(text: String) {
        _draft = text
    }

    fun discardDraft() {
        _draft = ""
    }

    fun getDiary(id: Int): LiveData<Diary> {
        return diaryDao.getDiary(id).take(1).asLiveData()
    }

    fun deleteDiary(diary: Diary): LiveData<Unit> {
        return flow<Unit> {
            emit(diaryDao.deleteDiary(diary))
        }.asLiveData()
    }

    companion object {
        val FILE_PREFIX = "diary_image_"
        val FILE_SUFFIX = ".jpg"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras

                return DiariesViewModel(
                    application.applicationContext,
                    (application as MyDiaryApplication).database.diaryDao()
                ) as T
            }
        }
    }
}