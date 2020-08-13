package com.android.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.adapter.SearchedVersesListRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
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

class SearchFragment : Fragment(), IChangeFragment, ISelectBibleText {
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

    private lateinit var etSearch: AppCompatEditText
    private lateinit var btnCleanText: ImageView
    private lateinit var tvCount: TextView

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

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        etSearch = myView.findViewById(R.id.etSearch)

        btnCleanText = myView.findViewById(R.id.btnCleanText)
        btnCleanText.setOnClickListener { etSearch.setText("") } //Очищаем текст поля ввода при нажатии на кнопку очищения

        tvCount = myView.findViewById(R.id.tvCount)

        radioGroupSearch = myView.findViewById(R.id.radioGroupSearch)
        //При переключении RadioButtons поиск будет сразу начинаться, если в поле поиска больше двух букв
        radioGroupSearch.setOnCheckedChangeListener { _, _ -> searchText(etSearch.text.toString()) }

        rbAllBible = myView.findViewById(R.id.rbAllBible)
        rbOldTestament = myView.findViewById(R.id.rbOldTestament)
        rbNewTestament = myView.findViewById(R.id.rbNewTestament)

        recyclerView = myView.findViewById(R.id.recyclerView)
        progressBar = myView.findViewById(R.id.progressBar)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val animation: Animation?
                if (count > 0) {
                    animation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_slow)
                    //Делаем проверку на видимость, чтобы кнопка не анимировалось при печатании каждой буквы
                    if (btnCleanText.visibility == View.VISIBLE) return
                    else btnCleanText.visibility = View.VISIBLE
                } else {
                    animation = AnimationUtils.loadAnimation(context, R.anim.zoom_out_slow)
                    //Делаем проверку на видимость, чтобы кнопка не анимировалось при стирании каждой буквы
                    if (btnCleanText.visibility == View.GONE) return
                    else btnCleanText.visibility = View.GONE
                }
                btnCleanText.startAnimation(animation)
            }
        })

        etSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (etSearch.length() > 2) searchText(etSearch.text.toString())
                else Toast.makeText(context, getString(R.string.toast_at_least_three_letters), Toast.LENGTH_SHORT).show()
                return@OnEditorActionListener true
            }
            false
        })

        return myView
    }

    private fun searchText(searchText: String) {
        //Вводимый текст для поиска должен состоять как минимум из 3х букв
        if (etSearch.length() > 2) {
            progressBar.visibility = View.VISIBLE
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
                    bibleDataViewModel                                                                 //Перед отправкой текста для поиска, очищаем его от лишний пробелов, если такие имеются
                                                                                                       //Метод trim() не нужен, потому что пользователь может захотеть найти слово с пробелом до или после него
                            .getSearchedBibleVerses(BibleDataViewModel.TABLE_VERSES, searchingSection, searchText.replace("\\s+".toRegex(), " "))
                            .observe(viewLifecycleOwner, Observer { searchedVerses ->
                                tvCount.text = searchedVerses.size.toString()

                                rvAdapter = SearchedVersesListRVAdapter(context!!, searchedVerses, myFragmentManager)
                                rvAdapter.setFragmentChangerListener(this@SearchFragment)
                                rvAdapter.setSelectedBibleTextListener(this@SearchFragment)

                                recyclerView.layoutManager = LinearLayoutManager(context)
                                recyclerView.adapter = rvAdapter
                                progressBar.visibility = View.GONE
                            })
                }
                mainHandler.post(myRunnable)
            }
        }
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

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as BibleTextFragment
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_bible, myFragment)
            transaction.commit()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    //Поле для того, чтобы сравнивать какая тема и если тема меняется, то адаптер обновляется
    private var currentTheme = ThemeManager.theme
    override fun onResume() {
        super.onResume()

        //Обновляем адаптер, чтобы при смене темы все айтемы обновились
        if (currentTheme != ThemeManager.theme) {
            rvAdapter.notifyDataSetChanged()
            currentTheme = ThemeManager.theme
        }

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

    override fun setSelectedBibleText(selectedText: String, isBook: Boolean) {
        listener.setTvSelectedBibleText(selectedText, isBook)
    }
}
