package com.android.bible.knowbible.mvvm.view.fragment.bible_section.daily_verse_subsection

import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.google.android.material.button.MaterialButton
import java.util.*

class DailyVerseFragment : Fragment() {
    lateinit var myFragmentManager: FragmentManager

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var tvVerse: TextView
    private lateinit var btnFind: TextView

    private lateinit var btnList: MaterialButton
    private lateinit var btnShare: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_daily_verse, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        tvVerse = myView.findViewById(R.id.tvVerse)

        btnFind = myView.findViewById(R.id.btnFind)
        btnFind.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(context, R.anim.myanim)
//            tvVerse.text = getRandomObject(verses!!)
            tvVerse.startAnimation(animation)
        }

        btnList = myView.findViewById(R.id.btnList)
        btnList.setOnClickListener {
            val intent = Intent(context, ListActivity::class.java)
            startActivity(intent)
        }

        btnShare = myView.findViewById(R.id.btnShare)
        btnShare.setOnClickListener {
            if (tvVerse.text.toString() == getString(R.string.tv_find_your_daily_verse)) {
                Toast.makeText(context, getString(R.string.toast_find_verse), Toast.LENGTH_SHORT).show()
            }
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            val shareBody = getString(R.string.my_daily_verse) + " \n" + tvVerse.text.toString()
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_verse)))
        }

        when (ThemeManager.theme) {
            ThemeManager.Theme.LIGHT -> {
                btnList.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundLightTheme))
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextLightTheme))
                btnShare.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundLightTheme))
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextLightTheme))
            }
            ThemeManager.Theme.DARK -> {
                btnList.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundDarkTheme))
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextDarkTheme))
                btnShare.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundDarkTheme))
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextDarkTheme))
            }
            ThemeManager.Theme.BOOK -> {
                btnList.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundBookTheme))
                btnList.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextBookTheme))
                btnShare.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBackgroundBookTheme))
                btnShare.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorTextBookTheme))
            }
        }

        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    private fun getRandomObject(from: Collection<*>): String {
        val rnd = Random()
        val i = rnd.nextInt(from.size)
        return from.toTypedArray()[i].toString()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }
}
