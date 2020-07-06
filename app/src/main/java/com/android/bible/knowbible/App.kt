package com.android.bible.knowbible

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.android.bible.knowbible.mvvm.model.ArticleModel

class App : Application() {
    companion object {
        var articlesData: ArrayList<ArticleModel>? = null
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}