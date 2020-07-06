package com.android.bible.knowbible.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.android.bible.knowbible.mvvm.model.BibleTextInfoModel
import com.android.bible.knowbible.utility.Utility
import io.reactivex.Single
import java.util.*

class BibleTextInfoDBHelper private constructor() {
    //Используется Singleton, чтобы правильно распределить ресурсы памяти на подключение БД. Подключение к БД достаточно затратно по времени,
    //поэтому если подключения потенциально могут быть частыми, то лучше держать подключение постоянно активным. Singleton позволяет это осуществить.
    companion object {
        private lateinit var dbHelp: DBHelp
        private lateinit var dataBase: SQLiteDatabase
        private lateinit var cv: ContentValues

        private var instance: BibleTextInfoDBHelper? = null
        fun getInstance(context: Context?): BibleTextInfoDBHelper? {
            if (instance == null) {
                instance = BibleTextInfoDBHelper()
                dbHelp = context?.let { instance!!.DBHelp(it) }!!
                dataBase = dbHelp.writableDatabase
                cv = ContentValues()
            }
            return instance
        }

        const val BIBLE_TEXT_INFO_BASE_NAME = "myBibleTextInfoDB"
    }

//    companion object {
//        const val BIBLE_TEXT_INFO_BASE_NAME = "myBibleTextInfoDB"
//    }

//    private var dbHelp: DBHelp
//    private var dataBase: SQLiteDatabase
//    private var cv: ContentValues
//
//    init {
//        dbHelp = DBHelp(context)
//        dataBase = dbHelp.writableDatabase
//        cv = ContentValues()
//    }

    //    fun loadBibleTextInfo(bookNumber: Int, chapterNumber: Int): Single<ArrayList<BibleTextInfoModel>> {
    fun loadBibleTextInfo(bookNumber: Int): Single<ArrayList<BibleTextInfoModel>> {
        val cursor = dataBase.query("my_bible_text_info_table",
                null,
//                "book_number == ? AND chapter_number = ?", //Фильтруем БД для поиска нужных текстов в конкретной главе
//                arrayOf(bookNumber.toString(), chapterNumber.toString()),
                "book_number == ?", //Фильтруем БД для поиска нужных текстов в конкретной главе
                arrayOf(bookNumber.toString()),
                null,
                null,
                null)

        val collection = ArrayList<BibleTextInfoModel>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex("id"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter_number"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse_number"))
                val textColorHex = cursor.getString(cursor.getColumnIndex("text_color_hex"))
                val isTextBold = cursor.getInt(cursor.getColumnIndex("text_bold")) != 0 //Конвертируем int на boolean
                val isTextUnderline = cursor.getInt(cursor.getColumnIndex("text_underline")) != 0 //Конвертируем int на boolean
                val isTextToDailyVerse = cursor.getInt(cursor.getColumnIndex("text_to_daily_verse")) != 0 //Конвертируем int на boolean

                collection.add(BibleTextInfoModel(id, bookNumber, chapterNumber, verseNumber, textColorHex, isTextBold, isTextUnderline, isTextToDailyVerse))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BibleTextInfoModel>> { collection }
    }

    fun addBibleTextInfo(bibleTextInfo: BibleTextInfoModel): Long {
        cv.put("book_number", bibleTextInfo.bookNumber)
        cv.put("chapter_number", bibleTextInfo.chapterNumber)
        cv.put("verse_number", bibleTextInfo.verseNumber)
        cv.put("text_color_hex", bibleTextInfo.textColorHex)
        cv.put("text_bold", bibleTextInfo.isTextBold)
        cv.put("text_underline", bibleTextInfo.isTextUnderline)
        cv.put("text_to_daily_verse", bibleTextInfo.isTextToDailyVerse)

        Utility.log("Add")

        return dataBase.insert("my_bible_text_info_table", null, cv) //Добавляем информацию о тексте и возвращаем id, под которым эти данные добавлены в БД
    }

    fun updateBibleTextInfo(bibleTextInfo: BibleTextInfoModel) {
        cv.put("book_number", bibleTextInfo.bookNumber)
        cv.put("chapter_number", bibleTextInfo.chapterNumber)
        cv.put("verse_number", bibleTextInfo.verseNumber)
        cv.put("text_color_hex", bibleTextInfo.textColorHex)
        cv.put("text_bold", bibleTextInfo.isTextBold)
        cv.put("text_underline", bibleTextInfo.isTextUnderline)
        cv.put("text_to_daily_verse", bibleTextInfo.isTextToDailyVerse)

        Utility.log("Update")

        dataBase.update("my_bible_text_info_table", cv, "id = ?", arrayOf(bibleTextInfo.id.toString()))
    }

    //Метод для проверки элемента на наличие в БД
    fun isBibleTextInfoInDB(textId: Int): Boolean {
        val cursor: Cursor = dataBase.rawQuery("SELECT * FROM my_bible_text_info_table where id= $textId", null)
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    fun deleteBibleTextInfo(bibleTextInfoId: Long) {
        Utility.log("Delete")

        dataBase.delete("my_bible_text_info_table", "id = $bibleTextInfoId", null)
    }

    fun closeDatabase() {
        dbHelp.close()
    }

    private inner class DBHelp(context: Context) : SQLiteOpenHelper(context, BIBLE_TEXT_INFO_BASE_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            Utility.log("DataBase created")
            // Создаем таблицу с полями
            db.execSQL("create table my_bible_text_info_table ("
                    + "id integer primary key autoincrement,"
                    + "book_number integer,"
                    + "chapter_number integer,"
                    + "verse_number integer,"
                    + "text_color_hex text,"
                    + "text_bold integer,"
                    + "text_underline integer,"
                    + "text_to_daily_verse integer" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}
