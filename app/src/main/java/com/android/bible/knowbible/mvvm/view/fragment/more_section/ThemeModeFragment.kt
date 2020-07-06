package com.android.bible.knowbible.mvvm.view.fragment.more_section

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ThemeInfoModel
import com.android.bible.knowbible.mvvm.view.adapter.VPThemeModeAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData


class ThemeModeFragment : Fragment(), VPThemeModeAdapter.IThemeChanger {
    companion object {
        const val THEME_NAME_KEY: String = "THEME_NAME_KEY"

        const val LIGHT_THEME: String = "LIGHT_THEME"
        const val DARK_THEME: String = "DARK_THEME"
        const val BOOK_THEME: String = "BOOK_THEME"
    }

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_theme_mode, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)

        val adapter = VPThemeModeAdapter(context!!, getViewPagerData())
        adapter.setThemeChangerListener(this)

        val viewPager: ViewPager = myView.findViewById(R.id.horizontalViewPager)
        viewPager.adapter = adapter

        //В зависимости от выбранной темы, открываем айтем выбранной темы в ViewPager
        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> viewPager.currentItem = 0
            ThemeManager.Theme.DARK -> viewPager.currentItem = 1
            ThemeManager.Theme.BOOK -> viewPager.currentItem = 2
        }

        return myView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val themeFragment = ThemeModeFragment()
            themeFragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_more, themeFragment)
            transaction.commit()
        }
    }


    private fun getViewPagerData(): ArrayList<ThemeInfoModel> {
//        val btnTextColor = arrayOf("#ffffff", "#ffffff", "#ffffff")
//        val btnBackgroundColor = arrayOf("#f6c457", "#22526b", "#a66a41")
//        val themeNamesColor = arrayOf("#f6c457", "#ffffff", "#a66a41")
//        val themeNames = arrayOf(R.string.day_theme, R.string.night_theme, R.string.book_theme)
//        val themeImages = arrayOf(R.drawable.day_theme, R.drawable.night_theme, R.drawable.book_theme)
//        val themeBackgrounds = arrayOf(R.color.colorBackgroundLightTheme, R.color.colorBackgroundDarkTheme, R.color.colorBackgroundBookTheme)
//        val themeEnums = arrayOf(ThemeManager.Theme.LIGHT, ThemeManager.Theme.DARK, ThemeManager.Theme.BOOK)

//        val themeInfoList = ArrayList<ThemeInfoModel>()
//        for (position in themeBackgrounds.indices) {
//            val model = ThemeInfoModel(btnTextColor[position], btnBackgroundColor[position], themeNamesColor[position], themeNames[position], themeImages[position], themeBackgrounds[position], themeEnums[position])
//            themeInfoList.add(model)
//        }

        val themeInfoList = ArrayList<ThemeInfoModel>()
        themeInfoList.add(ThemeInfoModel("#ffffff", R.color.colorButtonApplyLightTheme, "#006EFF", R.string.day_theme, R.drawable.day_theme, R.color.colorBackgroundLightTheme, ThemeManager.Theme.LIGHT))
        themeInfoList.add(ThemeInfoModel("#ffffff", R.color.colorButtonApplyDarkTheme, "#ffffff", R.string.night_theme, R.drawable.night_theme, R.color.colorBackgroundDarkTheme, ThemeManager.Theme.DARK))
        themeInfoList.add(ThemeInfoModel("#ffffff", R.color.colorButtonApplyBookTheme, "#a66a41", R.string.book_theme, R.drawable.book_theme, R.color.colorBackgroundBookTheme, ThemeManager.Theme.BOOK))

        return themeInfoList
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(2)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun changeItemTheme(theme: ThemeManager.Theme) {
        listener.setTheme(theme, true)
    }

    override fun updateTabIconAndTextColor() {
        listener.updateTabIconAndTextColor()
    }
}