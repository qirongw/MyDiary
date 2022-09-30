package com.monica.mydiary

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
import kotlinx.coroutines.runBlocking

class DiariesViewModel(private val diaryDao: DiaryDao) : ViewModel() {

    private var _draft = ""
    val draft: String get() = _draft

    val diaries: LiveData<List<Diary>> = diaryDao.getDiaries().asLiveData()

    fun saveDiary(text: String): Boolean {
        //_draft = text
        val diary = Diary(0, text)
        viewModelScope.launch {
            diaryDao.insertDiary(diary)
        }
        return true
    }

    fun saveDraft(text: String) {
        _draft = text
    }

    fun discardDraft() {
        _draft = ""
    }

    fun getDiary(id: Int): LiveData<Diary> {
        return diaryDao.getDiary(id).asLiveData()
    }

    suspend fun testDelete(diary: Diary) {
        runBlocking { diaryDao.deleteDiary(diary) }
        delay(3000)
    }

    fun deleteDiary(diary: Diary): LiveData<Unit> {
        return flow<Unit> {
            emit(testDelete(diary))
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