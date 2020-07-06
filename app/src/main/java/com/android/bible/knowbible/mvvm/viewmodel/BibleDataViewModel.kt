package com.android.bible.knowbible.mvvm.viewmodel

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.bible.knowbible.data.local.RepositoryLocal
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.BookModel
import com.android.bible.knowbible.mvvm.model.ChapterModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class BibleDataViewModel : ViewModel() {
    //Это названия таблиц в файлах БД.
    companion object {
        const val TABLE_BOOKS = "books"
        const val TABLE_VERSES = "verses"
    }

    private val localRepository = RepositoryLocal()
    private var bibleTextDataBase: SQLiteDatabase? = null

    private val dataBaseLiveData = MutableLiveData<SQLiteDatabase>()

    private val booksListLiveData = MutableLiveData<ArrayList<BookModel>>()
    private val chaptersListLiveData = MutableLiveData<ArrayList<ChapterModel>>()
    private val bibleTextOfChapterLiveData = MutableLiveData<ArrayList<BibleTextModel>>()
    private val bibleTextOfBookLiveData = MutableLiveData<ArrayList<ArrayList<BibleTextModel>>>()
    private val bookShortNameLiveData = MutableLiveData<String>()

    fun openDatabase(dbPath: String) {
        bibleTextDataBase = localRepository.openDatabase(dbPath)
    }

    fun getDatabase(): SQLiteDatabase? {
        return bibleTextDataBase
    }

    fun closeDatabase() {
        localRepository.closeDatabase()
    }

    fun getTestamentBooksList(tableName: String, isOldTestament: Boolean): LiveData<ArrayList<BookModel>> {
        getTestamentBooksListData(tableName, isOldTestament)
        return booksListLiveData
    }

    fun getAllBooksList(tableName: String): LiveData<ArrayList<BookModel>> {
        getAllBooksListData(tableName)
        return booksListLiveData
    }

    fun getChaptersList(tableName: String, bookNumber: Int): LiveData<ArrayList<ChapterModel>> {
        getChaptersListData(tableName, bookNumber)
        return chaptersListLiveData
    }

    fun getBibleTextOfChapter(tableName: String, bookNumber: Int, chapterNumber: Int): LiveData<ArrayList<BibleTextModel>> {
        getBibleTextOfChapterData(tableName, bookNumber, chapterNumber)
        return bibleTextOfChapterLiveData
    }

    fun getBibleTextOfBook(tableName: String, bookNumber: Int): LiveData<ArrayList<ArrayList<BibleTextModel>>> {
        getBibleTextOfBookData(tableName, bookNumber)
        return bibleTextOfBookLiveData
    }

    fun getBookShortName(tableName: String, bookNumber: Int): LiveData<String> {
        getBookShortNameData(tableName, bookNumber)
        return bookShortNameLiveData
    }

    @SuppressLint("CheckResult")
    private fun getTestamentBooksListData(tableName: String, isOldTestament: Boolean) {
        localRepository
                .getTestamentBooksList(tableName, isOldTestament)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    booksListLiveData.value = it
                })
    }

    @SuppressLint("CheckResult")
    private fun getAllBooksListData(tableName: String) {
        localRepository
                .getAllBooksList(tableName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    booksListLiveData.value = it
                })
    }

    @SuppressLint("CheckResult")
    private fun getChaptersListData(tableName: String, bookNumber: Int) {
        localRepository
                .getChaptersList(tableName, bookNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    chaptersListLiveData.value = it
                })
    }

    @SuppressLint("CheckResult")
    private fun getBibleTextOfChapterData(tableName: String, bookNumber: Int, chapterNumber: Int) {
        localRepository
                .getBibleTextOfChapter(tableName, bookNumber, chapterNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    bibleTextOfChapterLiveData.value = it
                })
    }

    @SuppressLint("CheckResult")
    private fun getBibleTextOfBookData(tableName: String, bookNumber: Int) {
        localRepository
                .getBibleTextOfBook(tableName, bookNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    bibleTextOfBookLiveData.value = it
                })
    }

    @SuppressLint("CheckResult")
    private fun getBookShortNameData(tableName: String, bookNumber: Int) {
        localRepository
                .getBookShortName(tableName, bookNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    bookShortNameLiveData.value = it
                })
    }
}