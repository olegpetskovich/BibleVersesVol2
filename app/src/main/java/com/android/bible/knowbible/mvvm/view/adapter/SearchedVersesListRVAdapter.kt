package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.os.Build
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.search_subsection.SearchFragment
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import java.util.*

class SearchedVersesListRVAdapter(internal val context: Context, private val searchedVersesList: ArrayList<BibleTextModel>, private val bibleDataViewModel: BibleDataViewModel, private val viewLifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<SearchedVersesListRVAdapter.MyViewHolder>() {
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
        val view = inflater.inflate(R.layout.item_searched_verse, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dailyVerseModel = searchedVersesList[position]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvDailyVerse.text = Html.fromHtml(dailyVerseModel.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            holder.tvDailyVerse.text = Html.fromHtml(dailyVerseModel.text)
        }
    }

    override fun getItemCount(): Int {
        return searchedVersesList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDailyVerse: TextView = itemView.findViewById(R.id.tvDailyVerse)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        init {
            itemView.setOnClickListener {

            }
            themeChanger.changeItemTheme() //Смена темы для айтемов
        }
    }
}
