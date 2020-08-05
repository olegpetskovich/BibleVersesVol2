package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.google.android.material.button.MaterialButton
import java.util.*

class DailyVersesListRVAdapter(internal var context: Context, private var dailyVersesList: ArrayList<BibleTextModel>) : RecyclerView.Adapter<DailyVersesListRVAdapter.MyViewHolder>() {
    interface UpdateDailyVersesDataListener {
        fun deleteDailyVerse(model: BibleTextModel, adapterPosition: Int)
    }

    private lateinit var dailyVersesUpdateData: UpdateDailyVersesDataListener
    fun setUpdateDailyVersesDataListener(dailyVersesUpdateData: UpdateDailyVersesDataListener) {
        this.dailyVersesUpdateData = dailyVersesUpdateData
    }

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
        val view = inflater.inflate(R.layout.item_daily_verse, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dailyVerseModel = dailyVersesList[position]

        if (ThemeManager.theme == ThemeManager.Theme.BOOK) {
            holder.btnDeleteDailyVerse.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.btnDeleteDailyVerse.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }

        holder.tvDailyVerse.text = dailyVerseModel.text
    }

    override fun getItemCount(): Int {
        return dailyVersesList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDailyVerse: TextView = itemView.findViewById(R.id.tvDailyVerse)
        var btnDeleteDailyVerse: MaterialButton = itemView.findViewById(R.id.btnDeleteDailyVerse)

        init {
            btnDeleteDailyVerse.setOnClickListener {
                dailyVersesUpdateData.deleteDailyVerse(dailyVersesList[adapterPosition], adapterPosition)
            }
            themeChanger.changeItemTheme() //Смена темы для айтемов
        }
    }
}
