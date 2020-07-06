package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.lang.reflect.Field

class MyEditText
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr),
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
//      typeface = FontCache["fonts/avenir_next_regular_cyr", context]
        setTextColor(ContextCompat.getColor(context, theme.editTextTheme.textColor))

        setHintTextColor(ContextCompat.getColor(context, theme.editTextTheme.hintColor))

        //Устанавливаем цвет курсора
        try {
            // Get the cursor resource id
            var field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId: Int = field.getInt(this)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor: Any = field.get(this)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(this.context, drawableResId)
            drawable?.colorFilter = context?.let { PorterDuffColorFilter(ContextCompat.getColor(context, theme.editTextTheme.cursorColor), PorterDuff.Mode.SRC_IN) }
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)
        } catch (ignored: Exception) {
        }
    }
}
