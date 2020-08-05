package com.android.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.adapter.SearchedVersesListRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment(), IThemeChanger {
    private var searchingSection = -1

    companion object {
        const val ALL_BIBLE_SECTION: Int = 1
        const val OLD_TESTAMENT_SECTION: Int = 2
        const val NEW_TESTAMENT_SECTION: Int = 3
    }


    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var rvAdapter: SearchedVersesListRVAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var tvCount: TextView
    private lateinit var etSearch: AppCompatEditText

    private lateinit var radioGroupSearch: RadioGroup

    private lateinit var rbAllBible: MaterialRadioButton
    private lateinit var rbOldTestament: MaterialRadioButton
    private lateinit var rbNewTestament: MaterialRadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_search, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        radioGroupSearch = myView.findViewById(R.id.radioGroupSearch)

        rbAllBible = myView.findViewById(R.id.rbAllBible)
        rbOldTestament = myView.findViewById(R.id.rbOldTestament)
        rbNewTestament = myView.findViewById(R.id.rbNewTestament)

        recyclerView = myView.findViewById(R.id.recyclerView)
        progressBar = myView.findViewById(R.id.progressBar)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        tvCount = myView.findViewById(R.id.tvCount)

        etSearch = myView.findViewById(R.id.etSearch)
        etSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                progressBar.visibility = View.VISIBLE
                //Вводимый текст для поиска должен состоять как минимум из 3х букв
                if (etSearch.length() > 2) {
                    when (true) {
                        rbAllBible.isChecked -> searchingSection = ALL_BIBLE_SECTION
                        rbOldTestament.isChecked -> searchingSection = OLD_TESTAMENT_SECTION
                        rbNewTestament.isChecked -> searchingSection = NEW_TESTAMENT_SECTION
                    }
                    Utility.hideKeyboard(activity!!)

                    GlobalScope.launch(Dispatchers.Main) {
                        delay(100)
                        //Выводим осуществление запроса в отдельный поток для того, чтобы не стопорить главный
                        val mainHandler = Handler(context!!.mainLooper)
                        val myRunnable = Runnable {
                            bibleDataViewModel
                                    .getSearchedBibleVerses(BibleDataViewModel.TABLE_VERSES, searchingSection, etSearch.text.toString())
                                    .observe(viewLifecycleOwner, Observer { searchedVerses ->
                                        tvCount.text = searchedVerses.size.toString()

                                        rvAdapter = SearchedVersesListRVAdapter(context!!, searchedVerses, bibleDataViewModel, viewLifecycleOwner)
                                        rvAdapter.setRecyclerViewThemeChangerListener(this@SearchFragment) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.

                                        recyclerView = myView.findViewById(R.id.recyclerView)
                                        recyclerView.layoutManager = LinearLayoutManager(context)
                                        recyclerView.adapter = rvAdapter
                                        progressBar.visibility = View.GONE

                                    })
                        }
                        mainHandler.post(myRunnable)
                    }
                } else progressBar.visibility = View.GONE
                return@OnEditorActionListener true
            }
            false
        })

        return myView
    }

    //Метод для смены цвета checkBox, код из StackOverflow, не вникал в него
    private fun setRadioButtonColor(radioButton: MaterialRadioButton, uncheckedColor: Int, checkedColor: Int, textColor: Int) {
        radioButton.setTextColor(textColor)

        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(uncheckedColor, checkedColor))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CompoundButtonCompat.setButtonTintList(radioButton, colorStateList)
        } else {
            radioButton.buttonTintList = colorStateList
        }
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
    }

    override fun onResume() {
        super.onResume()
        //Есть класс MyRadioButton, но он не меняет цвета должным образом, поэтому приходится менять цвета здесь
        when (SaveLoadData(context!!).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.LIGHT
                setRadioButtonColor(rbAllBible,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextLightTheme))
                setRadioButtonColor(rbOldTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextLightTheme))
                setRadioButtonColor(rbNewTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextLightTheme))
            }
            ThemeModeFragment.DARK_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.DARK
                setRadioButtonColor(rbAllBible,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextDarkTheme))
                setRadioButtonColor(rbOldTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextDarkTheme))
                setRadioButtonColor(rbNewTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorCheckedLightDarkThemes),
                        ContextCompat.getColor(context!!, R.color.colorTextDarkTheme))
            }
            ThemeModeFragment.BOOK_THEME -> {
                ThemeManager.theme = ThemeManager.Theme.BOOK
                setRadioButtonColor(rbAllBible,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorCheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorTextBookTheme))
                setRadioButtonColor(rbOldTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorCheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorTextBookTheme))
                setRadioButtonColor(rbNewTestament,
                        ContextCompat.getColor(context!!, R.color.colorUncheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorCheckedBookTheme),
                        ContextCompat.getColor(context!!, R.color.colorTextBookTheme))
            }
        }

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

    override fun onPause() {
        super.onPause()
        activity?.let { Utility.hideKeyboard(it) }
    }
}
