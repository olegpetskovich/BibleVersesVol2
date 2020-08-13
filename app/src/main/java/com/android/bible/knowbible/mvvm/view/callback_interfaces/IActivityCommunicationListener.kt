package com.android.bible.knowbible.mvvm.view.callback_interfaces

import androidx.fragment.app.FragmentManager
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager

interface IActivityCommunicationListener {
    fun setTabNumber(tabNumber: Int)

    fun setBottomAppBarVisibility(visibility: Int)

    fun setFABVisibility(fabVisibility: Boolean)
    fun getFABVisibility(): Int

    fun setTheme(theme: ThemeManager.Theme, animate: Boolean = true)
    fun setShowHideToolbarBackButton(backButtonVisibility: Int)

    fun setNoteIdForDelete(noteId: Int)

    fun setShowHideArticlesInfoButton(articlesInfoBtnVisibility: Int)
    fun setShowHideAddNoteButton(addNoteBtnVisibility: Int)
    fun setShowHideDeleteNoteButton(deleteNoteBtnVisibility: Int)

    fun updateTabIconAndTextColor()

    fun setBtnSelectTranslationVisibility(visibility: Int)
    fun setBtnSelectTranslationText(selectedTranslation: String)
    fun setBtnSelectTranslationClickableState(clickableState: Boolean)

    fun setTvSelectedBibleTextVisibility(selectedTextVisibility: Int)
    fun setTvSelectedBibleText(selectedText: String, isBook: Boolean)

    fun setMyFragmentManager(myFragmentManager: FragmentManager)
    fun setIsBackStackNotEmpty(isBackStackNotEmpty: Boolean)

    fun setIsTranslationDownloaded(isTranslationDownloaded: Boolean)
}