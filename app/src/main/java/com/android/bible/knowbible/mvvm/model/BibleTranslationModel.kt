package com.android.bible.knowbible.mvvm.model

data class BibleTranslationModel(val languageName: String,
                                 val translationName: String,
                                 val translationDBFileName: String, //Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                                 val abbreviationTranslationName: String)