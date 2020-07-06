package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MyButton
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialButton(context, attrs, defStyleAttr),
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
        setTextColor(ContextCompat.getColor(context, theme.buttonTheme.textColor))
        backgroundTintList = ContextCompat.getColorStateList(context, theme.buttonTheme.backgroundTint)
        rippleColor = ContextCompat.getColorStateList(context, theme.buttonTheme.rippleColor)
        strokeColor = ContextCompat.getColorStateList(context, theme.buttonTheme.strokeColor)
        iconTint = ContextCompat.getColorStateList(context, theme.buttonTheme.iconColor)
    }
}
