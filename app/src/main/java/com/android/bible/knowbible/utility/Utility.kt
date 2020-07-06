package com.android.bible.knowbible.utility

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTranslationModel
import java.io.File

class Utility {
    companion object {
        //Исправить метод, потому что некоторые функции deprecated на 29 sdk
        fun isNetworkAvailable(context: Context): Boolean? {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        //Проверка на то, скачан ли хоть один перевод или же папка, предназначенная для скачанных переводов, пуста
        fun isTranslationsDownloaded(context: Context): Boolean {
            val directory = File(context.getExternalFilesDir(context.getString(R.string.folder_name)).toString())
            val contents = directory.listFiles()
            return contents != null && contents.isNotEmpty()
        }

        //Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится имя скачанного файла, но его самого не существует.
        //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
        //потому что в действительности он будет удалён
        fun isSelectedTranslationDownloaded(context: Context, bibleTranslationInfo: BibleTranslationModel): Boolean {
            val applicationFile = File(context.getExternalFilesDir(context.getString(R.string.folder_name)).toString() + "/" + bibleTranslationInfo.translationDBFileName)
            return applicationFile.exists()
        }

        fun log(text: String) {
            Log.d("MyTag", text)
        }

        //Конвертируем вводное число DP в пиксели
        fun convertDbInPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun viewAnimatorX(pixels: Float, view: View, myDuration: Long): ObjectAnimator? {
            return ObjectAnimator
                    .ofFloat(view, "translationX", pixels)
                    .apply {
                        duration = myDuration
                    }
        }

        fun viewAnimatorY(pixels: Float, view: View, myDuration: Long): ObjectAnimator? {
            return ObjectAnimator
                    .ofFloat(view, "translationY", pixels)
                    .apply {
                        duration = myDuration
                    }
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view: View? = activity.currentFocus
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}