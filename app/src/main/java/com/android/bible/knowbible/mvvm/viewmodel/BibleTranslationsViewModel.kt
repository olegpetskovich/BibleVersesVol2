package com.android.bible.knowbible.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTranslationModel

//Наследуемся от AndroidViewModel, чтобы можно было пользоваться контекстом для string ресурсов
class BibleTranslationsViewModel(application: Application) : AndroidViewModel(application) {
    private val translationsListLiveData = MutableLiveData<ArrayList<BibleTranslationModel>>()

    fun getTranslationsList(): LiveData<ArrayList<BibleTranslationModel>> {
        translationsListLiveData.value = getTranslationsListData()
        return translationsListLiveData
        getApplication<Application>().applicationContext
    }

    private fun getTranslationsListData(): ArrayList<BibleTranslationModel> {
        val translationsList = ArrayList<BibleTranslationModel>()

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.russian_language_translation),
                getApplication<Application>().getString(R.string.synodal_translation),
                "synodal_translation.SQLite3", //Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "SYNO"))

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.russian_language_translation),
                getApplication<Application>().getString(R.string.nrt_translation),
                "nrt_translation.SQLite3", //Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "NRT"))

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.ukrainian_language_translation),
                getApplication<Application>().getString(R.string.ogienko_translation),
                "translation_ivan_ogienko.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "UBIO"))

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.english_language_translation),
                getApplication<Application>().getString(R.string.kjv_translation),
                "king_james_translation.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "KJV"))

        return translationsList
    }
}