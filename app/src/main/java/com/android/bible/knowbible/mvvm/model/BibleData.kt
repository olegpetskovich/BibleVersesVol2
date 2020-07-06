package com.android.bible.knowbible.mvvm.model

data class BibleData(val info: ArrayList<BibleInfo>,
                     val books_list: ArrayList<BookModel>,
                     val verses_list: ArrayList<BibleTextModel>)