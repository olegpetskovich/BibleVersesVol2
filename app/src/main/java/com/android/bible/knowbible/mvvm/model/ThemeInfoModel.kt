package com.android.bible.knowbible.mvvm.model

import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager

data class ThemeInfoModel(val btnTextColor: String, val btnBackgroundColor: Int, val nameColorRes: String, val nameRes: Int, val themeImageRes: Int, val cardBackgroundColorRes: Int, val theme: ThemeManager.Theme)