package com.android.bible.knowbible.mvvm.view.fragment.bible_section.daily_verse_subsection

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.BibleTextInfoDBHelper
import com.android.bible.knowbible.data.local.DailyVersesDBHelper
import com.android.bible.knowbible.mvvm.model.DailyVerseModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.Utility
import com.google.android.material.button.MaterialButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*

class DailyVerseFragment : Fragment() {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var bibleDataViewModel: BibleDataViewModel
    private lateinit var bibleTextInfoDBHelper: BibleTextInfoDBHelper

    private lateinit var dailyVersesDBHelper: DailyVersesDBHelper

    private lateinit var progressBar: ProgressBar
    private lateinit var ivBook: ImageView
    private lateinit var tvVerse: TextView
    private lateinit var btnFind: MaterialButton

    private lateinit var btnList: MaterialButton
    private lateinit var btnShare: MaterialButton

    private lateinit var dailyVersesListInfo: ArrayList<DailyVerseModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_daily_verse, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        ivBook = myView.findViewById(R.id.ivBook)
        tvVerse = myView.findViewById(R.id.tvVerse)
        progressBar = myView.findViewById(R.id.progressBar)

        //При открытии фрагмента сразу получаем, форматируем и добавляем готовые тексты для "Стих дня" в коллекцию.
        tvVerse.visibility = View.GONE

        GlobalScope.launch(Dispatchers.Main) {
            delay(300)
            //ViewModel для получения конкретного текста для Стих дня
            bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

            bibleTextInfoDBHelper = BibleTextInfoDBHelper.getInstance(context)!! //DBHelper для работы с БД информации текста

            dailyVersesDBHelper = DailyVersesDBHelper(context!!)
            dailyVersesDBHelper
                    .loadDailyVersesList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { dailyVersesList ->
                        dailyVersesListInfo = dailyVersesList

                        tvVerse.visibility = View.VISIBLE
                        val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
                        tvVerse.startAnimation(animation)
                    }
        }

        btnFind = myView.findViewById(R.id.btnFind)
        btnFind.setOnClickListener {
            if (dailyVersesListInfo.size == 0) {
                Toast.makeText(context!!, getString(R.string.toast_no_verses_in_daily_verse), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setRandomVerse(dailyVersesListInfo)
            val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
            tvVerse.startAnimation(animation)
        }

        btnList = myView.findViewById(R.id.btnList)
        btnList.setOnClickListener {
            myFragmentManager.let {
                val dailyVersesListFragment = DailyVersesListFragment()
                dailyVersesListFragment.setRootFragmentManager(myFragmentManager)
                dailyVersesListFragment.setDailyVersesListInfo(dailyVersesListInfo)

                val transaction: FragmentTransaction = it.beginTransaction()
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                transaction.addToBackStack(null)
                transaction.replace(R.id.fragment_container_bible, dailyVersesListFragment)
                transaction.commit()
            }
        }

        btnShare = myView.findViewById(R.id.btnShare)
        btnShare.setOnClickListener {
            if (tvVerse.text.toString() == getString(R.string.tv_find_your_daily_verse)) {
                Toast.makeText(context, getString(R.string.toast_find_verse), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            val shareBody = getString(R.string.my_daily_verse) + " \n" + tvVerse.text.toString()
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_verse)))
        }
        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    @SuppressLint("SetTextI18n")
    private fun setRandomVerse(dailyVerses: ArrayList<DailyVerseModel>) {
        val random = Random()
        val randomNumber = random.nextInt(dailyVerses.size)
        val dailyVerse: DailyVerseModel = dailyVerses[randomNumber]

        bibleDataViewModel
                .getBibleVerse(BibleDataViewModel.TABLE_VERSES, dailyVerse.bookNumber, dailyVerse.chapterNumber, dailyVerse.verseNumber)
                .observe(viewLifecycleOwner, Observer { verseModel ->

                    //По непонятной причине метод observe с каждым нажатием кнопки "Стих" начинается срабатывать всё больше раз по количеству, вместо одного раза постоянно.
                    //Поэтому пришлось написать небольшую проверку, которая не будет позволять срабатывать коду в методе больше одного раза
                    var index = 0
                    bibleDataViewModel
                            .getBookShortName(BibleDataViewModel.TABLE_BOOKS, dailyVerse.bookNumber)
                            .observe(viewLifecycleOwner, Observer { verseShortName ->
                                if (index == 0) {
                                    //Очищаем текст от ненужных тегов. Эти действия называются регулярными выражениями
                                    val str = Utility.getClearedText(StringBuilder(verseModel!!.text))

                                    tvVerse.text = "«" + str + "»" + " (" + verseShortName + ". " + verseModel.chapter_number + ":" + verseModel.verse_number + ")"

                                    index++
                                }
                            })
                })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
        //Обновляем тему вьюшек в onResume, чтобы при смене темы и возврата к этому фрагменту, внешний вид вьюшек поменялся в соответствии с темой
        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> {
                ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconLightTheme), PorterDuff.Mode.SRC_IN)

                btnList.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconLightTheme)
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextLightTheme))

                btnShare.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconLightTheme)
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextLightTheme))
            }
            ThemeManager.Theme.DARK -> {
                ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconDarkTheme), PorterDuff.Mode.SRC_IN)

                btnList.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconLightTheme)
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextDarkTheme))

                btnShare.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconLightTheme)
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextDarkTheme))
            }
            ThemeManager.Theme.BOOK -> {
                ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconBookTheme), PorterDuff.Mode.SRC_IN)

                btnList.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconBookTheme)
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextBookTheme))

                btnShare.strokeColor = ContextCompat.getColorStateList(context!!, R.color.colorButtonIconBookTheme)
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextBookTheme))
            }
        }

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }
}
