package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.android.bible.knowbible.R

class MyRelativeLayout
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr),
        ThemeManager.ThemeChangedListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ThemeManager.addListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: ThemeManager.Theme) {
        if (ThemeManager.theme == ThemeManager.Theme.BOOK) {
//            val drawable = GradientDrawable(GradientDrawable.Orientation.BR_TL, intArrayOf(
//                    Color.parseColor("#9f7928"),
//                    Color.parseColor("#d8c080"),
//                    Color.parseColor("#c19d4a"),
//                    Color.parseColor("#c6ad6d"),
//                    Color.parseColor("#9f7928")))
//            drawable.shape = GradientDrawable.RECTANGLE
//            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
//
//            background = drawable
            background = ContextCompat.getDrawable(context, R.drawable.texture_book_background)
        } else setBackgroundColor(
                ContextCompat.getColor(
                        context,
                        theme.viewGroupTheme.backgroundColor
                )
        )
    }
}