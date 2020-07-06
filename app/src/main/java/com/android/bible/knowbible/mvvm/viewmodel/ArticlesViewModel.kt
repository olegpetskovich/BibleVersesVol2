package com.android.bible.knowbible.mvvm.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.bible.knowbible.data.local.ArticlesDBHelper
import com.android.bible.knowbible.mvvm.model.ArticleModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val fireBaseStorage = FirebaseStorage.getInstance()

//    private val articlesDBHelper = ArticlesDBHelper(getApplication<Application>().applicationContext)
    private val articlesLiveData = MutableLiveData<ArrayList<ArticleModel>>()
    private val articlePictureLiveData = MutableLiveData<Bitmap>()

    //Эти методы могут понадобиться для сохранения статей в локальной БД на всё время, если такая настройка в дальнейшем будет предусмотрена в приложении.
    // На данный момент она не нужна, потому как заменена сохранением данных в поле в классе App
//    fun createArticlesDB() {
//        articlesDBHelper.createDatabase()
//    }
//    fun setArticles(articles: ArrayList<ArticleModel>) {
//        articlesDBHelper.addArticles(articles)
//    }
//    fun getArticles(): LiveData<ArrayList<ArticleModel>> {
//        loadArticlesData()
//        return articlesLiveData
//    }
//    fun closeArticlesDB() {
//        articlesDBHelper.closeDatabase()
//    }

//    @SuppressLint("CheckResult")
//    private fun loadArticlesData() {
//        articlesDBHelper
//                .loadArticles()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(Consumer {
//                    articlesLiveData.value = it
//                })
//    }

    //Метод для загрузки картинки, которая потом будет помещена в адаптер списка и в БД для кэширования
    fun loadArticlePicture(uri: String): LiveData<Bitmap> {
        val listRef = fireBaseStorage.reference.child(uri)
        listRef.downloadUrl.addOnSuccessListener { pictureUri ->
            Glide.with(getApplication<Application>().applicationContext)
                    .asBitmap()
                    .load(pictureUri)
                    .into(object : CustomTarget<Bitmap>(700, 400) {
                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            articlePictureLiveData.value = resource
                        }

                    })
        }
        return articlePictureLiveData
    }
}