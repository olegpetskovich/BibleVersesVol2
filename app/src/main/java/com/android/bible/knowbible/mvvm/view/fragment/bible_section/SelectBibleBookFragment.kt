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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.DataToRestoreModel
import com.android.bible.knowbible.mvvm.view.adapter.BooksRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.*
import com.android.bible.knowbible.mvvm.view.dialog.BookInfoDialog
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.google.gson.Gson

class SelectBibleBookFragment : Fragment(), IChangeFragment, IThemeChanger, ISelectBibleText, BooksRVAdapter.BookInfoDialogListener, DialogListener {
    var bookNumber: Int = -1 //Переменная, предназначенная для восстановления стэка фрагментов
    var isOldTestament: Boolean = false //Переменная, предназначенная для определения того, данные какого завета нужно предоставить. true - Ветхий Завет, false - Новый завет. На данный момент присвоено значение по умолчанию - false

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager
    private lateinit var recyclerView: RecyclerView

    private lateinit var bookInfoDialog: BookInfoDialog //Диалог краткого описания книги

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    //Интересный момент - поскольку onCreateView вызывается даже когда он просто выходит на передний план,
    //будучи до этого ниже в стэке, не нужно заботиться о том, чтобы обновить и отобразить новые данные, когда выбирается другой перевод.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        saveLoadData = SaveLoadData(context!!)

//      Получаем данные, которые будут использованы для восстановления стека фрагментов то ли после поворота экрана, то ли в случае, когда человек закрыл приложение из диспетчера задача
//      Делаем мы это специально в самом начале метода, чтобы оно запускалось без пролагов и незаметно для пользователя
        var dataToRestoreModel: DataToRestoreModel? = null
        if (saveLoadData.loadString(BibleTextFragment.DATA_TO_RESTORE) != null && saveLoadData.loadString(BibleTextFragment.DATA_TO_RESTORE)!!.isNotEmpty()) {
            val gson = Gson()
            dataToRestoreModel = gson.fromJson(saveLoadData.loadString(BibleTextFragment.DATA_TO_RESTORE), DataToRestoreModel::class.java)
        }
        if (dataToRestoreModel != null && bookNumber != -1 && dataToRestoreModel.chapterNumber != -1) {

            myFragmentManager.let {
                val selectBookChapterFragment = SelectBookChapterFragment()
                selectBookChapterFragment.setRootFragmentManager(myFragmentManager)
                selectBookChapterFragment.bookNumber = bookNumber
                selectBookChapterFragment.chapterNumber = dataToRestoreModel.chapterNumber

                val transaction: FragmentTransaction = it.beginTransaction()
                transaction.addToBackStack(null)
                transaction.replace(R.id.fragment_container_bible, selectBookChapterFragment)
                transaction.commit()

                bookNumber = -1 //Устанавливаем значение -1, чтобы при попытке вернуться на прежний фрагмент, пользователя снова не перебрасывало на уже открытый
            }
        }

        val myView: View = inflater.inflate(R.layout.fragment_select_bible_info, container, false)

        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        recyclerView = myView.findViewById(R.id.recyclerView)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        val progressBar: ProgressBar = myView.findViewById(R.id.downloadProgressBar)
        progressBar.visibility = View.VISIBLE

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()

        var rvAdapter: BooksRVAdapter
        bibleDataViewModel
                .getTestamentBooksList(BibleDataViewModel.TABLE_BOOKS, isOldTestament)
                .observe(viewLifecycleOwner, Observer { list ->

                    rvAdapter = BooksRVAdapter(list)
                    rvAdapter.setFragmentChangerListener(this@SelectBibleBookFragment)
                    rvAdapter.setRecyclerViewThemeChangerListener(this@SelectBibleBookFragment) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                    rvAdapter.setSelectedBibleTextListener(this@SelectBibleBookFragment)
                    rvAdapter.setBookInfoDialogListener(this@SelectBibleBookFragment)

                    recyclerView.adapter = rvAdapter
                    progressBar.visibility = View.GONE
                })
        return myView
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as SelectBookChapterFragment
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

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Убираем текст выбранной главы Библии
        listener.setTvSelectedBibleText("", false)
    }

    override fun setSelectedBibleText(selectedText: String, isBook: Boolean) {
        listener.setTvSelectedBibleText(selectedText, isBook)
    }

    override fun createInfoDialog() {
        bookInfoDialog = BookInfoDialog(this)
        bookInfoDialog.isCancelable = true
        bookInfoDialog.show(childFragmentManager, "Book Info Dialog") //Тут должен быть именно childFragmentManager
    }

    override fun dismissDialog() {
        bookInfoDialog.dismiss()
    }
}