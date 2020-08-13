package com.android.bible.knowbible.mvvm.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTextInfoModel
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.dialog.VerseDialog
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.Utility.Companion.convertDbInPx
import kotlin.text.StringBuilder

//FragmentManager нужен здесь для открытия диалога
class BibleTextRVAdapter(private val context: Context, private val models: ArrayList<BibleTextModel>, private val myFragmentManager: FragmentManager) : RecyclerView.Adapter<BibleTextRVAdapter.MyViewHolder>() {
    private var verseDialog: VerseDialog? = null

    private lateinit var fragmentChanger: IChangeFragment
    fun setFragmentChangerListener(fragmentChanger: IChangeFragment) {
        this.fragmentChanger = fragmentChanger
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_bible_verse_text, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //Устанавливаем анимацию на item
//        setAnimation(holder.itemView, position) //Немного влияет на производительность, пока будет отключено

        val verseNumber = models[position].verse_number

        //Чтобы установить выделенный цвет, нужно задержать немного время, чтобы устанавливать его тогда, когда адаптер окончательно отобразит все данные, после всех обновлений
        if (ThemeManager.theme == ThemeManager.Theme.DARK) {
            holder.tvVerse.setTextColor(ContextCompat.getColor(context, R.color.colorTextDarkTheme))
        } else {
            holder.tvVerse.setTextColor(ContextCompat.getColor(context, R.color.colorTextLightTheme))
        }
        if (models[position].textColorHex != null) {
            //Устанавливаем цвет выделенного текста, если выделение присутствует. Если же его нет, то пропускаем
            holder.tvVerse.setTextColor(Color.parseColor(models[position].textColorHex))
        }

        //Устанавливаем и отключаем жирный шрифт именно таким образом. Установка через параметр Typeface не подходит
        if (models[position].isTextBold) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.tvVerse.setTextAppearance(context, R.style.TextViewStyleBold)
            } else {
                holder.tvVerse.setTextAppearance(R.style.TextViewStyleBold)
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.tvVerse.setTextAppearance(context, R.style.TextViewStyleNormal)
            } else {
                holder.tvVerse.setTextAppearance(R.style.TextViewStyleNormal)
            }
        }

        if (models[position].isTextUnderline) holder.tvVerse.paintFlags = holder.tvVerse.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        else holder.tvVerse.paintFlags = holder.tvVerse.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() //Убираем подчёркивание

        //В зависимости от выбранной темы, выставляем нужные цвета для Views
        if (ThemeManager.theme == ThemeManager.Theme.BOOK) {
            holder.tvVerseNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGrayBookTheme))
        } else {
            holder.tvVerseNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
        }
        holder.tvVerseNumber.text = verseNumber.toString()

        //Удаляем пробел в начале текста, если он есть
        val textSB = StringBuilder(models[position].text)
        if (textSB[0] == ' ') models[position].text = textSB.deleteCharAt(0).toString()

        //switch case для проверки того, какое количество цифр в номере стиха. И в соответствии с этим выставляем нужный отступ  первой строчки для самого текста стиха
        when {
            verseNumber < 10 -> {
                holder.tvVerse.text = createIndentedText(models[position].text, convertDbInPx(context, 10f).toInt(), 0)
            }
            verseNumber < 100 -> {
                holder.tvVerse.text = createIndentedText(models[position].text, convertDbInPx(context, 14f).toInt(), 0)
            }
            else -> {
                holder.tvVerse.text = createIndentedText(models[position].text, convertDbInPx(context, 20f).toInt(), 0)
            }
        }
    }

    private fun createIndentedText(text: String, marginFirstLine: Int, marginNextLines: Int): SpannableString? {
        val result = SpannableString(text)
        result.setSpan(LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length, 0)
        return result
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) { // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
//        holder.clearAnimation()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), VerseDialog.VerseDialogListener {
        var selectedItem: Int = -1

        val tvVerseNumber: TextView = itemView.findViewById(R.id.tvVerseNumber)
        val tvVerse: TextView = itemView.findViewById(R.id.tvVerse)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            itemView.setOnClickListener {
                selectedItem = adapterPosition

                verseDialog = VerseDialog(this)
                verseDialog!!.setVerseData(models[adapterPosition])
                verseDialog!!.setFragmentManager(myFragmentManager)
                verseDialog!!.show(myFragmentManager, "Verse Dialog") //Тут должен быть именно childFragmentManager
            }
        }

        fun clearAnimation() {
            itemView.clearAnimation()
        }

        override fun dismissDialog() {
            verseDialog?.dismiss()
        }

        override fun updateItemColor(bibleTextInfo: BibleTextInfoModel) {
            val model = models[selectedItem]
            if (selectedItem != -1) {
                //Проверка на сходство на всякий случай
                if (model.book_number == bibleTextInfo.bookNumber
                        && model.chapter_number == bibleTextInfo.chapterNumber
                        && model.verse_number == bibleTextInfo.verseNumber) {
                    model.id = bibleTextInfo.id
                    model.textColorHex = bibleTextInfo.textColorHex
                    model.isTextBold = bibleTextInfo.isTextBold
                    model.isTextUnderline = bibleTextInfo.isTextUnderline
                    notifyItemChanged(selectedItem, Unit)
                    selectedItem = -1
                }
            }
        }
    }
}