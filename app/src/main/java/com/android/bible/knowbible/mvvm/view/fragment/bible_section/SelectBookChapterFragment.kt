package com.android.bible.knowbible.mvvm.view.fragment.bible_section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ChapterModel
import com.android.bible.knowbible.mvvm.model.EnumBooksList
import com.android.bible.knowbible.mvvm.view.adapter.ChaptersRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData

class SelectBookChapterFragment : Fragment(), IChangeFragment, IThemeChanger, ISelectBibleText {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var myFragmentManager: FragmentManager

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var bibleDataViewModel: BibleDataViewModel

    var chapterNumber: Int = -1 //Переменная, предназначенная для восстановления стэка фрагментов
    var bookNumber: Int = -1

    lateinit var chaptersList: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (bookNumber != -1 && chapterNumber != -1) {
            myFragmentManager.let {
                listener.setTvSelectedBibleText(chapterNumber.toString(), false) //Устанавливаем номер главы при восстановлении стэка
                val bibleTextFragment = BibleTextFragment()
                bibleTextFragment.setRootFragmentManager(myFragmentManager)
                bibleTextFragment.chapterInfo = ChapterModel(bookNumber, chapterNumber)

                val transaction: FragmentTransaction = it.beginTransaction()
                transaction.addToBackStack(null)
                transaction.replace(R.id.fragment_container_bible, bibleTextFragment)
                transaction.commit()

                //В случае восстановления стэка фрагментов данные списка глав не инициализируются, потому что тот код не срабатывает при восстановлении стэка.
                //Поэтому список глав формируем таким образом:
                chaptersList = ArrayList()
                for (book in EnumBooksList.values())
                    if (bookNumber == book.bookNumber) {
                        var x = 0
                        while (chaptersList.size < book.numberOfChapters) {
                            chaptersList.add(x + 1)
                            x++
                        }
                        break
                    }

                chapterNumber = -1 //Устанавливаем значение -1, чтобы при попытке вернуться на прежний фрагмент, пользователя снова не перебрасывало на уже открытый
            }
        }

        val myView: View = inflater.inflate(R.layout.fragment_select_bible_info, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)

        val recyclerView: RecyclerView = myView.findViewById(R.id.recyclerView)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
//        bibleDataViewModel.setDatabase() //Вызов этого метода не нужен скорее всего

        val progressBar: ProgressBar = myView.findViewById(R.id.downloadProgressBar)
        progressBar.visibility = View.VISIBLE

//        bibleDataViewModel
//                .getChaptersList(BibleDataViewModel.TABLE_VERSES, bookNumber)
//                .observe(viewLifecycleOwner, Observer { list ->
//                    val rvAdapter = ChaptersRVAdapter(list)
//                    rvAdapter.setFragmentChangerListener(this)
//                    rvAdapter.setRecyclerViewThemeChangerListener(this) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
//                    rvAdapter.setSelectedBibleTextListener(this)
//
//                    recyclerView.adapter = rvAdapter
//
//                    bibleDataViewModel
//                            .getBookShortName(BibleDataViewModel.TABLE_BOOKS, bookNumber)
//                            .observe(viewLifecycleOwner, Observer { shortName ->
//                                listener.setTvSelectedBibleText("$shortName.", true)
//                            })
//                })

        val rvAdapter = ChaptersRVAdapter(chaptersList)
        rvAdapter.setFragmentChangerListener(this)
        rvAdapter.setRecyclerViewThemeChangerListener(this) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
        rvAdapter.setSelectedBibleTextListener(this)

        recyclerView.adapter = rvAdapter

        bibleDataViewModel
                .getBookShortName(BibleDataViewModel.TABLE_BOOKS, bookNumber)
                .observe(viewLifecycleOwner, Observer { shortName ->
                    listener.setTvSelectedBibleText("$shortName.", true)
                })

        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.itemAnimator = DefaultItemAnimator()

        progressBar.visibility = View.GONE
        return myView
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as BibleTextFragment
            //Указываем, что фрагмент не был открыт ни с фрагмента AddEditNoteFragment, ни с фрагмента SearchFragment
//            myFragment.isBibleTextFragmentOpenedFromAddEditNoteFragment = false
//            myFragment.isBibleTextFragmentOpenedFromSearchFragment = false
            myFragment.chapterInfo!!.bookNumber = bookNumber
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_bible, myFragment)
            transaction.commit()
        }
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
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

        listener.setBtnSelectTranslationVisibility(View.VISIBLE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.VISIBLE)
    }

    override fun setSelectedBibleText(selectedText: String, isBook: Boolean) {
        listener.setTvSelectedBibleText(selectedText, isBook)
    }
}
