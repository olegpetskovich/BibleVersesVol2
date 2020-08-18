package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.android.bible.knowbible.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomappbar.BottomAppBar

class MyBottomAppBarLayout
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null
) : BottomAppBar(context, attrs),
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
        backgroundTint = ContextCompat.getColorStateList(context, theme.bottomAppBarTheme.backgroundColor)
    }
}