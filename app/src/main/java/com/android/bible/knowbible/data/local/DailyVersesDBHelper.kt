//package com.android.bible.knowbible.data.local
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.Cursor
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import com.android.bible.knowbible.mvvm.model.DailyVerseModel
//import com.android.bible.knowbible.utility.Utility
//import io.reactivex.Single
//import java.util.*
//
//class DailyVersesDBHelper(context: Context) {
//    private val tableName: String = "my_daily_verses_table"
//
//    private var dbHelp: DBHelp
//    private var db: SQLiteDatabase
//    private var cv: ContentValues
//
//    init {
//        dbHelp = DBHelp(context)
//        db = dbHelp.writableDatabase
//        cv = ContentValues()
//    }
//
//    fun loadDailyVersesList(): Single<ArrayList<DailyVerseModel>> {
//        val c = db.query(tableName, null, null, null, null, null, null)
//        val collection = ArrayList<DailyVerseModel>()
//        if (c.moveToFirst()) {
//            do {
//                val id = c.getInt(c.getColumnIndex("id"))
//                val bookNumber = c.getInt(c.getColumnIndex("book_number"))
//                val chapterNumber = c.getInt(c.getColumnIndex("chapter_number"))
//                val verseNumber = c.getInt(c.getColumnIndex("verse_number"))
//
//                val dailyVerse = DailyVerseModel(bookNumber, chapterNumber, verseNumber)
//
//                collection.add(dailyVerse)
//            } while (c.moveToNext())
//        }
//        c.close()
//        return Single.fromCallable { collection }
//    }
//
//    fun addDailyVerse(dailyVerse: DailyVerseModel) {
//        cv.put("book_number", dailyVerse.book_number)
//        cv.put("chapter_number", dailyVerse.chapter_number)
//        cv.put("verse_number", dailyVerse.verse_number)
//
//        db.insert(tableName, null, cv)
//    }
//
//    fun deleteDailyVerse(bookNumber: Int, chapterNumber: Int, verseNumber: Int) {
////        db.delete(tableName, "verse_number = $dailyVerseNumber", null)
//        db.delete(tableName, "book_number" + " = ? AND " + "chapter_number" + " = ? AND " + "verse_number" + " = ?", arrayOf(bookNumber.toString(), chapterNumber.toString(), verseNumber.toString()))
//
//    }
//
//    //Метод для проверки элемента на наличие в БД
//    fun isBibleTextInfoInDB(bookNumber: Int, chapterNumber: Int, verseNumber: Int): Boolean {
//        val cursor: Cursor = db.rawQuery("SELECT * FROM my_daily_verses_table where book_number = $bookNumber AND chapter_number = $chapterNumber AND verse_number = $verseNumber", null)
//        if (cursor.count > 0) {
//            cursor.close()
//            return true
//        }
//        cursor.close()
//        return false
//    }
//
//    fun deleteAllVerses() {
//        db.execSQL("delete from $tableName")
//    }
//
//    fun closeDB() {
//        dbHelp.close()
//    }
//
//    private inner class DBHelp(context: Context) : SQLiteOpenHelper(context, "myDailyVerseDB", null, 1) {
//        override fun onCreate(db: SQLiteDatabase) {
//            Utility.log("onCreate DataBase")
//            // создаем таблицу с полями
//            db.execSQL("create table my_daily_verses_table ("
//                    + "id integer primary key autoincrement,"
//                    + "book_number integer,"
//                    + "chapter_number integer,"
//                    + "verse_number integer" + ");")
//        }
//
//        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
//    }
//}
