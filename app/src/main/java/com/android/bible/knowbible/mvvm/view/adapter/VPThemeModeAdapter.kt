package com.android.bible.knowbible.mvvm.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ThemeInfoModel
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.BOOK_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.DARK_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.LIGHT_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.THEME_NAME_KEY
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.*

class VPThemeModeAdapter(private val context: Context, private val models: ArrayList<ThemeInfoModel>) : PagerAdapter() {
    private val saveLoadData = SaveLoadData(context)

    private lateinit var themeChanger: IThemeChanger
    fun setThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    interface IThemeChanger {
        fun changeItemTheme(theme: ThemeManager.Theme)
        fun updateTabIconAndTextColor()
    }

    override fun getCount(): Int {
        return models.size
    }

    override fun isViewFromObject(@NonNull view: View, @NonNull o: Any): Boolean {
        return view == o
    }

    override fun destroyItem(@NonNull container: ViewGroup, position: Int, @NonNull `object`: Any) {
        container.removeView(`object` as View?)
    }


    @NonNull
    override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_theme_card, container, false)

        val themeCard: MaterialCardView = view!!.findViewById(R.id.themeCard)
        if (models[position].theme == ThemeManager.Theme.BOOK) {
//            val drawable = GradientDrawable(GradientDrawable.Orientation.BR_TL, intArrayOf(
//                    Color.parseColor("#9f7928"),
//                    Color.parseColor("#d8c080"),
//                    Color.parseColor("#c19d4a"),
//                    Color.parseColor("#c6ad6d"),
//                    Color.parseColor("#9f7928")))
//            drawable.shape = GradientDrawable.RECTANGLE
//            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            val layoutTheme: RelativeLayout = view.findViewById(R.id.layoutTheme)
//            layoutTheme.background = drawable
            layoutTheme.background = ContextCompat.getDrawable(context, R.drawable.texture_book_background)
        }
        themeCard.setCardBackgroundColor(ContextCompat.getColor(context, models[position].cardBackgroundColorRes))

        val themeImage: ImageView = view.findViewById(R.id.themeImage)
        themeImage.setImageResource(models[position].themeImageRes)
        val themeImage2: ImageView = view.findViewById(R.id.themeImage2)
        themeImage2.setImageResource(models[position].themeImageRes)

        val themeName: TextView = view.findViewById(R.id.themeName)
        themeName.setText(models[position].nameRes)
        themeName.setTextColor(Color.parseColor(models[position].nameColorRes))

        val btnApplyTheme: MaterialButton = view.findViewById(R.id.btnApplyTheme)
        btnApplyTheme.setTextColor(Color.parseColor(models[position].btnTextColor))
        btnApplyTheme.backgroundTintList = ContextCompat.getColorStateList(context, models[position].btnBackgroundColor)
        btnApplyTheme.setOnClickListener {
            //Проверка, если выбирается тема, не установленная ранее, то переключаем тему приложения, если же нажимаем на ту, которая уже установлена, то смены не происходит
            if (ThemeManager.theme == models[position].theme) return@setOnClickListener

            when (models[position].theme) {
                ThemeManager.Theme.LIGHT -> saveLoadData.saveString(THEME_NAME_KEY, LIGHT_THEME)
                ThemeManager.Theme.DARK -> saveLoadData.saveString(THEME_NAME_KEY, DARK_THEME)
                ThemeManager.Theme.BOOK -> saveLoadData.saveString(THEME_NAME_KEY, BOOK_THEME)
            }
            themeChanger.changeItemTheme(models[position].theme)
            themeChanger.updateTabIconAndTextColor() //Отправляем выбранную тему, чтобы обновить цвета иконок и текста в TabLayout на нужные
        }

        container.addView(view)
        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}