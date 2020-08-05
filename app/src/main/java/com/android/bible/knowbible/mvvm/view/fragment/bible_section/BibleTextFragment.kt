package com.android.bible.knowbible.mvvm.view.fragment.bible_section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ChapterModel
import com.android.bible.knowbible.mvvm.model.DataToRestoreModel
import com.android.bible.knowbible.mvvm.view.activity.MainActivity.Companion.isBackButtonClicked
import com.android.bible.knowbible.mvvm.view.adapter.ViewPager2Adapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Field

class BibleTextFragment : Fragment(), IThemeChanger, ViewPager2Adapter.IFragmentCommunication {
    companion object {
        const val DATA_TO_RESTORE = "DATA_TO_RESTORE"
    }

    private lateinit var swipeListener: OnViewPagerSwipeStateListener
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var saveLoadData: SaveLoadData
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var progressBar: ProgressBar

    private lateinit var myFragmentManager: FragmentManager

    private var vpAdapter: ViewPager2Adapter? = null
    private lateinit var viewPager2: ViewPager2

    var chapterInfo: ChapterModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_bible_text, container, false)
        Utility.log("BibleTextFragment: onCreateView")
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
//        bibleDataViewModel.setDatabase() //Вызов этого метода не нужен скорее всего

        progressBar = myView.findViewById(R.id.downloadProgressBar)

        viewPager2 = myView.findViewById(R.id.viewPager2)
//        viewPager2.offscreenPageLimit = 10 //Потестировать этот метод, чтобы не было мелкого пролага при перелистывании

        bibleDataViewModel
                .getBookShortName(BibleDataViewModel.TABLE_BOOKS, chapterInfo?.bookNumber!!)
                .observe(viewLifecycleOwner, Observer { shortName ->
                    listener.setTvSelectedBibleText("$shortName.", true)
                })

        reduceDragSensitivity(viewPager2)

        return myView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnViewPagerSwipeStateListener) swipeListener = context
        else throw RuntimeException("$context must implement OnViewPagerSwipeStateListener")
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    //Поле для того, чтобы сравнивать какая тема и если тема меняется, то адаптер обновляется
    private var currentTheme = ThemeManager.theme
    override fun onResume() {
        super.onResume()
        Utility.log("BibleTextFragment: onResume")
        //Включаем свайп для листания глав Библии, отключая при этом свайп для навигации между табами
        swipeListener.setViewPagerSwipeState(false)

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        //Оповещаем Activity, что BibleTextFragment открыт и видимости пользователя, чтобы,

        //Аннулируем здесь значение переменной isBackButtonClicked, потому что оно может установиться на true при нажатии кнопки "Назад" в других фрагментах
        //и при закрытии приложения через диспетчер задач данные сохраненного скролла всё равно будут стёрты,
        //потому что вызовется onPause, совершится проверка на то, была ли нажата кнопка назад и если там будет true, то данные скролла очистятся.
        //Ориентируясь на значение этой переменной, нужно очищать данные скролла только тогда,
        //когда кнопка "Назад" была нажата именно в открытом BibleTextFragment в табе "Библия", а не в каком-то другом табе(по этой причине здесь и присваивается ей значение true)
        //Потому что пользователь может нажать кнопку "Назад" в табе "Статьи", а данные сохранённого скролла Библии всё равно очистаться,
        //хотя пользователь этого естественно не хотел. Также, очищать данные сохранённого скролла нужно в Activity в методе onBackPressed,
        //потому что даже если данные будут очищаться в onDestroy BibleTextFragment, а кнопка "Назад" будет нажата, опять же, в другом табе "Статьи",
        //то onDestroy BibleTextFragment(а) всё равно сработает, и данные сохранённого скролла всё равно очистяться, без желания пользователя.
        //Надеюсь, логика понятна... Вибачайте, якшо по молдавському
        isBackButtonClicked = false

        if (vpAdapter != null && viewPager2.adapter != null && viewPager2.adapter!!.itemCount != 0) {
            vpAdapter!!.dataToRestoreData = DataToRestoreModel(chapterInfo?.bookNumber!!, chapterInfo?.chapterNumber!! - 1) //Поскольку счёт в коллекции начинается с 0, а главы начинаются с 1, то нужно номер главы минусовать. Также тут отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее
//            viewPager2.setCurrentItem(page, false) //Отключаем анимацию слайда именно при выборе главы, чтобы она сразу включалась без анимации,
            //потому что с анимацией в таком случае глава открывается будто бы с пролагом.
            //Анимируется только когда пользователь делает слайд влево или вправо, чтобы листать главы

            val page = chapterInfo?.chapterNumber!! - 1
            //Обновляем адаптер, чтобы при смене темы все айтемы обновились и устаналивалась нужная позиция скролла
            if (currentTheme != ThemeManager.theme) {
                viewPager2.adapter!!.notifyDataSetChanged()
                currentTheme = ThemeManager.theme
                setScroll(page, false)
            } else setScroll(page, false)
        } else {
//            bibleDataViewModel
//                    .getBibleTextOfAllBible(BibleDataViewModel.TABLE_VERSES)
//                    .observe(viewLifecycleOwner, Observer { allBibleTexts ->
            //Код для преобразования тексто в БД НИ В КОЕМ СЛУЧАЕ НЕ УДАЛЯТЬ!
//                        val mainHandler = Handler(context!!.mainLooper)
//                        val myRunnable = Runnable {
//                            progressBar.visibility = View.VISIBLE
//
//                            allBibleTexts?.forEachIndexed { indexBible, text ->
//                                bibleDataViewModel.updateBibleTextInDB(text)
//                                Utility.log("Verse of book: " + text.book_number + ", " + text.chapter_number + ":" + text.verse_number + " is changed")
//                                if (indexBible == allBibleTexts.size - 1) {
//                                    progressBar.visibility = View.GONE
//                                    Utility.log("Verses changing of ALL BIBLE in DB is finished!")
//                                }
//                            }
//                        }
//                        mainHandler.post(myRunnable)

            bibleDataViewModel
                    .getBibleTextOfBook(BibleDataViewModel.TABLE_VERSES, chapterInfo?.bookNumber!!)
                    .observe(viewLifecycleOwner, Observer { bookTexts ->


                        vpAdapter = ViewPager2Adapter(context!!, bookTexts, myFragmentManager)
                        vpAdapter!!.setRecyclerViewThemeChangerListener(this)
                        vpAdapter!!.setIFragmentCommunicationListener(this)

                        vpAdapter!!.dataToRestoreData = DataToRestoreModel(chapterInfo?.bookNumber!!, chapterInfo?.chapterNumber!! - 1) //Поскольку счёт в коллекции начинается с 0, а главы начинаются с 1, то нужно номер главы минусовать. Также тут отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее

                        viewPager2.adapter = vpAdapter
                        val page = chapterInfo?.chapterNumber!! - 1
                        viewPager2.setCurrentItem(page, false) //Отключаем анимацию слайда именно при выборе главы, чтобы она сразу включалась без анимации,
                        //потому что с анимацией в таком случае глава открывается будто бы с пролагом.
                        //Анимируется только когда пользователь делает слайд влево или вправо, чтобы листать главы

                        setScroll(page, false)
                    })
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                chapterInfo?.chapterNumber = position //Сохраняем выбранную страницу, чтобы сохранить её в случае закрытия приложения
                vpAdapter!!.dataToRestoreData = DataToRestoreModel(chapterInfo?.bookNumber!!, chapterInfo?.chapterNumber!!) //Отправляем данные, чтобы сравнить и выяснить, тот ли отображён текст, данные скролла которого были сохранены ранее
                Utility.log("BibleTextFragment: onPageSelected")

                val chapterNumber = position + 1
                chapterInfo!!.chapterNumber = chapterNumber
                listener.setTvSelectedBibleText(chapterNumber.toString(), false)
            }
        })

        listener.setBtnSelectTranslationVisibility(View.VISIBLE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.VISIBLE)
    }

    private val dataToRestore: DataToRestoreModel = DataToRestoreModel(-1, -1, -1)
    override fun onPause() {
        super.onPause()
        Utility.log("BibleTextFragment: onPause")
        //Включаем свайп для навигации между табами, отключая при этом свайп для листания глав Библии
        swipeListener.setViewPagerSwipeState(true)

        val jsonScrollData = saveLoadData.loadString(DATA_TO_RESTORE)

        val dataToRestoreJson =
                //Если кнопка "Назад" нажата, то очищаем сохранённые данные скролла
                if (isBackButtonClicked) {
                    //Очищаем сохранённые данные скролла, чтобы они не восстанавливались после того, как пользователь сам закрыл страницу текста кнопкой "Назад".
                    //Данные сохраняются только в том случае, когда человек вышел из приложения, будучи до этого в фрагменте текста Библии,
                    //чтобы потом, когда он войдёт снова, ему отобразились данные на том скролле, на котором они были до выхода приложения.
                    Gson().toJson(DataToRestoreModel(
                            -1,
                            -1,
                            -1))
                } else if (dataToRestore.bookNumber != -1 &&
                        dataToRestore.chapterNumber != -1 &&
                        dataToRestore.scrollPosition != -1) {
                    Gson().toJson(DataToRestoreModel(
                            dataToRestore.bookNumber,
                            dataToRestore.chapterNumber + 1, //Возвращаем исходный номер главы, который при восстановлении стэка снова отминусуется, чтобы установить нужную главу
                            dataToRestore.scrollPosition))
                }
                //Если пользователь ничего не скроллил, но экран открыт уже на какой-то ранее сохранённое позиции,
                //и пользователь захотел сразу перейти куда-то в другой фрагмент(например BibleTranslationsFragment),
                //то сохраняем ныне восстановленные данные скролла, чтобы отобразить при возврате на этот фрагмент
                else if (jsonScrollData != null && jsonScrollData.isNotEmpty() && Gson().fromJson(jsonScrollData, DataToRestoreModel::class.java).scrollPosition != -1) {
                    Gson().toJson(DataToRestoreModel(
                            chapterInfo!!.bookNumber,
                            chapterInfo!!.chapterNumber, //Если данные скролла не меняются и всё остаётся без изменений, то увеличивать номер главы на 1 не нужно, потому что всё уже как надо.
                            vpAdapter!!.getScrollPosition(chapterInfo!!.chapterNumber - 1))) //Получаем нынешнюю позицию скролла
                }
                //Если пользователь ничего не скроллил, то просто сохраняем выбранную книгу и главу, чтобы при повороте отобразить
                else {
                    Gson().toJson(DataToRestoreModel(
                            chapterInfo!!.bookNumber,
                            chapterInfo!!.chapterNumber, //Если данные скролла не меняются и всё остаётся без изменений, то увеличивать номер главы на 1 не нужно, потому что всё уже как надо.
                            0))
                }
        saveLoadData.saveString(DATA_TO_RESTORE, dataToRestoreJson)  //Сохранять данные тогда, когда фрагмент выходит из видимости
    }

    override fun onStop() {
        super.onStop()
        Utility.log("BibleTextFragment: onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utility.log("BibleTextFragment: onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utility.log("BibleTextFragment: onDestroy")

    }

    override fun onDetach() {
        super.onDetach()
        Utility.log("BibleTextFragment: onDetach")
    }

    //Задержка нужна для того, чтобы скролл устанавливался после того, как сработает onBindViewHolder, чтобы onBindViewHolder не сбросил установленный скролл
    private fun setScroll(page: Int, smoothScroll: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(50)
            vpAdapter!!.scrollTo(page, smoothScroll)
        }
    }

    //Уменьшаем чувствительность сенсора к свайпам. Вообще непонятный код)) , нашёл тут, в комманетариях под постом, потому что пост непонятный https://medium.com/@al.e.shevelev/how-to-reduce-scroll-sensitivity-of-viewpager2-widget-87797ad02414
    private fun reduceDragSensitivity(viewPager2: ViewPager2) {
        try {
            val ff: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            ff.isAccessible = true
            val recyclerView = ff.get(viewPager2) as RecyclerView
            val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int
            touchSlopField.set(recyclerView, touchSlop * 2)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    interface OnViewPagerSwipeStateListener {
        fun setViewPagerSwipeState(viewPagerSwipeState: Boolean)
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение
    }

    override fun saveScrollPosition(bookNumber: Int, chapterNumber: Int, scrollPosition: Int) {
        dataToRestore.bookNumber = bookNumber
        dataToRestore.chapterNumber = chapterNumber
        dataToRestore.scrollPosition = scrollPosition
    }
}
