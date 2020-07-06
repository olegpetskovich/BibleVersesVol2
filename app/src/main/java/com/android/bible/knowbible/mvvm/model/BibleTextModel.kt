package com.android.bible.knowbible.mvvm.model

data class BibleTextModel(var id: Long = -1,
                          val book_number: Int,
                          val chapter: Int,
                          val verse: Int,
                          var text: String,
                          var textColorHex: String?,
                          var isTextBold: Boolean,
                          var isTextUnderline: Boolean,
                          var isTextToDailyVerse: Boolean)