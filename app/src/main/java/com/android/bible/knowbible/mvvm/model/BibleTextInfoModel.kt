package com.android.bible.knowbible.mvvm.model

data class BibleTextInfoModel(var id: Long = -1,
                              val bookNumber: Int,
                              val chapterNumber: Int,
                              val verseNumber: Int,
                              var textColorHex: String?,
                              var isTextBold: Boolean,
                              var isTextUnderline: Boolean,
                              var isTextToDailyVerse: Boolean)