package com.android.bible.knowbible.data.local

import android.database.sqlite.SQLiteDatabase
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.BookModel
import com.android.bible.knowbible.mvvm.model.ChapterModel
import io.reactivex.Single
import kotlin.collections.ArrayList


class BibleTextDBHelper {
    private lateinit var dataBase: SQLiteDatabase
    private val matthewBookNumber = 470 //Это код книги Евангелие Матфея в Базе Данных. С помощью этого номера будет определяться, данные какого завета возвращать по запросу.

    //Соединение с БД выведено в отдельный метод, чтобы не приходилось подсоединяться к БД при каждом вызове методов
    fun openDatabase(dbPath: String): SQLiteDatabase {
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        return dataBase
    }

    fun closeDatabase() {
        dataBase.close()
    }

    fun loadTestamentBooksList(tableName: String, isOldTestament: Boolean): Single<ArrayList<BookModel>> {
//        val cursor = if (isOldTestament) {
//            dataBase.query(tableName, null, "book_number < ?", arrayOf(matthewBookNumber.toString()), null, null, null)
//        } else {
//            dataBase.query(tableName, null, "book_number >= ?", arrayOf(matthewBookNumber.toString()), null, null, null)
//        }
        val cursor = dataBase.query(tableName, null, null, null, null, null, null)
        val collection = ArrayList<BookModel>()
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                if (isOldTestament) {
                    if (bookNumber >= matthewBookNumber) break
                } else if (bookNumber < matthewBookNumber) continue

                val bookShortName = cursor.getString(cursor.getColumnIndex("short_name"))
                val bookLongName = cursor.getString(cursor.getColumnIndex("long_name"))

                val bookInfo = BookModel(bookNumber, bookShortName, bookLongName)

                collection.add(bookInfo)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BookModel>> { collection }
    }

    fun loadAllBooksList(tableName: String): Single<ArrayList<BookModel>> {
        val cursor = dataBase.query(tableName, null, null, null, null, null, null)
        val collection = ArrayList<BookModel>()
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val bookShortName = cursor.getString(cursor.getColumnIndex("short_name"))
                val bookLongName = cursor.getString(cursor.getColumnIndex("long_name"))

                val bookInfo = BookModel(bookNumber, bookShortName, bookLongName)

                collection.add(bookInfo)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BookModel>> { collection }
    }

    fun loadChaptersList(tableName: String, bookNumber: Int): Single<ArrayList<ChapterModel>> {
        val cursor = dataBase.query(tableName, arrayOf("book_number", "chapter"), "book_number == ?", arrayOf(bookNumber.toString()), null, null, null)
        val collection = ArrayList<ChapterModel>()

        var chapterNumber = 0
        if (cursor.moveToFirst()) {
            do {
                val chapter = cursor.getInt(cursor.getColumnIndex("chapter"))

                //Алгоритм получения количества глав конкретной книги. Поскольку в файле БД номера глав повторяются, нужно отсеять повторяющиеся номера.
                if (chapterNumber == chapter) continue
                else chapterNumber = chapter

                collection.add(ChapterModel(bookNumber, chapterNumber))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<ChapterModel>> { collection }
    }

    fun loadBibleTextOfChapter(tableName: String, myBookNumber: Int, myChapterNumber: Int): Single<ArrayList<BibleTextModel>> {
        val cursor = dataBase.query(tableName,
                null,
                "book_number == ? AND chapter = ?",
                arrayOf(myBookNumber.toString(), myChapterNumber.toString()),
                null,
                null,
                null)

        val collection = ArrayList<BibleTextModel>()

        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                collection.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/, isTextToDailyVerse = false/*Тут это заглушка*/))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BibleTextModel>> { collection }
    }

    fun loadBibleTextOfBook(tableName: String, myBookNumber: Int): Single<ArrayList<ArrayList<BibleTextModel>>> {
        val cursor = dataBase.query(tableName,
                null,
                "book_number == ?",
                arrayOf(myBookNumber.toString()),
                null,
                null,
                null)

        val collectionOfChaptersText = ArrayList<ArrayList<BibleTextModel>>()
        var chapterTextList: ArrayList<BibleTextModel>? = null

        var myChapterNumber = 0
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                if (myChapterNumber != chapterNumber) {
                    myChapterNumber = chapterNumber
                    chapterTextList?.let { collectionOfChaptersText.add(it) }
                    chapterTextList = ArrayList()
                }
                chapterTextList?.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/, isTextToDailyVerse = false/*Тут это заглушка*/))

            } while (cursor.moveToNext())
            chapterTextList?.let { collectionOfChaptersText.add(it) } //Поскольку коллекция добавляется в коллекцию в каждой следующей итерации, то последнюю коллекцию нужно добавлять после того, как отработал цикл. Может потом можно будет пересмотреть и сделать лучше.
        }
        cursor.close()
        return Single.fromCallable<ArrayList<ArrayList<BibleTextModel>>> { collectionOfChaptersText }
    }

    fun loadBookShortName(tableName: String, myBookNumber: Int): Single<String> {
        val cursor = dataBase.query(tableName,
                null,
                null,
                null,
                null,
                null,
                null)

        var shortName = ""
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                if (myBookNumber == bookNumber) {
                    shortName = cursor.getString(cursor.getColumnIndex("short_name"))
                    break
                }

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable { shortName }
    }
}
