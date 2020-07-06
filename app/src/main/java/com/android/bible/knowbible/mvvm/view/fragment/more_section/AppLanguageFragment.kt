package com.android.bible.knowbible.mvvm.view.fragment.more_section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.AppLanguageModel
import com.android.bible.knowbible.mvvm.view.adapter.AppLanguagesRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData

class AppLanguageFragment : Fragment(), IThemeChanger, AppLanguagesRVAdapter.IAppLanguageChangerListener {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var appLanguageChangerListener: IAppLanguageChangerListener

    private lateinit var saveLoadData: SaveLoadData

    companion object {
        const val APP_LANGUAGE_CODE_KEY = "APP_LANGUAGE_CODE_KEY"
        const val APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY = "APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY" //Это поле нужно только для AppLanguagesRVAdapter, чтобы получив сохранённый languageCode в MainActivity можно было отобразить его в списке
    }

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var rvAdapter: AppLanguagesRVAdapter

    interface IAppLanguageChangerListener {
        fun changeLanguage(languageCode: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_bible_translations, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)

        val recyclerView: RecyclerView = myView.findViewById(R.id.recyclerView)
        rvAdapter = AppLanguagesRVAdapter(context!!, getLanguagesList())
        rvAdapter.setRecyclerViewThemeChangerListener(this) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмент, не меняя при этом тему для списка, то в списке тема не поменяется.
        rvAdapter.setLanguageChangerListener(this)

        recyclerView.adapter = rvAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        return myView
    }

    private fun getLanguagesList(): ArrayList<AppLanguageModel> {
        val languages = ArrayList<AppLanguageModel>()
        languages.add(AppLanguageModel(getString(R.string.english_lang), getString(R.string.english_lang_local_name), "en"))
        languages.add(AppLanguageModel(getString(R.string.russian_lang), getString(R.string.russian_lang_local_name), "ru"))
        languages.add(AppLanguageModel(getString(R.string.ukrainian_lang), getString(R.string.ukrainian_lang_local_name), "uk"))
        return languages
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
        if (context is IAppLanguageChangerListener) appLanguageChangerListener = context
        else throw RuntimeException("$context must implement IAppLanguageChangerListener")
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(2)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun changeLanguage(languageCode: String) {
        appLanguageChangerListener.changeLanguage(languageCode)
        saveLoadData.saveString(APP_LANGUAGE_CODE_KEY, languageCode)
    }
}