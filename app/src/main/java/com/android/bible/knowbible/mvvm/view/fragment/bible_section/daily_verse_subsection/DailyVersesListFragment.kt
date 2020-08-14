package com.android.bible.knowbible.mvvm.view.fragment.bible_section.daily_verse_subsection

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.BibleTextInfoDBHelper
import com.android.bible.knowbible.data.local.DailyVersesDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.DailyVerseModel
import com.android.bible.knowbible.mvvm.view.adapter.DailyVersesListRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DailyVersesListFragment : Fragment(), IThemeChanger, DailyVersesListRVAdapter.UpdateDailyVersesDataListener {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var bibleDataViewModel: BibleDataViewModel
    private lateinit var bibleTextInfoDBHelper: BibleTextInfoDBHelper

    private lateinit var dailyVersesDBHelper: DailyVersesDBHelper

    private lateinit var dailyVersesListInfo: ArrayList<DailyVerseModel>

    private lateinit var rvAdapter: DailyVersesListRVAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dailyVersesList: ArrayList<BibleTextModel>

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_daily_verses_list, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        progressBar = myView.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        //Поскольку загрузка стихов достаточно долгая, делаем задержку с загрузкой, чтобы сначала открылся фрагмент
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            //ViewModel для получения конкретного текста для Стих дня
            bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
            bibleTextInfoDBHelper = BibleTextInfoDBHelper.getInstance(context)!! //DBHelper для работы с БД информации текста

            dailyVersesDBHelper = DailyVersesDBHelper(context!!) //DBHelper для работы с БД информации раздела "Стих дня"

            dailyVersesList = ArrayList()
            if (dailyVersesListInfo.size != 0) {

                dailyVersesListInfo.forEach { element: DailyVerseModel ->
                    bibleDataViewModel
                            .getBibleVerseForDailyVerse(BibleDataViewModel.TABLE_VERSES, element.bookNumber, element.chapterNumber, element.verseNumber)
                            .observe(viewLifecycleOwner, Observer { verseModel ->
                                dailyVersesList.add(verseModel!!)

                                if (dailyVersesList.size == dailyVersesListInfo.size) {
                                    dailyVersesList.reverse()
                                    rvAdapter = DailyVersesListRVAdapter(context!!, dailyVersesList)
                                    rvAdapter.setRecyclerViewThemeChangerListener(this@DailyVersesListFragment) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                                    rvAdapter.setUpdateDailyVersesDataListener(this@DailyVersesListFragment) //Устанавливаем Listener для обновления данных списка и БД после удаления стиха

                                    recyclerView = myView.findViewById(R.id.recyclerView)
                                    recyclerView.layoutManager = LinearLayoutManager(context)
                                    recyclerView.adapter = rvAdapter
                                    progressBar.visibility = View.GONE
                                }
                            })
                }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.tv_empty_daily_verses_list), Toast.LENGTH_SHORT).show()
            }
        }



        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    fun setDailyVersesListInfo(dailyVersesListInfo: ArrayList<DailyVerseModel>) {
        this.dailyVersesListInfo = dailyVersesListInfo
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

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

    override fun deleteDailyVerse(model: BibleTextModel, adapterPosition: Int) {
        dailyVersesDBHelper.deleteDailyVerse(model.book_number, model.chapter_number, model.verse_number)
        dailyVersesList.remove(model)
        rvAdapter.notifyItemRemoved(adapterPosition)
    }
}
