package com.monica.mydiary

import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.monica.mydiary.database.Diary
import com.monica.mydiary.database.DiaryDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import java.util.Date

class DiariesViewModel(private val diaryDao: DiaryDao) : ViewModel() {

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

    fun saveDiary(title:String = "", content: String): Boolean {
        //_draft = text
        val diary = Diary(0, title, Date(), content)
        viewModelScope.launch {
            diaryDao.insertDiary(diary)
        }
        return true
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

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras

                return DiariesViewModel(
                    (application as MyDiaryApplication).database.diaryDao()
                ) as T
            }
        }
    }
}