package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class MyView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
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
        setBackgroundColor(
                ContextCompat.getColor(
                        context,
                        theme.viewLineTheme.backgroundColor
                )
        )
    }
}