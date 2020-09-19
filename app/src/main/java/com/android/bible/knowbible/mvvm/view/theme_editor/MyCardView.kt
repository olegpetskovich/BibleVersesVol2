package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MyCardView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialCardView(context, attrs, defStyleAttr),
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
        rippleColor = ContextCompat.getColorStateList(context, theme.buttonTheme.rippleColor)
        strokeColor = ContextCompat.getColor(context, theme.buttonTheme.strokeColor)
        setCardBackgroundColor(ContextCompat.getColorStateList(context, theme.buttonTheme.backgroundTint))
    }
}
