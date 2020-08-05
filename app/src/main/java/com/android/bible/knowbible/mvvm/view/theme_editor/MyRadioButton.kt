package com.android.bible.knowbible.mvvm.view.theme_editor

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat

class MyRadioButton
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatRadioButton(context, attrs, defStyleAttr),
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
        setTextColor(ContextCompat.getColor(context, theme.radioButtonTheme.textColor))

        val colorStateList = ColorStateList(arrayOf(intArrayOf(-R.attr.state_checked), intArrayOf(R.attr.state_checked)), intArrayOf(theme.radioButtonTheme.uncheckedColor, theme.radioButtonTheme.checkedColor))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CompoundButtonCompat.setButtonTintList(this, colorStateList)
        } else {
            buttonTintList = colorStateList
        }
    }
}
