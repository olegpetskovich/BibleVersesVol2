package com.android.bible.knowbible.mvvm.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BookModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.ISelectBibleText
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.dialog.BibleTranslationDialog
import com.android.bible.knowbible.mvvm.view.dialog.BookInfoDialog
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.SelectBookChapterFragment


class BooksRVAdapter(private val models: ArrayList<BookModel>) : RecyclerView.Adapter<BooksRVAdapter.MyViewHolder>() {
    interface BookInfoDialogListener { fun createInfoDialog() }
    private lateinit var bookInfoDialogListener: BookInfoDialogListener
    fun setBookInfoDialogListener(bookInfoDialogListener: BookInfoDialogListener) {
        this.bookInfoDialogListener = bookInfoDialogListener
    }

    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    private lateinit var selectBibleTextListener: ISelectBibleText
    fun setSelectedBibleTextListener(selectBibleTextListener: ISelectBibleText) {
        this.selectBibleTextListener = selectBibleTextListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_book, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvBookName.text = models[position].long_name
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookName: TextView = itemView.findViewById(R.id.tvBookName)
        val btnBookInfo: ImageView = itemView.findViewById(R.id.btnBookInfo)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            btnBookInfo.setOnClickListener {
                bookInfoDialogListener.createInfoDialog() //Потом когда займусь полностью этим на вход в метод отправлять текст описания книги
            }

            itemView.setOnClickListener {
                selectBibleTextListener.setSelectedBibleText(models[adapterPosition].short_name + ".", true)

                val selectBookChapterFragment = SelectBookChapterFragment()
                selectBookChapterFragment.bookNumber = models[adapterPosition].book_number
                fragmentChanger.changeFragment(selectBookChapterFragment)
            }
        }
    }
}