package com.android.bible.knowbible.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.android.bible.knowbible.mvvm.model.ArticleModel
import com.android.bible.knowbible.utility.Utility
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.util.*

class ArticlesDBHelper(val context: Context) {
    companion object {
        const val ARTICLES_DATA_BASE_NAME = "myArticlesDB"
    }

    private var dbHelp: DBHelp
    private var db: SQLiteDatabase
    private var cv: ContentValues

    init {
        dbHelp = DBHelp(context)
        db = dbHelp.writableDatabase
        cv = ContentValues()
    }

    fun createDatabase() {
//        dbHelp = DBHelp(context)
//        db = dbHelp.writableDatabase
//        cv = ContentValues()
    }

    fun loadArticles(): Single<ArrayList<ArticleModel>> {
        val c = db.query("my_articles_table", null, null, null, null, null, null)
        val collection = ArrayList<ArticleModel>()
        if (c.moveToFirst()) {
            do {
                val articleNameRu = c.getString(c.getColumnIndex("article_name_ru"))
                val articleNameUk = c.getString(c.getColumnIndex("article_name_uk"))
                val articleNameEn = c.getString(c.getColumnIndex("article_name_en"))

                val articleTextRu = c.getString(c.getColumnIndex("article_text_ru"))
                val articleTextUk = c.getString(c.getColumnIndex("article_text_uk"))
                val articleTextEn = c.getString(c.getColumnIndex("article_text_en"))

                val authorNameRu = c.getString(c.getColumnIndex("author_name_ru"))
                val authorNameUk = c.getString(c.getColumnIndex("author_name_uk"))
                val authorNameEn = c.getString(c.getColumnIndex("author_name_en"))

                val articleImage = c.getBlob(c.getColumnIndex("article_image"))
                val newArticleTextColor = c.getString(c.getColumnIndex("new_article_text_color"))

                val isArticleNew = c.getInt(c.getColumnIndex("is_article_new")) == 1

                val articleModel = ArticleModel()
                articleModel.article_name_ru = articleNameRu
                articleModel.article_name_uk = articleNameUk
                articleModel.article_name_en = articleNameEn

                articleModel.article_text_ru = articleTextRu
                articleModel.article_text_uk = articleTextUk
                articleModel.article_text_en = articleTextEn

                articleModel.author_name_ru = authorNameRu
                articleModel.author_name_uk = authorNameUk
                articleModel.author_name_en = authorNameEn

                articleModel.imageBitmap = BitmapFactory.decodeByteArray(articleImage, 0, articleImage.size)
                articleModel.new_article_text_color = newArticleTextColor
                articleModel.isIs_article_new = isArticleNew

                collection.add(articleModel)

            } while (c.moveToNext())
        }
        c.close()
        return Single.fromCallable<ArrayList<ArticleModel>> { collection }
    }

    fun addArticles(articles: ArrayList<ArticleModel>) {
        for (article in articles) {
            cv.put("article_name_ru", article.article_name_ru)
            cv.put("article_name_uk", article.article_name_uk)
            cv.put("article_name_en", article.article_name_en)

            cv.put("article_text_ru", article.article_text_ru)
            cv.put("article_text_uk", article.article_text_uk)
            cv.put("article_text_en", article.article_text_en)

            cv.put("author_name_ru", article.author_name_ru)
            cv.put("author_name_uk", article.author_name_uk)
            cv.put("author_name_en", article.author_name_en)

            val imageBitmap = article.imageBitmap
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(CompressFormat.PNG, 0, outputStream)
            cv.put("article_image", outputStream.toByteArray())
            cv.put("new_article_text_color", article.new_article_text_color)

            if (article.isIs_article_new) cv.put("is_article_new", 1)
            else cv.put("is_article_new", 0)

            db.insert("my_articles_table", null, cv)
        }
    }

    fun closeDatabase() {
        dbHelp.close()
    }

    private inner class DBHelp(context: Context) : SQLiteOpenHelper(context, ARTICLES_DATA_BASE_NAME, null, 1) {

        override fun onCreate(db: SQLiteDatabase) {
            Utility.log("DataBase created")
            // Создаем таблицу с полями
            db.execSQL("create table my_articles_table ("
                    + "id integer primary key autoincrement,"
                    + "article_name_ru text,"
                    + "article_name_uk text,"
                    + "article_name_en text,"

                    + "article_text_ru text,"
                    + "article_text_uk text,"
                    + "article_text_en text,"

                    + "author_name_ru text,"
                    + "author_name_uk text,"
                    + "author_name_en text,"

                    + "article_image blob,"
                    + "new_article_text_color text,"
                    + "is_article_new integer" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}
