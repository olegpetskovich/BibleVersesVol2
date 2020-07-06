package com.apps.oleg.bibleverses.mvvm.view.fragment.more_section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData

class SettingsFragment : Fragment() {
    companion object {
        const val nightMode: String = "night_mode"
    }

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_settings, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(context!!)

        myFragmentManager.let {
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)

            val btnInterfaceLanguage: RelativeLayout = myView.findViewById(R.id.btnInterfaceLanguage)
            btnInterfaceLanguage.setOnClickListener {
                val appLanguageFragment = AppLanguageFragment()
                appLanguageFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, appLanguageFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            val btnTheme: RelativeLayout = myView.findViewById(R.id.btnTheme)
            btnTheme.setOnClickListener {
                val themeFragment = ThemeModeFragment()
                themeFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, themeFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

//        val switchDarkTheme: SwitchCompat = myView.findViewById(R.id.switchDarkTheme)
//        if (saveLoadData.loadBoolean(nightMode)) {
//            switchDarkTheme.isChecked = true
//            switchDarkTheme.trackTintList = context?.let { ContextCompat.getColorStateList(it, R.color.colorChecked) }
//        } else {
//            switchDarkTheme.isChecked = false
//            switchDarkTheme.trackTintList = context?.let { ContextCompat.getColorStateList(it, R.color.colorUnchecked) }
//        }
//        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                saveLoadData.saveBoolean(nightMode, true) //Сохраняем состояние SwitchView
//                switchDarkTheme.trackTintList = context?.let { ContextCompat.getColorStateList(it, R.color.colorChecked) }
//                listener.setTheme(ThemeManager.Theme.DARK, true)
//
//            } else {
//                saveLoadData.saveBoolean(nightMode, false) //Сохраняем состояние SwitchView
//                switchDarkTheme.trackTintList = context?.let { ContextCompat.getColorStateList(it, R.color.colorUnchecked) }
//                listener.setTheme(ThemeManager.Theme.LIGHT, true)
//            }
//        }
        return myView
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
}