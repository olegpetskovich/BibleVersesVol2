package com.android.bible.knowbible.mvvm.model
                                                                                              //Поле, содержащее в себе цифру количества глав выбранной книги
data class BookModel(val book_number: Int, val short_name: String, val long_name: String, var number_of_chapters: Int = -1)