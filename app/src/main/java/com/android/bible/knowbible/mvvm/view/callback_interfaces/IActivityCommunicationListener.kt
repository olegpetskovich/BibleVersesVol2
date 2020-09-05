package com.android.bible.knowbible.mvvm.view.callback_interfaces

import androidx.fragment.app.FragmentManager
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager

interface IActivityCommunicationListener {
    fun setTabNumber(tabNumber: Int)

    fun disableMultiSelection()
    fun disableMultiSelectionIfBottomAppBarBtnClicked()

    //Это нужно для управления BottomAppBar в BibleTextFragment
    fun setBibleTextFragment(bibleTextFragment: BibleTextFragment)

    fun setIsBibleTextFragmentOpened(isBibleTextFragmentOpened: Boolean)

    fun setTheme(theme: ThemeManager.Theme, animate: Boolean = true)
    fun setShowHideToolbarBackButton(backButtonVisibility: Int)

    fun setNoteIdForDelete(noteId: Int)

    fun setShowHideMultiSelectionPanel(isVisible: Boolean)
    fun sendMultiSelectedTextsData(multiSelectedTextsList: ArrayList<BibleTextModel>)

    fun setShowHideArticlesInfoButton(articlesInfoBtnVisibility: Int)
    fun setShowHideAddNoteButtonFAB(addNoteFABBtnVisibility: Int)
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