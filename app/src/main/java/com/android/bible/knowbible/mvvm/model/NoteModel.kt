package com.android.bible.knowbible.mvvm.model

data class NoteModel(var id: Int = 0,
                     var isNoteForVerse: Boolean,
                     var bookNumber: Int,
                     var chapterNumber: Int,
                     var verseNumber: Int,
                     var verseText: String,
                     var text: String)
