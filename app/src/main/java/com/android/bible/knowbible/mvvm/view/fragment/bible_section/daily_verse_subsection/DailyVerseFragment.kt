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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.BibleTextInfoDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextInfoModel
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.google.android.material.button.MaterialButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class DailyVerseFragment : Fragment() {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var bibleDataViewModel: BibleDataViewModel
    private lateinit var bibleTextInfoDBHelper: BibleTextInfoDBHelper

    private lateinit var dailyVerses: ArrayList<BibleTextModel>

    private lateinit var progressBar: ProgressBar
    private lateinit var ivBook: ImageView
    private lateinit var tvVerse: TextView
    private lateinit var btnFind: TextView

    private lateinit var btnList: MaterialButton
    private lateinit var btnShare: MaterialButton

    private lateinit var dailyVersesInfo: ArrayList<BibleTextInfoModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_daily_verse, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        //ViewModel для получения конкретного текста для Стих дня
        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

        dailyVerses = ArrayList()

        ivBook = myView.findViewById(R.id.ivBook)
        tvVerse = myView.findViewById(R.id.tvVerse)
        progressBar = myView.findViewById(R.id.progressBar)

        //При открытии фрагмента сразу получаем, форматируем и добавляем готовые тексты для "Стих дня" в коллекцию.
        progressBar.visibility = View.VISIBLE
        tvVerse.visibility = View.GONE
        bibleTextInfoDBHelper = BibleTextInfoDBHelper.getInstance(context)!! //DBHelper для работы с БД информации текста
        bibleTextInfoDBHelper
                .loadDailyVersesInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { dailyVersesInfo ->
                    this.dailyVersesInfo = dailyVersesInfo

                    progressBar.visibility = View.GONE
                    tvVerse.visibility = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
                    tvVerse.startAnimation(animation)
                }


        btnFind = myView.findViewById(R.id.btnFind)
        btnFind.setOnClickListener {
            if (dailyVersesInfo.size == 0) {
                Toast.makeText(context!!, getString(R.string.toast_no_verses_in_daily_verse), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setRandomVerse(dailyVersesInfo)
            val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
            tvVerse.startAnimation(animation)
        }

        btnList = myView.findViewById(R.id.btnList)
        btnList.setOnClickListener {
//            val intent = Intent(context, ListActivity::class.java)
//            startActivity(intent)
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
    private fun setRandomVerse(dailyVerses: ArrayList<BibleTextInfoModel>) {
        val random = Random()
        val randomNumber = random.nextInt(dailyVerses.size)
        val dailyVerse: BibleTextInfoModel = dailyVerses[randomNumber]

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
                                    var str = verseModel.text
                                    val reg1 = Regex("""<S>(\d+)</S>""")
                                    val reg2 = Regex("""<f>(\S+)</f>""")
                                    val reg3 = Regex("""<(\w)>|</(\w)>""") //Без удаления пробела

                                    str = str.replace(reg1, "")
                                    str = str.replace(reg2, "")
                                    str = str.replace(reg3, "")
                                    str = str.replace("<pb/>", "")

                                    tvVerse.text = "«" + str + "»" + " (" + verseShortName + ". " + verseModel.chapter + ":" + verseModel.verse + ")"

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
