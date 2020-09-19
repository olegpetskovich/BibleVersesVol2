package com.android.bible.knowbible.mvvm.view.fragment.bible_section.daily_verse_subsection

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.DailyVerseModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.dialog.AddNoteDialog
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.Utility
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.StringReader
import java.util.*
import kotlin.collections.ArrayList

class DailyVerseFragment : Fragment(), DialogListener {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var ivBook: ImageView
    private lateinit var tvVerse: TextView
    private lateinit var btnFind: MaterialButton

    private lateinit var btnAddNote: ImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnCopy: ImageView

    private lateinit var versesInfoList: ArrayList<DailyVerseModel>
    private lateinit var addNoteDialog: AddNoteDialog

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
        val jsonFileContent: String = readJSONFromAsset("verses.txt")!!
        versesInfoList = fromJSON(jsonFileContent)

//        val mainHandler = Handler(context!!.mainLooper)
//        val myRunnable = Runnable {
//            GlobalScope.launch(Dispatchers.Main) {
//                delay(300)
                //ViewModel для получения конкретного текста для Стих дня
                bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!

//                tvVerse.visibility = View.VISIBLE
//                val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
//                tvVerse.startAnimation(animation)
//            }
//        }
//        mainHandler.post(myRunnable)

        btnFind = myView.findViewById(R.id.btnFind)
        btnFind.setOnClickListener {
            val randomDailyVerse = getRandomObject(versesInfoList)
            setRandomVerse(randomDailyVerse)

            val animation = AnimationUtils.loadAnimation(context, R.anim.my_anim)
            tvVerse.startAnimation(animation)
        }

        btnAddNote = myView.findViewById(R.id.btnAddNote)
        btnAddNote.setOnClickListener {
            if (tvVerse.text.toString() == getString(R.string.tv_find_your_daily_verse)) {
                Toast.makeText(context, getString(R.string.toast_find_verse), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addNoteDialog = AddNoteDialog(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) addNoteDialog.setVerse(Html.fromHtml(tvVerse.text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString())
            else addNoteDialog.setVerse(Html.fromHtml(tvVerse.text.toString()).toString())

            addNoteDialog.show(myFragmentManager, "Add Note Dialog") //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
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

        btnCopy = myView.findViewById(R.id.btnCopy)
        btnCopy.setOnClickListener {
            if (tvVerse.text.toString() == getString(R.string.tv_find_your_daily_verse)) {
                Toast.makeText(context, getString(R.string.toast_find_verse), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Код для копирования текста
            val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ClipData.newPlainText("label", Html.fromHtml(tvVerse.text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                    else ClipData.newPlainText("label", Html.fromHtml(tvVerse.text.toString()))

            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, context!!.getString(R.string.verse_copied), Toast.LENGTH_SHORT).show()
        }

        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    @SuppressLint("SetTextI18n")
    private fun setRandomVerse(dailyVerse: DailyVerseModel) {
        //Используем коллекцию для удобного управления данными для формирования стиха дня
        var verses: ArrayList<Int>? = null
        //Если Стих дня состоит только из одного текста Библии, то добавляем в verses номер стиха из поля verse_number
        //Если же Стих дня состоит из двух и более текстов, то добавляем в verses номера стихов из коллекции verses_numbers
        if (dailyVerse.verse_number != -1) {
            verses = ArrayList()
            verses.add(dailyVerse.verse_number)
        } else if (dailyVerse.verses_numbers.isNotEmpty())
            verses = dailyVerse.verses_numbers

        //Именно в таком порядке вызовов всё работает должным образом
        bibleDataViewModel
                .getBibleVerseForDailyVerse(BibleDataViewModel.TABLE_VERSES, dailyVerse.book_number, dailyVerse.chapter_number, verses!!)
                .observe(viewLifecycleOwner, Observer { verseText ->
                    bibleDataViewModel
                            .getBookShortName(BibleDataViewModel.TABLE_BOOKS, verseText!!.book_number)
                            .observe(viewLifecycleOwner, Observer { shortName ->
                                //Формируем ссылку текста нужным образом в случае, если Стих дня состоит из двух и более стихов
                                var textAddress = verseText.chapter_number.toString() + ":" + verseText.verses_numbers[0]
                                for ((index, element) in verseText.verses_numbers.withIndex()) {
                                    if (verseText.verses_numbers.size > 1 && index != 0) {
                                        textAddress += if ((element - verseText.verses_numbers[index - 1]) == 1) {
                                            if (index + 1 != verseText.verses_numbers.size && (verseText.verses_numbers[index + 1] - element) == 1) {
                                                continue
                                            }
                                            "-$element"
                                        } else {
                                            ",$element"
                                        }
                                    }
                                }

                                val clearedStr = Utility.getClearedText(StringBuilder(verseText.verse_text))
                                tvVerse.text = "«$clearedStr» ($shortName. $textAddress)"
                            })
                })
    }

    private fun readJSONFromAsset(name: String?): String? {
        val json: String?
        try {
            val inputStream: InputStream = activity!!.assets.open(name!!)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun fromJSON(json: String?): ArrayList<DailyVerseModel> {
        val gson = Gson()
        Utility.log(json!!)
        return gson.fromJson(StringReader(json), Array<DailyVerseModel>::class.java).toList() as ArrayList<DailyVerseModel>
    }

    private fun getRandomObject(from: ArrayList<DailyVerseModel>): DailyVerseModel {
        val rnd = Random()
        val i = rnd.nextInt(from.size)
        return from.toTypedArray()[i]
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
            ThemeManager.Theme.LIGHT -> ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconLightTheme), PorterDuff.Mode.SRC_IN)
            ThemeManager.Theme.DARK -> ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconDarkTheme), PorterDuff.Mode.SRC_IN)
            ThemeManager.Theme.BOOK -> ivBook.setColorFilter(ContextCompat.getColor(context!!, R.color.colorButtonIconBookTheme), PorterDuff.Mode.SRC_IN)
        }

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

    override fun dismissDialog() {
        addNoteDialog.dismiss()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val dailyVerseFragment = DailyVerseFragment()
            dailyVerseFragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_bible, dailyVerseFragment)
            transaction.commit()
        }
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
