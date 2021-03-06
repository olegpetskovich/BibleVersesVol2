package com.android.bible.knowbible.mvvm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.NotesDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.NoteModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.google.android.material.button.MaterialButton

class AddVerseNoteDialog(private val listener: DialogListener) : AppCompatDialogFragment() {
    private lateinit var notesDBHelper: NotesDBHelper

    private lateinit var verseModel: BibleTextModel
    private lateinit var verseText: String

    private lateinit var tvVerseForNote: TextView
    private lateinit var mainLayout: LinearLayout
    private lateinit var editTextNote: AppCompatEditText
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_add_verse_note, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(context!!).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }
        notesDBHelper = NotesDBHelper(context!!)

        tvVerseForNote = view.findViewById(R.id.tvVerseForNote)
        tvVerseForNote.text = verseText

        mainLayout = view.findViewById(R.id.mainLayout)

        editTextNote = view.findViewById(R.id.editTextNote)
//        editTextNote.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                mainLayout.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tvVerseForNote.height + editTextNote.height + btnSave.height)
//                mainLayout.requestLayout()
//            }
//        })

        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            if (editTextNote.text.toString().isNotEmpty()) {
                notesDBHelper.addNote(NoteModel(
                        -1/*-1 здесь как заглушка, этот параметр нужен не при добавлении, а при получении данных, потому что там id создаётся автоматически*/,
                        true,
                        verseModel.book_number,
                        verseModel.chapter_number,
                        verseModel.verse_number,
                        verseText,
                        editTextNote.text.toString()))
                Toast.makeText(context, getString(R.string.toast_note_added), Toast.LENGTH_SHORT).show()
                listener.dismissDialog()
            } else Toast.makeText(context, getString(R.string.toast_field_cant_be_empty), Toast.LENGTH_SHORT).show()
        }

//        mainLayout.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tvVerseForNote.height + editTextNote.height + btnSave.height)
//        mainLayout.requestLayout()

        builder.setView(view)
        return builder.create()
    }

    fun setVerse(verseModel: BibleTextModel, verseText: String) {
        this.verseModel = verseModel
        this.verseText = verseText
    }

    override fun onResume() {
        super.onResume()
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notesDBHelper.closeDB()
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
