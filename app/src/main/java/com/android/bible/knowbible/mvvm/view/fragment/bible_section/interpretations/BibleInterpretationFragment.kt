package com.android.bible.knowbible.mvvm.view.fragment.bible_section.interpretations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.adapter.InterpretationsVPAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.google.android.material.tabs.TabLayout

class BibleInterpretationFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_bible_interpretation, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        viewPager = myView.findViewById(R.id.viewPager)
        setupViewPager(viewPager)

        tabLayout = myView.findViewById(R.id.tabLayout)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = 2

        return myView
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = InterpretationsVPAdapter(childFragmentManager)

        val firstInterpretationFragment = FirstInterpretationFragment()
        adapter.addFragment(firstInterpretationFragment, getString(R.string.tab_first_interpretation))

        val secondInterpretationFragment = SecondInterpretationFragment()
        adapter.addFragment(secondInterpretationFragment, getString(R.string.tab_second_interpretation))

        val thirdInterpretationFragment = ThirdInterpretationFragment()
        adapter.addFragment(thirdInterpretationFragment, getString(R.string.tab_third_interpretation))

        viewPager.adapter = adapter
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
//        //Сообщаем, что BibleTextFragment открыт, а это значит, что BottomAppBar можно показывать
//        listener.setIsBibleTextFragmentOpened(true)
//
//        listener.setTabNumber(1)
//        listener.setMyFragmentManager(myFragmentManager)
//        listener.setIsBackStackNotEmpty(true)
//
//        listener.setShowHideToolbarBackButton(View.VISIBLE)
//
//        listener.setTvSelectedBibleTextVisibility(View.VISIBLE)
    }


    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }
}