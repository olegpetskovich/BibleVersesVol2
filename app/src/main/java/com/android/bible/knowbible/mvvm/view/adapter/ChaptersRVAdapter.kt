package com.android.bible.knowbible.mvvm.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ChapterModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment

class ChaptersRVAdapter(private val models: ArrayList<ChapterModel>) : RecyclerView.Adapter<ChaptersRVAdapter.MyViewHolder>() {
    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) { this.fragmentChanger = fragmentChanger }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) { this.themeChanger = themeChanger }
    
    private lateinit var selectBibleTextListener: ISelectBibleText
    fun setSelectedBibleTextListener(selectBibleTextListener: ISelectBibleText) { this.selectBibleTextListener = selectBibleTextListener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_chapter, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvBookName.text = models[position].chapterNumber.toString()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookName: TextView = itemView.findViewById(R.id.tvBookName)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            itemView.setOnClickListener {
                selectBibleTextListener.setSelectedBibleText(models[adapterPosition].chapterNumber.toString(), false)

                val bibleTextFragment = BibleTextFragment()
                bibleTextFragment.chapterInfo = models[adapterPosition]
                fragmentChanger.changeFragment(bibleTextFragment)
            }
        }
    }
}