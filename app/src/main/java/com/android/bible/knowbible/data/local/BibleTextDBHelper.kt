package com.android.bible.knowbible.data.local

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.android.bible.knowbible.mvvm.model.*
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection.SearchFragment.Companion.NEW_TESTAMENT_SECTION
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection.SearchFragment.Companion.OLD_TESTAMENT_SECTION
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.Utility
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.regex.Matcher
import java.util.regex.Pattern


class BibleTextDBHelper {
    private lateinit var dataBase: SQLiteDatabase
    private val matthewBookNumber = 470 //Это код книги Евангелие Матфея в Базе Данных. С помощью этого номера будет определяться, данные какого завета возвращать по запросу.
    private lateinit var cv: ContentValues

    //Соединение с БД выведено в отдельный метод, чтобы не приходилось подсоединяться к БД при каждом вызове методов
    fun openDatabase(dbPath: String): SQLiteDatabase {
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        cv = ContentValues()
        return dataBase
    }

    fun closeDatabase() {
        dataBase.close()
    }

    fun loadAllBooksList(tableName: String): Single<ArrayList<BookModel>> {
        val cursor = dataBase.query(tableName, null, null, null, null, null, null)
        val mainCollection = ArrayList<BookModel>()
        val secondCollection = ArrayList<BookModel>()
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val bookShortName = cursor.getString(cursor.getColumnIndex("short_name"))
                val bookLongName = cursor.getString(cursor.getColumnIndex("long_name"))

                val bookInfo = BookModel(bookNumber, bookShortName, bookLongName)

                //Если номер книги идет в промежутке от 660 до 720 включительно(то есть от книги Иакова до книги Иуды),
                //то добавляем их в отдельную коллекцию, чтобы потом добавить в главную коллекцию в нужное место ради стандартного порядка расопложения книг Библии
                if (bookNumber in 660..720) secondCollection.add(bookInfo)
                else mainCollection.add(bookInfo)
            } while (cursor.moveToNext())
        }

        //Индекс 44 это индекс, идущий по счёту после книги Деяний. Добавляем после Деяний соборные послания, а потом уже идут послания Павла и остальное
        mainCollection.addAll(44, secondCollection)

        //Добавляем в каждый объект коллекции mainCollection информацию о количестве глав для каждой книги
        EnumBooksList.values().forEachIndexed { index, enumBookChapters ->
            mainCollection[index].number_of_chapters = enumBookChapters.numberOfChapters
        }

        cursor.close()
        return Single.fromCallable<ArrayList<BookModel>> { mainCollection }
    }

    fun loadTestamentBooksList(tableName: String, isOldTestament: Boolean): Single<ArrayList<BookModel>> {
//        val cursor = if (isOldTestament) {
//            dataBase.query(tableName, null, "book_number < ?", arrayOf(matthewBookNumber.toString()), null, null, null)
//        } else {
//            dataBase.query(tableName, null, "book_number >= ?", arrayOf(matthewBookNumber.toString()), null, null, null)
//        }
        val cursor = dataBase.query(tableName, null, null, null, null, null, null)
        val mainCollection = ArrayList<BookModel>()
        val secondCollection = ArrayList<BookModel>()
        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                if (isOldTestament) {
                    if (bookNumber >= matthewBookNumber) break
                } else if (bookNumber < matthewBookNumber) continue

                val bookShortName = cursor.getString(cursor.getColumnIndex("short_name"))
                val bookLongName = cursor.getString(cursor.getColumnIndex("long_name"))

                val bookInfo = BookModel(bookNumber, bookShortName, bookLongName)

                //В Новом Завете меняем порядок расположения книг, потому что в БД он не такой, как стандартно принятно.
                //В БД после Деяний идут послания Павла, а в привычном варианте сначала идут соборные послания, а потом уже послания Павла
                if (!isOldTestament) {
                    //Если номер книги идет в промежутке от 660 до 720 включительно(то есть от книги Иакова до книги Иуды),
                    //то добавляем их в отдельную коллекцию, чтобы потом добавить в главную коллекцию в нужное место ради стандартного порядка расопложения книг Библии
                    if (bookNumber in 660..720) secondCollection.add(bookInfo)
                    else mainCollection.add(bookInfo)
                } else
                    mainCollection.add(bookInfo)

            } while (cursor.moveToNext())
        }

        if (!isOldTestament)
        //Индекс 5 это индекс, идущий по счёту после книги Деяний. Добавляем после Деяний соборные послания, а потом уже идут послания Павла и остальное
            mainCollection.addAll(5, secondCollection)

        val booksList = ArrayList<EnumBooksList>()
        if (isOldTestament) {
            for (element in EnumBooksList.values()) {
                if (element.bookNumber == matthewBookNumber) break
                booksList.add(element)
            }
        } else {
            for (element in EnumBooksList.values()) {
                if (element.bookNumber < matthewBookNumber) continue
                booksList.add(element)
            }
        }

        //Добавляем в каждый объект коллекции mainCollection информацию о количестве глав для каждой книги
        booksList.forEachIndexed { index, enumBookChapters ->
            mainCollection[index].number_of_chapters = enumBookChapters.numberOfChapters
        }

        cursor.close()
        return Single.fromCallable<ArrayList<BookModel>> { mainCollection }
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

    fun loadAllBibleTexts(tableName: String): Single<ArrayList<BibleTextModel>> {
        val cursor = dataBase.query(tableName,
                null,
                null,
                null,
                null,
                null,
                null)

        val allBibleTextsList: ArrayList<BibleTextModel> = ArrayList()

        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                allBibleTextsList.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/))


            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BibleTextModel>> { allBibleTextsList }
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
                chapterTextList?.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/))

            } while (cursor.moveToNext())
            chapterTextList?.let { collectionOfChaptersText.add(it) } //Поскольку коллекция добавляется в коллекцию в каждой следующей итерации, то последнюю коллекцию нужно добавлять после того, как отработал цикл. Может потом можно будет пересмотреть и сделать лучше.
        }
        cursor.close()
        return Single.fromCallable<ArrayList<ArrayList<BibleTextModel>>> { collectionOfChaptersText }
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

                collection.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BibleTextModel>> { collection }
    }

    fun loadBibleVerse(tableName: String, myBookNumber: Int, myChapterNumber: Int, myVerseNumber: Int): Single<BibleTextModel> {
        val cursor = dataBase.query(tableName,
                null,
                "book_number == ? AND chapter = ? AND verse = ?",
                arrayOf(myBookNumber.toString(), myChapterNumber.toString(), myVerseNumber.toString()),
                null,
                null,
                null)

        var verse: BibleTextModel? = null

        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                verse = BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, text, null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/)

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<BibleTextModel> { verse }
    }

    @SuppressLint("CheckResult")
    fun loadDailyVerse(tableName: String, myBookNumber: Int, myChapterNumber: Int, myVersesNumbers: ArrayList<Int>): Single<DailyVerseModel> {
        val cursor: Cursor? =
                if (myVersesNumbers.size == 1)
                    dataBase.query(tableName,
                            null,
                            "book_number == ? AND chapter = ? AND verse = ?",
                            arrayOf(myBookNumber.toString(), myChapterNumber.toString(), myVersesNumbers[0].toString()),
                            null,
                            null,
                            null)
                else
                    dataBase.query(tableName,
                            null,
                            "book_number == ? AND chapter = ?",
                            arrayOf(myBookNumber.toString(), myChapterNumber.toString()),
                            null,
                            null,
                            null)

        var bookNumber: Int = -1
        var chapterNumber: Int = -1

        val versesNumbers = ArrayList<Int>()
        var verseText = ""
        if (cursor!!.moveToFirst()) {
            do {
                bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))

                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                for (number in myVersesNumbers)
                    if (verseNumber == number) {
                        versesNumbers.add(verseNumber)
                        verseText += text
                    }

            } while (cursor.moveToNext())
        }

        cursor.close()
        return Single.fromCallable<DailyVerseModel> { DailyVerseModel(bookNumber, chapterNumber, -1, versesNumbers, verseText) }
    }

    @SuppressLint("CheckResult")
    fun loadSearchedBibleVerse(tableName: String, searchingSection: Int, searchingText: String): Single<ArrayList<BibleTextModel>> {
        val sql = "SELECT * FROM $tableName WHERE text LIKE '%$searchingText%'"
        val cursor = dataBase.rawQuery(sql, null)

        val verses: ArrayList<BibleTextModel> = ArrayList()

        if (cursor.moveToFirst()) {
            do {
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))

                //Делаем фильтр, чтобы искать тексты по одному из Заветов
                if (searchingSection == OLD_TESTAMENT_SECTION) {
                    if (bookNumber >= matthewBookNumber) continue
                } else if (searchingSection == NEW_TESTAMENT_SECTION) if (bookNumber < matthewBookNumber) continue

                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse"))
                val text = cursor.getString(cursor.getColumnIndex("text"))

                var str = text

                loadBookShortName(BibleDataViewModel.TABLE_BOOKS, bookNumber)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { shortName ->
                            //Достаём из строки все слова, которые совпадают со словом или фразой, которое ищет пользователь и каждон из найденного слова или фразы выделяем
                            val pattern: Pattern = Pattern.compile(searchingText, Pattern.CASE_INSENSITIVE)
                            val matcher: Matcher = pattern.matcher(str)
                            while (matcher.find()) {
                                val foundedWord = matcher.group()
                                if (str.contains(foundedWord, true)) {
                                    str = str.replace("\\s{2,}".toRegex(), " ").trim()
                                    str = str.replace(foundedWord, "<b>$foundedWord</b>") //Выделяем найденное слово
                                }
                            }

                            verses.add(BibleTextModel(-1/*Тут это заглушка*/, bookNumber, chapterNumber, verseNumber, "<b>$shortName. $chapterNumber:$verseNumber</b> $str", null, isTextBold = false/*Тут это заглушка*/, isTextUnderline = false/*Тут это заглушка*/))
                        }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<BibleTextModel>> { verses }
    }

    fun updateBibleText(bibleTextModel: BibleTextModel) {
        val text = Utility.getClearedStringFromTags(bibleTextModel.text)
        cv.put("text", text)

        Utility.log("Text before: " + bibleTextModel.text)
        Utility.log("Text after: $text")

        dataBase.update("verses", cv, "book_number = ? AND chapter = ? AND verse = ?", arrayOf(bibleTextModel.book_number.toString(), bibleTextModel.chapter_number.toString(), bibleTextModel.verse_number.toString()))
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
