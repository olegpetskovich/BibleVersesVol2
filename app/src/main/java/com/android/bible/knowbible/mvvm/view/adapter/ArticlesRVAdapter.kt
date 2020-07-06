package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.mvvm.model.ArticleModel
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.articles_section.ArticleFragment
import com.android.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment.Companion.APP_LANGUAGE_CODE_KEY
import com.android.bible.knowbible.utility.SaveLoadData

//Поле isDataFromLocalDB нужна, чтобы определить, откуда взяты данные, с Firebase или с сохранённой БД
class ArticlesRVAdapter(private val context: Context, private val models: ArrayList<ArticleModel>) : RecyclerView.Adapter<ArticlesRVAdapter.MyViewHolder>() {
    private val saveLoadData = SaveLoadData(context)

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
        val view = inflater.inflate(R.layout.item_article, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //Устанавливаем анимацию на item
        setAnimation(holder.itemView, position)

        when (saveLoadData.loadString(APP_LANGUAGE_CODE_KEY)) {
            "en" -> {
                holder.tvArticleName.text = models[position].article_name_en

                models[position].article_text_en = models[position].article_text_en.replace("NL", "\n") //NL - New Line
                holder.tvArticleText.text = models[position].article_text_en
            }
            "ru" -> {
                holder.tvArticleName.text = models[position].article_name_ru

                models[position].article_text_ru = models[position].article_text_ru.replace("NL", "\n") //NL - New Line
                holder.tvArticleText.text = models[position].article_text_ru
            }
            "uk" -> {
                holder.tvArticleName.text = models[position].article_name_uk

                models[position].article_text_uk = models[position].article_text_uk.replace("NL", "\n") //NL - New Line
                holder.tvArticleText.text = models[position].article_text_uk
            }

        }

        if (models[position].isIs_article_new) {
            holder.tvNewArticle.visibility = View.VISIBLE
            holder.tvNewArticle.setTextColor(Color.parseColor(models[position].new_article_text_color))
        } else holder.tvNewArticle.visibility = View.GONE

        holder.ivArticleImage.setImageBitmap(models[position].imageBitmap)
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) { // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNewArticle: TextView = itemView.findViewById(R.id.tvNewArticle)
        val ivArticleImage: ImageView = itemView.findViewById(R.id.ivArticleImage)
        val tvArticleName: TextView = itemView.findViewById(R.id.tvArticleName)
        val tvArticleText: TextView = itemView.findViewById(R.id.tvArticleText)

        init {
            themeChanger.changeItemTheme() //Смена темы для айтемов

            itemView.setOnClickListener {
                //Отключать возможность открывать статью, пока идёт скачивание. Потому что если этого не делать, то статья откроется,
                //но нажать кнопку "Назад" не получится, потому что во время скачивания кнопка "Назад" отключается
                if (saveLoadData.loadBoolean(BibleTranslationsRVAdapter.isTranslationDownloading)) {
                    Toast.makeText(context, context.getString(R.string.toast_please_wait), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                val articleFragment = ArticleFragment()

                val bitmap = (ivArticleImage.drawable as BitmapDrawable).bitmap

                when (saveLoadData.loadString(APP_LANGUAGE_CODE_KEY)) {
                    "ru" -> {
                        articleFragment.setArticleData(bitmap, models[adapterPosition].article_name_ru, models[adapterPosition].article_text_ru, models[adapterPosition].author_name_ru)
                    }
                    "uk" -> {
                        articleFragment.setArticleData(bitmap, models[adapterPosition].article_name_uk, models[adapterPosition].article_text_uk, models[adapterPosition].author_name_uk)
                    }
                    "en" -> {
                        articleFragment.setArticleData(bitmap, models[adapterPosition].article_name_en, models[adapterPosition].article_text_en, models[adapterPosition].author_name_en)
                    }
                }
                fragmentChanger.changeFragment(articleFragment)
            }
        }
    }
}