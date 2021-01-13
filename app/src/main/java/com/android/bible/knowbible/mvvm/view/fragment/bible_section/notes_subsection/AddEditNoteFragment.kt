package com.android.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.NotesDBHelper
import com.android.bible.knowbible.mvvm.model.ChapterModel
import com.android.bible.knowbible.mvvm.model.NoteModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.google.android.material.button.MaterialButton

class AddEditNoteFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var myFragmentManager: FragmentManager
    private lateinit var notesDBHelper: NotesDBHelper

    var isNoteToAdd: Boolean = false //Значение этого поля определяет, пользователь открыл фрагмент для создания заметки, или же для её редактирования
    private var isBtnSaveClicked: Boolean = false

    private var noteData: NoteModel? = null

    private lateinit var tvVerseForNote: TextView
    private lateinit var editTextNote: AppCompatEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var saveLoadData: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_add_note, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)
        notesDBHelper = NotesDBHelper(context!!)

        if (noteData != null && noteData!!.verseText.isNotEmpty()) {
            tvVerseForNote = myView.findViewById(R.id.tvVerseForNote)
            tvVerseForNote.setOnClickListener {
                myFragmentManager.let {
                    val myFragment = BibleTextFragment()
                    myFragment.isBibleTextFragmentOpenedFromAddEditNoteFragment = true
                    myFragment.chapterInfo = ChapterModel(noteData!!.bookNumber, noteData!!.chapterNumber, noteData!!.verseNumber - 1) //Пишем - 1, чтобы проскроллить к нужному айтему
                    myFragment.setRootFragmentManager(myFragmentManager)

                    val transaction: FragmentTransaction = it.beginTransaction()
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.fragment_container_bible, myFragment)
                    transaction.commit()
                }
            }
            tvVerseForNote.visibility = View.VISIBLE
            tvVerseForNote.text = noteData!!.verseText
        }

        editTextNote = myView.findViewById(R.id.editTextNote)
        if (!isNoteToAdd) editTextNote.setText(noteData?.text)

        btnSave = myView.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            if (editTextNote.text.toString().isNotEmpty()) {
                isBtnSaveClicked = true
                saveOrUpdateNote()
                myFragmentManager.popBackStack() //Закрываем фрагмент
            } else Toast.makeText(context, getString(R.string.toast_field_cant_be_empty), Toast.LENGTH_SHORT).show()
        }

        return myView
    }

    private fun saveOrUpdateNote() {
        if (isNoteToAdd) {
            notesDBHelper.addNote(NoteModel(
                    -1/*-1 здесь как заглушка, этот параметр нужен не при добавлении, а при получении данных, потому что там id создаётся автоматически*/,
                    false,
                    -1,
                    -1,
                    -1,
                    "",
                    editTextNote.text.toString()))
            Toast.makeText(context, getString(R.string.toast_note_added), Toast.LENGTH_SHORT).show()
        } else {
            //Обновляем только если текст заметки отличается от ранее написанного
            if (editTextNote.text.toString() != noteData?.text) {
                notesDBHelper.updateNote(NoteModel(
                        noteData!!.id,
                        noteData!!.isNoteForVerse,
                        noteData!!.bookNumber,
                        noteData!!.chapterNumber,
                        noteData!!.verseNumber,
                        noteData!!.verseText,
                        editTextNote.text.toString()))
                Toast.makeText(context, getString(R.string.toast_note_edited), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setNoteData(noteData: NoteModel) {
        this.noteData = noteData
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onStop() {
        super.onStop()
        Utility.log("onStop work")

        //Эта проверка нужна для того, чтобы в случае нажатия кнопки сохранить не сохранять заметку дважды. Если кнопка "Сохранить" нажата, то весь этот вызов срабатывать не будет
        if (!isBtnSaveClicked) {
            if (editTextNote.text.toString().isNotEmpty()) saveOrUpdateNote()
        }
    }

    override fun onPause() {
        super.onPause()
        Utility.log("onPause work")
        listener.setShowHideDeleteNoteButton(View.GONE)
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Если заметка только добавляется, то не отображать иконку удаления, если же открыт для редактирования, то отображать
        if (isNoteToAdd) listener.setShowHideDeleteNoteButton(View.GONE)
        else {
            listener.setShowHideDeleteNoteButton(View.VISIBLE)
            noteData?.id?.let { listener.setNoteIdForDelete(it) } //Присваиваем id заметки для удаления
        }
    }


    //Метод для связи с активити
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notesDBHelper.closeDB()
    }
}
