package com.android.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.android.bible.knowbible.R

class MyLayoutInflater(private val delegate: AppCompatDelegate) : LayoutInflater.Factory2 {

    override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet
    ): View? {
        return when (name) {
            "TextView" -> MyTextView(context, attrs)
            "MaterialButton" -> MyButton(context, attrs, R.attr.materialButtonStyle)
            "LinearLayout" -> MyLinearLayout(context, attrs)
            "ConstraintLayout" -> MyConstraintLayout(context, attrs)
            "CoordinatorLayout" -> MyCoordinatorLayout(context, attrs)
            "RelativeLayout" -> MyRelativeLayout(context, attrs)
            "NestedScrollView" -> MyNestedScrollViewLayout(context, attrs)
            "AppBarLayout" -> MyAppBarLayout(context, attrs)
            "Toolbar" -> MyToolbar(context, attrs)
            "EditText" -> MyEditText(context, attrs)
            "TabLayout" -> MyTabLayout(context, attrs)
            "View" -> MyView(context, attrs)
            "ImageView" -> MyIconImageView(context, attrs)
            "AppCompatRadioButton" -> MyRadioButton(context, attrs)
            else -> delegate.createView(parent, name, context, attrs)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }
}