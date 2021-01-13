package com.android.bible.knowbible.mvvm.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.BibleTextInfoDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextInfoModel
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment

class VerseDialog(private val listener: VerseDialogListener) : AppCompatDialogFragment(), ColorPickerDialog.ColorPickerDialogListener2, DialogListener {
    interface VerseDialogListener {
        fun dismissDialog()
        fun updateItemColor(bibleTextInfo: BibleTextInfoModel)
    }

    private lateinit var bibleTextInfoDBHelper: BibleTextInfoDBHelper
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var verseData: BibleTextModel
    private lateinit var verseShortName: String

//    private lateinit var dailyVersesDBHelper: DailyVersesDBHelper

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var colorPickerDialog: ColorPickerDialog
    private lateinit var addVerseNoteDialog: AddVerseNoteDialog

    private lateinit var tvVerse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_verse, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(context!!).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        bibleTextInfoDBHelper = BibleTextInfoDBHelper.getInstance(context)!! //DBHelper для работы с БД информации текста

        //Вынужденно приходится подключаться здесь к ViewModel, чтобы получить short_name книги для того, чтобы можно было скопировать текст с ссылкой на него в Библии.
        //Приходится так подключаться по причине того, что в БД текстов Библии нет поля short_name в таблице текстов. Это является на данный момент самой оптимальной альтернативой
        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
        bibleDataViewModel
                .getBookShortName(BibleDataViewModel.TABLE_BOOKS, verseData.book_number)
                .observe(this, Observer { shortName ->
                    verseShortName = shortName

                    //Устаналиваем текст здесь, потому что нужно дождаться получения verseShortName, который приходит в отдельном потоке и потом возвращается в главный.
                    //В противном случае возникнет NPE
                    tvVerse = view.findViewById(R.id.tvVerse)
                    tvVerse.text = "«" + verseData.text + "»" + " (" + verseShortName + ". " + verseData.chapter_number + ":" + verseData.verse_number + ")"
                    //Устанавливаем текст выделения, если этот текст был выделен ранее
                    if (ThemeManager.theme == ThemeManager.Theme.DARK) {
                        tvVerse.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDarkTheme))
                    } else {
                        tvVerse.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextLightTheme))
                    }
                    if (verseData.textColorHex != null) {
                        tvVerse.setTextColor(Color.parseColor(verseData.textColorHex))
                    }

                    if (verseData.isTextBold) {
                        //Устанавливаем и отключаем жирный шрифт именно таким образом. Установка через параметр Typeface не подходит
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            tvVerse.setTextAppearance(context, R.style.TextViewStyleBold)
                        } else {
                            tvVerse.setTextAppearance(R.style.TextViewStyleBold)
                        }
                    }
                    if (verseData.isTextUnderline) tvVerse.paintFlags = tvVerse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                })

        val tvCopy: TextView = view.findViewById(R.id.tvCopy)
        tvCopy.setOnClickListener {
            //Код для копирования текста
            val clipboard = activity!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", "«" + verseData.text + "»" + " (" + verseShortName + ". " + verseData.chapter_number + ":" + verseData.verse_number + ")")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, getString(R.string.verse_copied), Toast.LENGTH_SHORT).show()

            listener.dismissDialog()
        }

        //Функцию выделения сделана коллбеком как метод dismissDialog. Создана БД, где храняться номер книги, главы и стиха,
        //чтобы при последующем отображении стиха в разных языках, выделять его основываясь именно на номере книги, главы и стиха
        val tvHighlight: TextView = view.findViewById(R.id.tvHighlight)
        tvHighlight.setOnClickListener {
//            colorPickerDialog = ColorPickerDialog(this)
//            colorPickerDialog.setVersesData(verseData)
//            colorPickerDialog.show(myFragmentManager, "Color Picker Dialog") //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
        }

        val tvRemoveHighlighting: TextView = view.findViewById(R.id.tvRemoveHighlighting)
        //В зависимости от того, есть ли выделение на тексте, показываем или скрываем кнопку "Убрать выделение"
        if (verseData.textColorHex != null) tvRemoveHighlighting.visibility = View.VISIBLE
        else tvRemoveHighlighting.visibility = View.GONE
        tvRemoveHighlighting.setOnClickListener {
            val verseForRemoveHighlighting = BibleTextInfoModel(verseData.id, verseData.book_number, verseData.chapter_number, verseData.verse_number, null, isTextBold = false, isTextUnderline = false)
            bibleTextInfoDBHelper.deleteBibleTextInfo(verseForRemoveHighlighting.id)

            verseForRemoveHighlighting.id = -1 //Обновляем также и данные в коллекции, чтобы не было проблем при очередной попытке добавить, обновить или удалить текст
            listener.updateItemColor(verseForRemoveHighlighting)
            listener.dismissDialog()
        }

        //Функция добавления и удаления стиха в Стих Дня не будет добавлена в рабочий вариант приложения, потому как сочтена не совсем удобной и подходящей для пользователя
        //Список стихов дня будет добавляться разработчиком и предоставляться пользователю как готовый вариант.
        //Чтобы пользователь, во-первых, не "игрался" с добавлением текстов в стих дня,
        //и, во-вторых, чтобы пользователю было интереснее от того, что ему буду попадаться те тексты Библии, которые им заведомо не были добавлены,
        //то есть потенциально он не будет знать о всём списке текстом и от этого интерес к функции Стих дня будет больше
        //Но пока код будет просто закомментирован, вдруг пригодится

//        val tvAddToDailyVerse: TextView = view.findViewById(R.id.tvAddToDailyVerse)
//        tvAddToDailyVerse.setOnClickListener {
//            dailyVersesDBHelper.addDailyVerse(DailyVerseModel(-1/*Тут это заглушка*/, verseData.book_number, verseData.chapter_number, verseData.verse_number))
//            listener.dismissDialog()
//        }
//
//        val tvRemoveFromDailyVerse: TextView = view.findViewById(R.id.tvRemoveFromDailyVerse)
//        tvRemoveFromDailyVerse.setOnClickListener {
//            dailyVersesDBHelper.deleteDailyVerse(verseData.book_number, verseData.chapter_number, verseData.verse_number)
//            listener.dismissDialog()
//        }
//        dailyVersesDBHelper = DailyVersesDBHelper(context!!) //DBHelper для работы с БД информации раздела "Стих дня"
//        if (dailyVersesDBHelper.isBibleTextInfoInDB(verseData.book_number, verseData.chapter_number, verseData.verse_number)) {
//            tvAddToDailyVerse.visibility = View.GONE
//            tvRemoveFromDailyVerse.visibility = View.VISIBLE
//        } else {
//            tvAddToDailyVerse.visibility = View.VISIBLE
//            tvRemoveFromDailyVerse.visibility = View.GONE
//        }


        val tvAddToNotes: TextView = view.findViewById(R.id.tvAddToNotes)
        tvAddToNotes.setOnClickListener {
//            addVerseNoteDialog = AddVerseNoteDialog(this)
//            addVerseNoteDialog.setVerse(multiSelectedTextsList[0], tvVerse.text.toString())
//            addVerseNoteDialog.show(myFragmentManager, "Add Note Dialog") //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
        }

        val tvShare: TextView = view.findViewById(R.id.tvShare)
        tvShare.setOnClickListener {
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            val shareBody = tvVerse.text.toString()
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_verse)))

            listener.dismissDialog()
        }

        builder.setView(view)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
    }

    fun setVerseData(verseData: BibleTextModel) {
        this.verseData = verseData
    }

    fun setFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun dismissDialog(isColorSelected: Boolean) {
        if (isColorSelected) {
            colorPickerDialog.dismiss()
            listener.dismissDialog()
        } else colorPickerDialog.dismiss()
    }

    override fun updateItemColor(bibleTextInfo: BibleTextInfoModel) {
        listener.updateItemColor(bibleTextInfo)
    }

    override fun dismissDialog() {
        addVerseNoteDialog.dismiss()
        listener.dismissDialog()
    }

    //Метод для связи с активити
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = try {
//            context as LanguageDialogListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(context.toString() + "must implement LanguageDialogListener")
//        }
//    }
}
