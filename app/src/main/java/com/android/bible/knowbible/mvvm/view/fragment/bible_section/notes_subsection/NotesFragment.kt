package com.android.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.NotesDBHelper
import com.android.bible.knowbible.mvvm.model.NoteModel
import com.android.bible.knowbible.mvvm.view.adapter.NotesRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NotesFragment : Fragment(), IThemeChanger, IChangeFragment {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var notesDBHelper: NotesDBHelper
    private lateinit var notesRVAdapter: NotesRVAdapter
    private lateinit var notesList: ArrayList<NoteModel>

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_notes, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        notesDBHelper = NotesDBHelper(context!!)
        notesDBHelper
                .loadNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { listData ->
                    recyclerView = myView.findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            //Если скролл вниз, то FAB скрывается, если вверх, то появляется
                            if (dy > 0) listener.setShowHideAddNoteButtonFAB(View.GONE)
                            else listener.setShowHideAddNoteButtonFAB(View.VISIBLE)
                            super.onScrolled(recyclerView, dx, dy)
                        }
                    })

                    listData.reverse() //Переворачиваем коллекцию, чтобы самые новые заметки были сверху
                    notesList = listData
                    notesRVAdapter = context?.let { NotesRVAdapter(it, notesList) }!!
                    notesRVAdapter.setRecyclerViewThemeChangerListener(this)
                    notesRVAdapter.setFragmentChangerListener(this)
                    recyclerView.adapter = notesRVAdapter
                }

        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onPause() {
        super.onPause()
        listener.setShowHideAddNoteButtonFAB(View.GONE)
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideAddNoteButtonFAB(View.VISIBLE)
        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as AddEditNoteFragment
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//            transaction.setCustomAnimations( R.animator.slide_up, 0, 0, R.animator.slide_down)
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_bible, myFragment)
            transaction.commit()
        }
    }
}
