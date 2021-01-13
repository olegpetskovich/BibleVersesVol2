package com.android.bible.knowbible.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.android.bible.knowbible.mvvm.model.NoteModel
import io.reactivex.Single
import java.util.*

class NotesDBHelper(context: Context) {
    private val tableName: String = "my_notes_table"

    private var dbHelp: DBHelp
    private var db: SQLiteDatabase
    private var cv: ContentValues

    init {
        dbHelp = DBHelp(context)
        db = dbHelp.writableDatabase
        cv = ContentValues()
    }

    fun loadNotes(): Single<ArrayList<NoteModel>> {
        val c = db.query(tableName, null, null, null, null, null, null)
        val collection = ArrayList<NoteModel>()
        if (c.moveToFirst()) {
            do {
                val isNoteForVerse = c.getInt(c.getColumnIndex("is_note_for_verse")) != 0
                val verse = c.getString(c.getColumnIndex("verse"))
                val text = c.getString(c.getColumnIndex("text"))
                val id = c.getInt(c.getColumnIndex("id"))

                val note: NoteModel
                //Если это заметка к стиху, то получаем данные стиха, что при нажатии на него, можно было к нему перейти
                note = (if (isNoteForVerse) {
                    val bookNumber = c.getInt(c.getColumnIndex("book_number"))
                    val chapterNumber = c.getInt(c.getColumnIndex("chapter_number"))
                    val verseNumber = c.getInt(c.getColumnIndex("verse_number"))
                    NoteModel(id, isNoteForVerse, bookNumber, chapterNumber, verseNumber, verse, text)
                } else NoteModel(id, isNoteForVerse, -1, -1, -1, verse, text))

                collection.add(note)

            } while (c.moveToNext())
        }
        c.close()
        return Single.fromCallable { collection }
    }

    fun addNote(note: NoteModel) {
        cv.put("is_note_for_verse", note.isNoteForVerse)
        cv.put("book_number", note.bookNumber)
        cv.put("chapter_number", note.chapterNumber)
        cv.put("verse_number", note.verseNumber)
        cv.put("verse", note.verseText)
        cv.put("text", note.text)

        db.insert(tableName, null, cv)
    }

    fun updateNote(note: NoteModel) {
        cv.put("is_note_for_verse", note.isNoteForVerse)
        cv.put("book_number", note.bookNumber)
        cv.put("chapter_number", note.chapterNumber)
        cv.put("verse_number", note.verseNumber)
        cv.put("verse", note.verseText)
        cv.put("text", note.text)

        db.update(tableName, cv, "id = ?", arrayOf(note.id.toString()))
    }

    fun deleteVerse(noteId: Int) {
        db.delete(tableName, "id = $noteId", null)
    }

    fun deleteAllVerses() {
        db.execSQL("delete from $tableName")
    }

    fun closeDB() {
        dbHelp.close()
    }

    private inner class DBHelp(context: Context) : SQLiteOpenHelper(context, "myDB", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            // создаем таблицу с полями
            db.execSQL("create table my_notes_table ("
                    + "id integer primary key autoincrement,"
                    + "is_note_for_verse integer,"
                    + "book_number integer,"
                    + "chapter_number integer,"
                    + "verse_number integer,"
                    + "verse text,"
                    + "text text" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}
