package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.NoteModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection.AddEditNoteFragment
import java.util.*


class NotesRVAdapter(internal var context: Context, private var notesList: ArrayList<NoteModel>) : RecyclerView.Adapter<NotesRVAdapter.MyViewHolder>() {
    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_note, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val verse = notesList[position]

        if (verse.isNoteForVerse) {
            holder.ivNoteForVerse.visibility = View.VISIBLE
            holder.ivNote.visibility = View.GONE
        } else {
            holder.ivNote.visibility = View.VISIBLE
            holder.ivNoteForVerse.visibility = View.GONE
        }
        holder.tvNote.text = verse.text
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivNoteForVerse: ImageView = itemView.findViewById(R.id.ivNoteForVerse)
        var ivNote: ImageView = itemView.findViewById(R.id.ivNote)
        var tvNote: TextView = itemView.findViewById(R.id.tvNote)

        init {
            itemView.setOnClickListener {
                val addEditNoteFragment = AddEditNoteFragment()
                addEditNoteFragment.isNoteToAdd = false
                addEditNoteFragment.setNoteData(notesList[adapterPosition])
                fragmentChanger.changeFragment(addEditNoteFragment)
            }
            themeChanger.changeItemTheme() //Смена темы для айтемов
        }
    }
}
