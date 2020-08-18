package com.android.bible.knowbible.mvvm.model

data class BibleTextModel(var id: Long = -1,
                          val book_number: Int,
                          val chapter_number: Int,
                          val verse_number: Int,
                          var text: String,
                          var textColorHex: String?,
                          var isTextBold: Boolean,
                          var isTextUnderline: Boolean,
                          var isTextSelected: Boolean = false /*Поле сугубо для BibleTextRVAdapter, чтобы можно было отметить выбранный текст в режиме MultiSelection*/)