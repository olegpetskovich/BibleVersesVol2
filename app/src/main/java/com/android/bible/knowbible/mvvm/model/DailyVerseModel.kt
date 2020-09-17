package com.android.bible.knowbible.mvvm.model

data class DailyVerseModel(
        val book_number: Int,
        val chapter_number: Int,
        val verse_number: Int,
        val verses_numbers: ArrayList<Int>,
        var verse_text: String = "")