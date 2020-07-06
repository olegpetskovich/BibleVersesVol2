package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.AppLanguageModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData

class AppLanguagesRVAdapter(private val context: Context, private val models: ArrayList<AppLanguageModel>) : RecyclerView.Adapter<AppLanguagesRVAdapter.MyViewHolder>() {
    private val saveLoadData = SaveLoadData(context)

    private lateinit var appLanguageChangerListener: IAppLanguageChangerListener

    interface IAppLanguageChangerListener {
        fun changeLanguage(languageCode: String)
    }

    fun setLanguageChangerListener(appLanguageChangerListener: IAppLanguageChangerListener) {
        this.appLanguageChangerListener = appLanguageChangerListener
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_app_language, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvLanguageName.text = models[position].languageName
        holder.tvLocalLanguageName.text = models[position].languageLocalName

        //В зависимости от выбранной темы, выставляем нужные цвета для Views
        if (ThemeManager.theme == ThemeManager.Theme.BOOK) {
            holder.ivSelectedLanguage.setColorFilter(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.ivSelectedLanguage.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
        }

//        if (saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY) == models[position].languageCode) {
        if (saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY) == models[position].languageCode) {
            holder.ivSelectedLanguage.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLanguageName: TextView = itemView.findViewById(R.id.tvLanguageName)
        val tvLocalLanguageName: TextView = itemView.findViewById(R.id.tvLocalLanguageName)

        val ivSelectedLanguage: ImageView = itemView.findViewById(R.id.ivSelectedLanguage)

        init {
            itemView.setOnClickListener {
                //Проверка, если выбирается язык, не установленный ранее, то переключаем язык приложения, если же нажимаем на тот, который уже установлен, то нажатия не происходит
//                if (saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY) != models[adapterPosition].languageCode) {
                if (saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY) != models[adapterPosition].languageCode) {
                    val languageCode = models[adapterPosition].languageCode
                    appLanguageChangerListener.changeLanguage(languageCode)
                }
            }
            themeChanger.changeItemTheme() //Смена темы для айтемов
        }
    }
}